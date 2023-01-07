package dog.abcd.walkwoman.base

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import dog.abcd.fastmvp.FastPresenter
import dog.abcd.fastmvp.IFastMVP

abstract class BaseMvpFragment<K : IBaseView, T : FastPresenter<K>, V : ViewBinding> :
    BaseFragment<V>(),
    IFastMVP<K, T> {
    override lateinit var presenter: T

    override fun lifecycle() = lifecycle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindPresenter()
    }

}