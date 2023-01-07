package dog.abcd.walkwoman.view.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.ktx.immersionBar
import com.gyf.immersionbar.ktx.statusBarHeight
import com.luck.picture.lib.tools.ScreenUtils
import dog.abcd.walkwoman.R
import dog.abcd.walkwoman.base.BaseFragment
import dog.abcd.walkwoman.base.QuickAdapter
import dog.abcd.walkwoman.base.ViewBindingHolder
import dog.abcd.walkwoman.databinding.FragmentAlbumsBinding
import dog.abcd.walkwoman.databinding.ItemSongBinding
import dog.abcd.walkwoman.model.bean.Song
import dog.abcd.walkwoman.utils.changePlaylist
import dog.abcd.walkwoman.utils.start

class AlbumsFragment : BaseFragment<FragmentAlbumsBinding>() {

    val songs = ArrayList<Song>()
    val songAdapter = SongAdapter(songs)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        immersionBar {
            bind.rlTitle.setPadding(0, statusBarHeight, 0, 0)
            fitsSystemWindows(false)
            transparentBar()
        }

        bind.rvSong.layoutManager = LinearLayoutManager(context)
        bind.rvSong.adapter = songAdapter
        bind.rvSong.addItemDecoration(object : RecyclerView.ItemDecoration() {
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