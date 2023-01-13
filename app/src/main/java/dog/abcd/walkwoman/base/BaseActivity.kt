package dog.abcd.walkwoman.base

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ktx.immersionBar
import dog.abcd.fastmvp.IDisposableHandler
import dog.abcd.nicedialog.NiceDialog
import dog.abcd.walkwoman.showToast
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


abstract class BaseActivity<T : ViewBinding> : AppCompatActivity(), IDisposableHandler,
    IViewBinder<T>, IBaseView {

    override var compositeDisposable: CompositeDisposable? = null

    override lateinit var bind: T

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersionBar {
            fitsSystemWindows(false)
            statusBarDarkFont(!isDarkMode())
        }
        if (forcePortrait()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT//竖屏
        }
        createViewBinding(layoutInflater)?.let {
            bind = it
            setContentView(it.root)
        }
        initView()
    }

    abstract fun initView()

    open fun forcePortrait(): Boolean {
        return true
    }

    override fun getContext(): Context {
        return this
    }

    override fun getLifecycleOwner(): LifecycleOwner {
        return this
    }

    override fun getBundle(): Bundle? {
        return intent.extras
    }

    @SuppressLint("ShowToast")
    override fun toast(message: String, time: Int) {
        showToast(message, time)
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
        return (getSystemService(UI_MODE_SERVICE) as UiModeManager).nightMode == UiModeManager.MODE_NIGHT_YES
    }

}