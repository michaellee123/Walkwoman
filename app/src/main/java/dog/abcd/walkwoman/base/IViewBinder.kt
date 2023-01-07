package dog.abcd.walkwoman.base

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

interface IViewBinder<V : ViewBinding> {

    var bind: V

    fun createViewBinding(layoutInflater: LayoutInflater): V? {
        return getRealViewType()?.let {
            val method = it.getMethod("inflate", LayoutInflater::class.java)
            method.invoke(null, layoutInflater) as V
        } ?: kotlin.run {
            null
        }
    }

    private fun getRealViewType(): Class<*>? {
        // 获取当前new的对象的泛型的父类类型
        val genericSuperclass: Type = this.javaClass.genericSuperclass!! as ParameterizedType
        val pt = genericSuperclass as ParameterizedType
        var clazz: Class<*>? = null
        for (type in pt.actualTypeArguments) {
            val cls = type as Class<*>
            if (cls.interfaces.contains(ViewBinding::class.java)) {
                clazz = cls
            }
        }
        return clazz
    }
}