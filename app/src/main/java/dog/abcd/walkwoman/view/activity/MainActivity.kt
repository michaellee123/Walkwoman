package dog.abcd.walkwoman.view.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.gyf.immersionbar.ktx.immersionBar
import com.gyf.immersionbar.ktx.navigationBarHeight
import com.jeremyliao.liveeventbus.LiveEventBus
import dog.abcd.walkwoman.R
import dog.abcd.walkwoman.base.BaseActivity
import dog.abcd.walkwoman.constant.EventKeys
import dog.abcd.walkwoman.databinding.ActivityMainBinding
import dog.abcd.walkwoman.model.LocalMediaModel
import dog.abcd.walkwoman.model.bean.Song
import dog.abcd.walkwoman.utils.*
import dog.abcd.walkwoman.view.fragment.AlbumsFragment
import dog.abcd.walkwoman.view.fragment.PlaylistsFragment
import dog.abcd.walkwoman.view.fragment.SongsFragment

class MainActivity : BaseActivity<ActivityMainBinding>() {

    val albumsFragment = AlbumsFragment()
    val playlistsFragment = PlaylistsFragment()
    val songsFragment = SongsFragment()

    val fragments = listOf<Fragment>(playlistsFragment, albumsFragment, songsFragment)

    lateinit var fragmentsAdapter: FragmentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        immersionBar {
            statusBarDarkFont(false)
            fitsSystemWindows(false)
            transparentBar()
            bind.flGap.setPadding(0, 0, 0, navigationBarHeight)
        }
        fragmentsAdapter = FragmentsAdapter(this, fragments)
        bind.viewPager.adapter = fragmentsAdapter
        bind.viewPager.setCurrentItem(1, false)
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
        LiveEventBus.get<Song?>(EventKeys.currentSong).observeSticky(this) {
            if (it != null) {
                bind.tvTitle.text = it.title
                bind.tvArtist.text = it.artist
                Glide.with(bind.ivCurrentSong)
                    .load(it.albumArt)
                    .error(R.mipmap.default_audio_art)
                    .into(bind.ivCurrentSong)
            } else {
                bind.tvTitle.text = getText(R.string.app_name)
                bind.tvArtist.text = getText(R.string.author)
                bind.ivCurrentSong.setImageResource(R.mipmap.default_audio_art)
            }
        }
        LiveEventBus.get<Boolean>(EventKeys.playing).observeSticky(this) {
            bind.ibPlay.setImageResource(if (it) R.drawable.ic_pause else R.drawable.ic_play_arrow)
        }
        bind.progressCircular.handleProgress()
        LocalMediaModel.refresh()
    }

    class FragmentsAdapter(activity: FragmentActivity, val list: List<Fragment>) :
        FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return list[position]
        }
    }
}