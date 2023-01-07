package dog.abcd.walkwoman.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import dog.abcd.fastmvp.FastPresenter
import dog.abcd.fastmvp.IFastMVP

abstract class BaseMvpActivity<K : IBaseView, T : FastPresenter<K>, V : ViewBinding> :
    BaseActivity<V>(), IFastMVP<K, T> {
    override lateinit var presenter: T

    override fun lifecycle() = lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindPresenter()
    }
}