package com.tencent.iot.explorer.link.demo.video.fragment

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.fragment.BaseFragment
import com.tencent.iot.explorer.link.demo.video.OnOrientationChangedListener
import com.tencent.iot.explorer.link.demo.common.customView.CalendarView
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

open class VideoPlaybackBaseFragment: BaseFragment(), CoroutineScope by MainScope() {
    var dateFormat = SimpleDateFormat(CalendarView.SECOND_DATE_FORMAT_PATTERN, Locale.getDefault())
    @Volatile
    var portrait = true
    var onOrientationChangedListener: OnOrientationChangedListener? = null
    private var job : Job? = null

    override fun getContentView(): Int {
        return R.layout.fragment_video_cloud_playback
    }

    override fun startHere(view: View) {
        playback_control_orientation.setOnClickListener {
            portrait = !portrait
            onOrientationChangedListener?.onOrientationChanged(portrait)
            var moreSpace = 12
            var marginWidth = 0
            if (portrait) {
                layout_control.visibility = View.VISIBLE
                v_space.visibility = View.VISIBLE
            } else {
                layout_control.visibility = View.GONE
                v_space.visibility = View.GONE
                moreSpace = 32
                marginWidth = 73
            }

            var btnLayoutParams = playback_control_orientation.layoutParams as ConstraintLayout.LayoutParams
            btnLayoutParams.bottomMargin = dp2px(moreSpace)
            playback_control_orientation.layoutParams = btnLayoutParams

            var videoLayoutParams = palayback_video.layoutParams as ConstraintLayout.LayoutParams
            videoLayoutParams.marginStart = dp2px(marginWidth)
            videoLayoutParams.marginEnd = dp2px(marginWidth)
            palayback_video.layoutParams = videoLayoutParams
        }

        layout_video.setOnClickListener {
            if (playback_control.visibility == View.VISIBLE) {
                playback_control.visibility = View.GONE
                job?.let {
                    it.cancel()
                }
            } else {
                playback_control.visibility = View.VISIBLE
                job = launch {
                    delay(5 * 1000)
                    playback_control.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}