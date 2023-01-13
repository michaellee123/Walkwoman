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
import dog.abcd.walkwoman.model.cache.AppCache
import dog.abcd.walkwoman.utils.*

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
                bind.tvMore.text = it.bucketDisplayName

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

        LiveEventBus.get<Boolean>(EventKeys.playing).observeSticky(this) {
            bind.ibPlay.setImageResource(if (it) R.drawable.ic_pause else R.drawable.ic_play_arrow)
        }
        bind.ibPlay.setOnClickListener {
            if (isPlaying) {
                pause()
            } else {
                start()
            }
        }
        bind.ibNext.setOnClickListener {
            next()
        }
        bind.ibPrevious.setOnClickListener {
            previous()
        }
        bind.seekbar.handleProgress()

        bind.ibMode.setOnClickListener {
            AppCache.shuffle.put(!AppCache.shuffle.get())
        }

        bind.ibRepeatOne.setOnClickListener {
            AppCache.repeatOne.put(!AppCache.repeatOne.get())
        }

        AppCache.shuffle.observe(this) {
            shuffle(it)
            bind.ibMode.setImageResource(if (it) R.drawable.ic_shuffle else R.drawable.ic_repeat)
        }

        AppCache.repeatOne.observe(this) {
            repeatOne(it)
            bind.ibRepeatOne.alpha = if (it) 1f else 0.3f
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startPostponedEnterTransition()
        }, 20)

    }

}