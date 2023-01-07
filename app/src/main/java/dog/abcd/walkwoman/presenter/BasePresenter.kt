package dog.abcd.walkwoman.presenter

import dog.abcd.fastmvp.FastPresenter
import dog.abcd.walkwoman.base.IBaseView
import io.reactivex.disposables.Disposable

abstract class BasePresenter<T : IBaseView> : FastPresenter<T>() {

    fun Disposable.add() {
        addDisposable(this)
    }
}