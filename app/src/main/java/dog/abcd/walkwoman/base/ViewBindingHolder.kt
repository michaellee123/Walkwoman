package dog.abcd.walkwoman.base

import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class ViewBindingHolder<T : ViewBinding>(val bind: T) : BaseViewHolder(bind.root) {
    fun easy(easy: T.() -> Unit) {
        easy(bind)
    }
}