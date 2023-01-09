package dog.abcd.walkwoman.view.fragment

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import dog.abcd.walkwoman.databinding.FragmentAlbumsBinding
import dog.abcd.walkwoman.databinding.ItemAlbumBinding
import dog.abcd.walkwoman.model.LocalMediaModel
import dog.abcd.walkwoman.model.bean.Album
import dog.abcd.walkwoman.view.activity.AlbumDetailsActivity


class AlbumsFragment : BaseFragment<FragmentAlbumsBinding>() {

    val albumAdapter = AlbumAdapter()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        immersionBar {
            bind.rlTitle.setPadding(0, statusBarHeight, 0, 0)
            fitsSystemWindows(false)
            transparentBar()
        }

        bind.rvAlbum.layoutManager = GridLayoutManager(context, 2)
        bind.rvAlbum.adapter = albumAdapter
        bind.rvAlbum.addItemDecoration(object : RecyclerView.ItemDecoration() {
            val dp12 = ScreenUtils.dip2px(context, 12f)
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position and 1 == 0) {
                    //左边
                    outRect.left = dp12 / 2
                    outRect.right = 0
                } else {
                    outRect.left = 0
                    outRect.right = dp12 / 2
                }
                outRect.top = 0
                outRect.bottom = dp12 / 2
                when (position) {
                    albumAdapter.data.lastIndex -> {
                        outRect.bottom = ScreenUtils.dip2px(context, 88f) +
                                ImmersionBar.getNavigationBarHeight(context)
                    }
                    0, 1 -> {
                        outRect.top = bind.rlTitle.measuredHeight
                    }
                }
            }
        })
        albumAdapter.setOnItemClickListener { adapter, view, position ->
            val intent = Intent(context, AlbumDetailsActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("album", albumAdapter.getItem(position))
            intent.putExtras(bundle)

            val ivAlbum = view.findViewById<View>(R.id.iv_album)

            val anim: androidx.core.util.Pair<View, String> =
                androidx.core.util.Pair(ivAlbum, ViewCompat.getTransitionName(ivAlbum)!!)

            val activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), anim)

            ActivityCompat.startActivity(context, intent, activityOptionsCompat.toBundle())
        }
        LiveEventBus.get<List<Album>>(EventKeys.localAlbums).observeSticky(this) {
            albumAdapter.setList(it)
            bind.refreshLayout.finishRefresh()
        }
        bind.refreshLayout.setOnRefreshListener {
            LocalMediaModel.refresh()
        }
    }

    class AlbumAdapter : QuickAdapter<Album, ItemAlbumBinding>() {

        lateinit var imageLayoutParams: LinearLayout.LayoutParams

        override fun onCreateDefViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewBindingHolder<ItemAlbumBinding> {
            if (!this::imageLayoutParams.isInitialized) {
                imageLayoutParams = LinearLayout.LayoutParams(
                    MATCH_PARENT,
                    (ScreenUtils.getScreenWidth(context) - ScreenUtils.dip2px(context, 12f * 3)) / 2
                )
            }
            val holder = super.onCreateDefViewHolder(parent, viewType)
            holder.bind.ivAlbum.layoutParams = imageLayoutParams
            return holder
        }

        override fun convert(holder: ViewBindingHolder<ItemAlbumBinding>, item: Album) {
            holder.bind.tvTitle.text = item.album
            holder.bind.tvArtist.text = item.artist

            ViewCompat.setTransitionName(holder.bind.ivAlbum, "album:" + item.albumId)
            Glide.with(holder.bind.ivAlbum)
                .load(item.albumArt)
                .placeholder(R.mipmap.default_album_art)
                .error(R.mipmap.default_album_art)
                .into(holder.bind.ivAlbum)
        }
    }

}