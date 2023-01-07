package dog.abcd.walkwoman.services

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.os.Binder
import android.os.IBinder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.jeremyliao.liveeventbus.LiveEventBus
import dog.abcd.walkwoman.R
import dog.abcd.walkwoman.base.App
import dog.abcd.walkwoman.constant.EventKeys
import dog.abcd.walkwoman.model.bean.Song
import dog.abcd.walkwoman.utils.PlaybackController

class PlaybackService : Service(), PlaybackController {

    val tag = this.javaClass.simpleName
    var mediaPlayer: MediaPlayer? = null
    lateinit var mediaSession: MediaSession

    var shuffle = false
    var current = -1
        set(value) {
            field = value
            if (value < 0) {
                currentSong = null
            } else {
                currentSong = playlist[value]
            }
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
            LiveEventBus.get<Boolean>(EventKeys.playing).post(field)
        }

    override fun onBind(intent: Intent?): IBinder {
        return PlaybackBinder()
    }

    class PlaybackBinder : Binder() {
    }

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession(this, tag)
        App.instance.controller = this
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
        val metaData = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadata.METADATA_KEY_TITLE, song.title)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, song.duration)
            .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, null)

        // there only about notification's album art, so remove "isAlbumArtOnLockScreen" and "isBlurredAlbumArt"
        Glide.with(this)
            .asBitmap()
            .load(song.albumArt)
            .into(object : CustomTarget<Bitmap?>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    metaData.putBitmap(
                        MediaMetadata.METADATA_KEY_ALBUM_ART,
                        BitmapFactory.decodeResource(
                            resources,
                            R.mipmap.default_audio_art
                        )
                    )
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?,
                ) {
                    metaData.putBitmap(
                        MediaMetadata.METADATA_KEY_ALBUM_ART,
                        resource
                    )
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }
}