package dog.abcd.walkwoman

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

var toast: Toast? = null

@SuppressLint("ShowToast")
fun Context.showToast(message: String, time: Int = Toast.LENGTH_SHORT): Toast {
    toast?.cancel()
    toast = Toast.makeText(this, message, time)
    toast?.show()
    return toast!!
}
