package dog.abcd.walkwoman.base

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import dog.abcd.fastmvp.IFastView
import dog.abcd.nicedialog.NiceDialogFragment

interface IBaseView : IFastView {
    fun getContext(): Context
    fun getLifecycleOwner(): LifecycleOwner
    fun getBundle(): Bundle?
    fun toast(
        message: String,
        time: Int = Toast.LENGTH_SHORT
    )

    fun hideProgress()
}