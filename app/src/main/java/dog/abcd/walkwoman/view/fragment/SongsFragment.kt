package dog.abcd.walkwoman.view.fragment

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Rect
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.bumptech.glide.Glide
import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.ktx.immersionBar
import com.gyf.immersionbar.ktx.statusBarHeight
import com.luck.picture.lib.tools.ScreenUtils
import dog.abcd.walkwoman.R
import dog.abcd.walkwoman.base.BaseFragment
import dog.abcd.walkwoman.base.QuickAdapter
import dog.abcd.walkwoman.base.ViewBindingHolder
import dog.abcd.walkwoman.databinding.FragmentSongsBinding
import dog.abcd.walkwoman.databinding.ItemSongBinding
import dog.abcd.walkwoman.model.bean.Song
import dog.abcd.walkwoman.utils.changePlaylist
import dog.abcd.walkwoman.utils.start


class SongsFragment : BaseFragment<FragmentSongsBinding>() {
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION
    )

    var selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

    val songs = ArrayList<Song>()
    val songAdapter = SongAdapter(songs)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        immersionBar {
            bind.rlTitle.setPadding(0, statusBarHeight, 0, 0)
            fitsSystemWindows(false)
            transparentBar()
        }
        val resolver: ContentResolver = context.contentResolver
        val uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? =
            resolver.query(uri, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER)
        when {
            cursor == null -> {
                // query failed, handle error.
            }
            !cursor.moveToFirst() -> {
                // no media on the device
            }
            else -> {
                val titleColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
                val idColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
                val artistColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
                val dataColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA)
                val displayNameColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DISPLAY_NAME)
                val albumIdColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DURATION)
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
                                getLong(durationColumn)
                            )
                        )

                    }
                } while (cursor.moveToNext())
            }
        }
        cursor?.close()

        bind.rvSong.layoutManager = LinearLayoutManager(context)
        bind.rvSong.adapter = songAdapter
        bind.rvSong.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                when (position) {
                    songs.lastIndex -> {
                        outRect.bottom = ScreenUtils.dip2px(context, 88f) +
                                ImmersionBar.getNavigationBarHeight(context)
                    }
                    0 -> {
                        outRect.top = bind.rlTitle.measuredHeight
                    }
                    else -> {
                        outRect.setEmpty()
                    }
                }
            }
        })
        songAdapter.setOnItemClickListener { _, _, position ->
            changePlaylist(songAdapter.data)
            start(songAdapter.getItem(position))
        }
    }

    class SongAdapter(list: MutableList<Song>) : QuickAdapter<Song, ItemSongBinding>(list) {
        override fun convert(holder: ViewBindingHolder<ItemSongBinding>, item: Song) {
            holder.bind.tvTitle.text = item.title
            holder.bind.tvArtist.text = item.artist


            Glide.with(holder.bind.ivAlbum)
                .load(item.albumArt)
                .placeholder(R.mipmap.default_audio_art)
                .error(R.mipmap.default_audio_art)
                .into(holder.bind.ivAlbum)
        }
    }

}