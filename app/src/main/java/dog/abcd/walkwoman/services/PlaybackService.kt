package dog.abcd.walkwoman.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.jeremyliao.liveeventbus.LiveEventBus
import dog.abcd.walkwoman.BuildConfig
import dog.abcd.walkwoman.R
import dog.abcd.walkwoman.base.App
import dog.abcd.walkwoman.constant.Constant.ACTION_PAUSE
import dog.abcd.walkwoman.constant.Constant.ACTION_PENDING_QUIT
import dog.abcd.walkwoman.constant.Constant.ACTION_PLAY
import dog.abcd.walkwoman.constant.Constant.ACTION_QUIT
import dog.abcd.walkwoman.constant.Constant.ACTION_REWIND
import dog.abcd.walkwoman.constant.Constant.ACTION_SKIP
import dog.abcd.walkwoman.constant.Constant.ACTION_STOP
import dog.abcd.walkwoman.constant.Constant.ACTION_TOGGLE_PAUSE
import dog.abcd.walkwoman.constant.EventKeys
import dog.abcd.walkwoman.model.LocalMediaModel
import dog.abcd.walkwoman.model.bean.Song
import dog.abcd.walkwoman.notification.PlayingNotification
import dog.abcd.walkwoman.notification.PlayingNotificationImpl24
import dog.abcd.walkwoman.utils.PlaybackController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PlaybackService : Service(), PlaybackController {

    val tag = this.javaClass.simpleName
    var mediaPlayer: MediaPlayer? = null
    lateinit var mediaSession: MediaSessionCompat

    var shuffle = false
    var current = -1
        set(value) {
            field = value
            if (playlist.isEmpty() || field < 0) {
                field = -1
                currentSong = null
                return
            }
            currentSong = playlist[value]
        }
    var currentSong: Song? = null
        set(value) {
            field = value
            LiveEventBus.get<Song?>(EventKeys.currentSong).post(field)
        }
    val playlistOrigin = ArrayList<Song>()
    val playlist = ArrayList<Song>()

    var playing = false
        set(value) {
            field = value
            playingNotification?.setPlaying(field)
            LiveEventBus.get<Boolean>(EventKeys.playing).post(field)
            startForegroundOrNotify()
        }

    override fun onBind(intent: Intent?): IBinder {
        return PlaybackBinder()
    }

    class PlaybackBinder : Binder() {
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)

        val mediaButtonReceiverComponentName = ComponentName(
            applicationContext,
            MediaButtonIntentReceiver::class.java
        )

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.component = mediaButtonReceiverComponentName
        val mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(
            applicationContext, 0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSessionCompat(
            this,
            BuildConfig.APPLICATION_ID,
            mediaButtonReceiverComponentName,
            mediaButtonReceiverPendingIntent
        )
        App.instance.controller = this
        initNotification()
    }

    override fun changePlaylist(list: List<Song>) {
        playlistOrigin.clear()
        playlistOrigin.addAll(list)
        playlist.clear()
        playlist.addAll(list)
        if (shuffle) {
            shuffle(shuffle)
        }
    }

    override fun nextPlay(song: Song) {
        playlist.add(current + 1, song)
    }

    override fun shuffle(shuffle: Boolean) {
        this.shuffle = shuffle
        if (shuffle) {
            //打乱
            playlist.shuffle()
        } else {
            playlist.clear()
            playlist.addAll(playlistOrigin)
            current = playlist.indexOf(currentSong)
        }
    }

    override fun pause() {
        mediaPlayer?.pause()
        playing = false
    }

    override fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        playing = false
    }

    override fun start(song: Song) {
        stop()
        current = playlist.indexOf(song)
        prepare()
    }

    override fun start() {
        if (playlist.isEmpty()) {
            //如果点播放的时候是空的播放列表，就拿所有歌曲默认开始播放(或者拿上一次的播放列表)
            changePlaylist(LocalMediaModel.songs)
        }
        if (current < 0) {
            current = 0
            currentSong?.let { start(it) }
            return
        }
        if (mediaPlayer == null) {
            currentSong?.let { start(it) }
            return
        }
        mediaPlayer?.start()
        playing = true
    }

    override fun next() {
        stop()
        current = if (current < playlist.lastIndex) current + 1 else 0
        prepare()
    }

    override fun previous() {
        stop()
        current = if (current == 0) playlist.lastIndex else current - 1
        prepare()
    }

    private fun prepare() {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(CONTENT_TYPE_MUSIC)
                .build()
        )
        mediaPlayer?.setDataSource(currentSong!!.data)
        mediaPlayer?.setOnPreparedListener {
            it.setOnPreparedListener(null)
            it.start()
            playing = true
        }
        mediaPlayer?.setOnCompletionListener { next() }
        mediaPlayer?.setOnErrorListener { mp, what, extra ->
            next()
            false
        }
        mediaPlayer?.prepareAsync()
        currentSong?.let { updateMediaSession(it) }
        startForegroundOrNotify()
    }

    override fun seek(msec: Int) {
        mediaPlayer?.seekTo(msec)
    }

    override fun offTimer(millis: Int) {
        //TODO 计时器停止
    }

    override val isPlaying: Boolean
        get() {
            return playing
        }

    override val progress: Int
        get() {
            return mediaPlayer?.currentPosition ?: 0
        }

    fun updateMediaSession(song: Song) {
        val metaData = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, song.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.bucketDisplayName)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, song.cdTrackNumber)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null)
            .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, playlist.size.toLong())

        Glide.with(this)
            .asBitmap()
            .load(song.albumArt)
            .into(object : CustomTarget<Bitmap?>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    metaData.putBitmap(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                        BitmapFactory.decodeResource(
                            resources,
                            R.mipmap.default_audio_art
                        )
                    )
                    mediaSession.setMetadata(metaData.build())
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?,
                ) {
                    metaData.putBitmap(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                        resource
                    )
                    mediaSession.setMetadata(metaData.build())
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
        playingNotification?.updateMetadata(song) {}
    }

    private var notificationManager: NotificationManager? = null
    private var playingNotification: PlayingNotification? = null

    fun initNotification() {
        playingNotification =
            PlayingNotificationImpl24.from(this, notificationManager!!, mediaSession.sessionToken)
    }

    private fun startForegroundOrNotify() {
        if (playingNotification != null && currentSong != null) {
            startForeground(
                PlayingNotification.NOTIFICATION_ID, playingNotification!!.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            // If we are already in foreground just update the notification
            notificationManager?.notify(
                PlayingNotification.NOTIFICATION_ID, playingNotification!!.build()
            )
        }
    }

    var pendingQuit = false
    private val serviceScope = CoroutineScope(Job() + Dispatchers.Main)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action != null) {
            serviceScope.launch {
                when (intent.action) {
                    ACTION_TOGGLE_PAUSE -> if (isPlaying) {
                        pause()
                    } else {
                        start()
                    }
                    ACTION_PAUSE -> pause()
                    ACTION_PLAY -> start()
//                    ACTION_PLAY_PLAYLIST -> playFromPlaylist(intent)
                    ACTION_REWIND -> previous()
                    ACTION_SKIP -> next()
                    ACTION_STOP, ACTION_QUIT -> {
                        stop()
                    }
                    ACTION_PENDING_QUIT -> pendingQuit = true
//                    TOGGLE_FAVORITE ->
                }
            }
        }
        return START_NOT_STICKY
    }
}