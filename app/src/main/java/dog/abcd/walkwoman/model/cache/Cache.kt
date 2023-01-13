package dog.abcd.walkwoman.model.cache

import androidx.lifecycle.LiveData
import com.orhanobut.hawk.Hawk

class Cache<T>(private val key: String, private val default: T) :
    LiveData<T>() {

    private var data: T = default
        set(value) {
            field = value
            postValue(value)
        }

    init {
        data = Hawk.get(key, default)
    }

    fun get(): T {
        return data
    }

    fun put(value: T) {
        data = value
        Hawk.put(key, value)
    }

    fun delete() {
        data = default
        Hawk.delete(key)
    }

    fun contains(): Boolean {
        return Hawk.contains(key)
    }
}