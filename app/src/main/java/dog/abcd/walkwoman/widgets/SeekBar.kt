package dog.abcd.walkwoman.widgets

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import dog.abcd.walkwoman.R
import dog.abcd.walkwoman.constant.EventKeys
import dog.abcd.walkwoman.databinding.LayoutSeekBarBinding
import dog.abcd.walkwoman.model.bean.Song
import dog.abcd.walkwoman.utils.formatTime
import dog.abcd.walkwoman.utils.playProgress
import dog.abcd.walkwoman.utils.seek

class SeekBar @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {
    val binding: LayoutSeekBarBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_seek_bar, this, true)
        binding = LayoutSeekBarBinding.bind(this)
    }

    fun handleProgress() {
        setWillNotDraw(false)
        LiveEventBus.get<Song>(EventKeys.currentSong).observeSticky(context as LifecycleOwner) {
            binding.seekbar.max = it.duration.toInt()
            binding.tvTotal.text = it.duration.toInt().formatTime()
        }
        binding.seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    seek(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        binding.seekbar.progress = playProgress
        binding.tvNow.text = playProgress.formatTime()
        postInvalidate()
    }
}