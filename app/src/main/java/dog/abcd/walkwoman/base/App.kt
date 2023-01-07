package dog.abcd.walkwoman.base

import android.app.Application
import android.content.Intent
import android.graphics.Color
import com.orhanobut.hawk.Hawk
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.scwang.smartrefresh.header.StoreHouseHeader
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.BallPulseFooter
import dog.abcd.walkwoman.R
import dog.abcd.walkwoman.services.PlaybackService
import dog.abcd.walkwoman.utils.PlaybackController

class App : Application() {

    companion object {
        lateinit var instance: App
    }

    var controller: PlaybackController? = null

    override fun onCreate() {
        super.onCreate()
        instance = this

        val intent = Intent(this, PlaybackService::class.java)
        startService(intent)

        SmartRefreshLayout.setDefaultRefreshInitializer { context, layout ->
            layout.setDragRate(0.35f)
            layout.setEnableOverScrollBounce(true)
            layout.setHeaderInsetStart(88f)
            layout.setEnableOverScrollDrag(true)
            layout.setEnableLoadMore(false)
            layout.setEnableRefresh(true)
            layout.setPrimaryColors(getColor(R.color.background), getColor(R.color.text_color))
        }
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            val header = StoreHouseHeader(context)
            header.initWithString("WALKWOMAN")
            header
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            BallPulseFooter(context).setAnimatingColor(Color.parseColor("#5ac2b9"))
        }

        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .tag("WM")
            .build()
        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))

        Hawk.init(applicationContext).build()
    }

}