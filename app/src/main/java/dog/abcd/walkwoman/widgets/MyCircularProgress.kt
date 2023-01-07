package dog.abcd.walkwoman.widgets

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.jeremyliao.liveeventbus.LiveEventBus
import dog.abcd.walkwoman.R
import dog.abcd.walkwoman.constant.EventKeys
import dog.abcd.walkwoman.model.bean.Song
import dog.abcd.walkwoman.utils.playProgress

class MyCircularProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    val progressIndicator: CircularProgressIndicator

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_circular_progress, this, true)
        progressIndicator = findViewById(R.id.progressBar)
    }

    fun handleProgress() {
        setWillNotDraw(false)
        LiveEventBus.get<Song>(EventKeys.currentSong).observe(context as LifecycleOwner) {
            progressIndicator.max = it.duration.toInt()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        progressIndicator.progress = playProgress
        postInvalidate()
    }
}