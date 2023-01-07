package dog.abcd.walkwoman.view.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.bumptech.glide.Glide
import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.ktx.immersionBar
import com.gyf.immersionbar.ktx.statusBarHeight
import com.jeremyliao.liveeventbus.LiveEventBus
import com.luck.picture.lib.tools.ScreenUtils
import dog.abcd.walkwoman.R
import dog.abcd.walkwoman.base.BaseFragment
import dog.abcd.walkwoman.base.QuickAdapter
import dog.abcd.walkwoman.base.ViewBindingHolder
import dog.abcd.walkwoman.constant.EventKeys
import dog.abcd.walkwoman.databinding.FragmentSongsBinding
import dog.abcd.walkwoman.databinding.ItemSongBinding
import dog.abcd.walkwoman.model.LocalMediaModel
import dog.abcd.walkwoman.model.bean.Song
import dog.abcd.walkwoman.utils.changePlaylist
import dog.abcd.walkwoman.utils.start


class SongsFragment : BaseFragment<FragmentSongsBinding>() {

    val songAdapter = SongAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        immersionBar {
            bind.rlTitle.setPadding(0, statusBarHeight, 0, 0)
            fitsSystemWindows(false)
            transparentBar()
        }

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
                    songAdapter.data.lastIndex -> {
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
        LiveEventBus.get<List<Song>>(EventKeys.localSongs).observeSticky(this) {
            songAdapter.setList(it)
            bind.refreshLayout.finishRefresh()
        }
        bind.refreshLayout.setOnRefreshListener {
            LocalMediaModel.refresh()
        }
    }

    class SongAdapter : QuickAdapter<Song, ItemSongBinding>() {
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