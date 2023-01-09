package dog.abcd.walkwoman.view.activity

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.gyf.immersionbar.ImmersionBar
import com.jeremyliao.liveeventbus.LiveEventBus
import com.luck.picture.lib.tools.ScreenUtils
import dog.abcd.walkwoman.R
import dog.abcd.walkwoman.base.BaseActivity
import dog.abcd.walkwoman.constant.EventKeys
import dog.abcd.walkwoman.databinding.ActivityPlayerBinding
import dog.abcd.walkwoman.model.bean.Song

class PlayerActivity : BaseActivity<ActivityPlayerBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        postponeEnterTransition()
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        bind.root.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        val lp = bind.ivAlbum.layoutParams
        lp.height = ScreenUtils.getScreenWidth(this) - ScreenUtils.dip2px(this, 24f)
        bind.ivAlbum.layoutParams = lp

        LiveEventBus.get<Song?>(EventKeys.currentSong).observeSticky(this) {
            if (it != null) {
                bind.tvTitle.text = it.title
                bind.tvMore.text = it.artist + " - " + it.album

                Glide.with(bind.ivAlbum)
                    .asDrawable()
                    .load(it.albumArt)
                    .error(R.mipmap.default_album_art)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            bind.ivAlbum.setImageDrawable(resource)
                            startPostponedEnterTransition()
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            bind.ivAlbum.setImageDrawable(errorDrawable)
                            startPostponedEnterTransition()
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }

                    })
            } else {
                startPostponedEnterTransition()
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startPostponedEnterTransition()
        }, 20)

    }
}