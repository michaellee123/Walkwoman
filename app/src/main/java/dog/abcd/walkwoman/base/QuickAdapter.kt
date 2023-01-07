package dog.abcd.walkwoman.base

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class QuickAdapter<T, K : ViewBinding>(data: MutableList<T>? = null) :
    BaseQuickAdapter<T, ViewBindingHolder<K>>(0, data) {

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): ViewBindingHolder<K> {
        return ViewBindingHolder(createBinding(parent.context))
    }

    private fun createBinding(context: Context): K {
        return getRealViewType()?.let {
            val method = it.getMethod("inflate", LayoutInflater::class.java)
            method.invoke(null, LayoutInflater.from(context)) as K
        }!!
    }

    private fun getRealViewType(): Class<*>? {
        // 获取当前new的对象的泛型的父类类型
        val genericSuperclass: Type = this.javaClass.genericSuperclass!! as ParameterizedType
        val pt = genericSuperclass as ParameterizedType
        var clazz: Class<*>? = null
        for (type in pt.actualTypeArguments) {
            if (type is Class<*> && type.interfaces.contains(ViewBinding::class.java)) {
                clazz = type
            }
        }
        return clazz
    }
}