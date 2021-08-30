package com.tencent.iot.explorer.link.demo.video.playback

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.fragment.BaseFragment
import com.tencent.iot.explorer.link.demo.common.customView.CalendarView
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.layout_control
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.layout_video
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.palayback_video
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.playback_control
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.playback_control_orientation
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.v_space
import kotlinx.android.synthetic.main.fragment_video_local_playback.*
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
        playback_control.visibility = View.GONE
        top_control_layout?.visibility = View.GONE
        playback_control_orientation.setOnClickListener {
            portrait = !portrait
            onOrientationChangedListener?.onOrientationChanged(portrait)
            var moreSpace = 12
            var marginWidth = 0
            var topMoreSpace = 12
            if (portrait) {
                layout_control.visibility = View.VISIBLE
                v_space.visibility = View.VISIBLE
            } else {
                layout_control.visibility = View.GONE
                v_space.visibility = View.GONE
                moreSpace = 32
                marginWidth = 73
                topMoreSpace = 32
            }

            var btnLayoutParams = playback_control_orientation.layoutParams as ConstraintLayout.LayoutParams
            btnLayoutParams.bottomMargin = dp2px(moreSpace)
            playback_control_orientation.layoutParams = btnLayoutParams

            iv_video_record?.let {
                var spaceParams = iv_video_record.layoutParams as ConstraintLayout.LayoutParams
                spaceParams.topMargin = dp2px(topMoreSpace)
                iv_video_record.layoutParams = spaceParams
            }

            videoViewNeeResize(dp2px(marginWidth), dp2px(marginWidth))

            if (!portrait) {
                iv_video_back?.visibility = View.VISIBLE
                tv_video_title?.visibility = View.VISIBLE
            } else {
                iv_video_back?.visibility = View.GONE
                tv_video_title?.visibility = View.GONE
            }
        }

        layout_video.setOnClickListener {
            if (playback_control.visibility == View.VISIBLE) {
                playback_control.visibility = View.GONE
                top_control_layout?.visibility = View.GONE
                job?.let {
                    it.cancel()
                }
            } else {
                playback_control.visibility = View.VISIBLE
                top_control_layout?.visibility = View.GONE  // 暂时屏蔽录像/拍照/倍速的功能
                if (!portrait) {
                    iv_video_back?.visibility = View.VISIBLE
                    tv_video_title?.visibility = View.VISIBLE
                } else {
                    iv_video_back?.visibility = View.GONE
                    tv_video_title?.visibility = View.GONE
                }
                job = launch {
                    delay(5 * 1000)
                    playback_control.visibility = View.GONE
                    top_control_layout?.visibility = View.GONE
                }
            }
        }
    }

    open fun videoViewNeeResize(marginStart: Int, marginEnd: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}