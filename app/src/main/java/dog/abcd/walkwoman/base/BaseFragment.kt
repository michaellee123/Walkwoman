package dog.abcd.walkwoman.base

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import dog.abcd.fastmvp.IDisposableHandler
import dog.abcd.nicedialog.NiceDialog
import dog.abcd.walkwoman.showToast
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseFragment<T : ViewBinding> : Fragment(), IDisposableHandler, IViewBinder<T>,
    IBaseView {

    override lateinit var bind: T

    override var compositeDisposable: CompositeDisposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return createViewBinding(inflater)?.let {
            bind = it
            it.root
        } ?: kotlin.run {
            null
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun getContext(): Context {
        return super.getContext()!!
    }

    override fun getLifecycleOwner(): LifecycleOwner {
        return this
    }

    override fun getBundle(): Bundle? {
        return arguments
    }

    @SuppressLint("ShowToast")
    override fun toast(message: String, time: Int) {
        requireContext().showToast(message, time)
    }

    override fun hideProgress() {
        NiceDialog.dismiss(javaClass.simpleName + "progress")
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.dispose()
    }

    fun Disposable.add() {
        addDisposable(this)
    }

    fun isDarkMode(): Boolean {
        return (context.getSystemService(AppCompatActivity.UI_MODE_SERVICE) as UiModeManager).nightMode == UiModeManager.MODE_NIGHT_YES
    }
}