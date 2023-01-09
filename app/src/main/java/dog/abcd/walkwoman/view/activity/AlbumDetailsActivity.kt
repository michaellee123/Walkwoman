package dog.abcd.walkwoman.view.activity

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.Slide
import android.view.Gravity
import android.view.View
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.ktx.immersionBar
import com.luck.picture.lib.tools.ScreenUtils
import dog.abcd.walkwoman.R
import dog.abcd.walkwoman.base.App
import dog.abcd.walkwoman.base.BaseActivity
import dog.abcd.walkwoman.databinding.ActivityAlbumDetailsBinding
import dog.abcd.walkwoman.databinding.LayoutAlbumHeaderBinding
import dog.abcd.walkwoman.model.LocalMediaModel
import dog.abcd.walkwoman.model.bean.Album
import dog.abcd.walkwoman.model.bean.Song
import dog.abcd.walkwoman.utils.changePlaylist
import dog.abcd.walkwoman.utils.start
import dog.abcd.walkwoman.view.adapter.SongAdapter

class AlbumDetailsActivity : BaseActivity<ActivityAlbumDetailsBinding>() {

    lateinit var album: Album

    val songAdapter = SongAdapter()

    lateinit var headerBinding: LayoutAlbumHeaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        postponeEnterTransition()
        album = intent.getSerializableExtra("album") as Album
        window.exitTransition = Fade()
        super.onCreate(savedInstanceState)
        immersionBar {
            transparentBar()
            fitsSystemWindows(false)
            statusBarDarkFont(false)
        }

    }

    override fun initView() {
        headerBinding = LayoutAlbumHeaderBinding.inflate(layoutInflater)

        ViewCompat.setTransitionName(headerBinding.ivAlbum, "album:" + album.albumId)

        bind.rvSong.layoutManager = LinearLayoutManager(this)
        songAdapter.setHeaderView(headerBinding.root)

        bind.rvSong.adapter = songAdapter


        headerBinding.root.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        val lp = headerBinding.ivAlbum.layoutParams
        lp.height = ScreenUtils.getScreenWidth(this) - ScreenUtils.dip2px(this, 24f)
        headerBinding.ivAlbum.layoutParams = lp

        headerBinding.tvAlbum.text = album.album
        headerBinding.tvMore.text = album.artist


        Handler(Looper.getMainLooper()).postDelayed({

            Glide.with(headerBinding.ivAlbum)
                .asDrawable()
                .load(album.albumArt)
                .error(R.mipmap.default_album_art)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        headerBinding.ivAlbum.setImageDrawable(resource)
                        startPostponedEnterTransition()
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        headerBinding.ivAlbum.setImageDrawable(errorDrawable)
                        startPostponedEnterTransition()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                })

        }, 10)

        bind.rvSong.addItemDecoration(object : ItemDecoration() {

            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                if (parent.getChildAdapterPosition(view) == songAdapter.data.lastIndex + songAdapter.headerLayoutCount) {
                    outRect.bottom = ImmersionBar.getNavigationBarHeight(this@AlbumDetailsActivity)
                } else {
                    outRect.bottom = 0
                }
            }

        })

        songAdapter.setOnItemClickListener { _, _, position ->
            changePlaylist(songAdapter.data)
            start(songAdapter.getItem(position))
        }

        querySongs()
    }

    fun querySongs() {
        val songs = ArrayList<Song>()
        var selection =
            MediaStore.Audio.Media.IS_MUSIC + " != 0 and " + MediaStore.Audio.Media.ALBUM_ID + " == " + album.albumId
        val resolver: ContentResolver = App.instance.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? =
            resolver.query(
                uri,
                LocalMediaModel.projection,
                selection,
                null,
                MediaStore.Audio.Media.CD_TRACK_NUMBER
            )
        when {
            cursor == null -> {
                // query failed, handle error.
            }
            !cursor.moveToFirst() -> {
                // no media on the device
            }
            else -> {
                val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val displayNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val bitrateColumn = cursor.getColumnIndex(MediaStore.Audio.Media.BITRATE)
                val discNumberColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISC_NUMBER)
                val genreColumn = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
                val isFavoriteColumn = cursor.getColumnIndex(MediaStore.Audio.Media.IS_FAVORITE)
                val numTracksColumn = cursor.getColumnIndex(MediaStore.Audio.Media.NUM_TRACKS)
                val bucketDisplayNameColumn =
                    cursor.getColumnIndex(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
                val cdTrackNumberColumn =
                    cursor.getColumnIndex(MediaStore.Audio.Media.CD_TRACK_NUMBER)
                val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                do {
                    cursor.apply {
                        songs.add(
                            Song(
                                getLong(idColumn),
                                getString(artistColumn),
                                getString(titleColumn),
                                getString(dataColumn),
                                getString(displayNameColumn),
                                getLong(albumIdColumn),
                                getLong(durationColumn),
                                getLong(bitrateColumn),
                                getLong(discNumberColumn),
                                getStringOrNull(genreColumn) ?: LocalMediaModel.unknown,
                                getInt(isFavoriteColumn) != 0,
                                getLong(numTracksColumn),
                                getString(bucketDisplayNameColumn),
                                getLongOrNull(cdTrackNumberColumn) ?: 0,
                                getStringOrNull(albumColumn) ?: LocalMediaModel.unknown
                            )
                        )

                    }
                } while (cursor.moveToNext())
            }
        }
        cursor?.close()
        songAdapter.setList(songs)
    }

    override fun finishAfterTransition() {
        super.finishAfterTransition()
    }
}