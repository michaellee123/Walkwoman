package dog.abcd.walkwoman.view.fragment

import android.os.Bundle
import android.view.View
import com.gyf.immersionbar.ktx.immersionBar
import com.gyf.immersionbar.ktx.statusBarHeight
import dog.abcd.walkwoman.base.BaseFragment
import dog.abcd.walkwoman.databinding.FragmentPlaylistsBinding
import dog.abcd.walkwoman.model.LocalMediaModel

class PlaylistsFragment : BaseFragment<FragmentPlaylistsBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        immersionBar {
            bind.rlTitle.setPadding(0, statusBarHeight, 0, 0)
            fitsSystemWindows(false)
            transparentBar()
            statusBarDarkFont(!isDarkMode())
        }
    }

}