package com.tencent.iot.explorer.link.demo.video.playback

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewbinding.ViewBinding
import com.tencent.iot.explorer.link.demo.core.fragment.BaseFragment
import com.tencent.iot.explorer.link.demo.common.customView.CalendarView
import com.tencent.iot.explorer.link.demo.databinding.FragmentVideoCloudPlaybackBinding
import com.tencent.iot.explorer.link.demo.databinding.FragmentVideoLocalPlaybackBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

abstract class VideoPlaybackBaseFragment<VB : ViewBinding> : BaseFragment<VB>(),
    CoroutineScope by MainScope() {
    var dateFormat = SimpleDateFormat(CalendarView.SECOND_DATE_FORMAT_PATTERN, Locale.getDefault())

    @Volatile
    var portrait = true
    var onOrientationChangedListener: OnOrientationChangedListener? = null
    private var job: Job? = null

    override fun startHere(view: View) {
//        playbackControl.visibility = View.GONE
//        top_control_layout?.visibility = View.GONE
//        playbackControlOrientation.setOnClickListener {
//            portrait = !portrait
//            onOrientationChangedListener?.onOrientationChanged(portrait)
//            var moreSpace = 12
//            var marginWidth = 0
//            var topMoreSpace = 12
//            if (portrait) {
//                layoutControl.visibility = View.VISIBLE
//                vSpace.visibility = View.VISIBLE
//            } else {
//                layoutControl.visibility = View.GONE
//                vSpace.visibility = View.GONE
//                moreSpace = 32
//                marginWidth = 73
//                topMoreSpace = 32
//            }
//
//            var btnLayoutParams =
//                playbackControlOrientation.layoutParams as ConstraintLayout.LayoutParams
//            btnLayoutParams.bottomMargin = dp2px(moreSpace)
//            playbackControlOrientation.layoutParams = btnLayoutParams
//
//            iv_video_record?.let {
//                var spaceParams = iv_video_record.layoutParams as ConstraintLayout.LayoutParams
//                spaceParams.topMargin = dp2px(topMoreSpace)
//                iv_video_record.layoutParams = spaceParams
//            }
//
//            videoViewNeeResize(dp2px(marginWidth), dp2px(marginWidth))
//
//            if (!portrait) {
//                iv_video_back?.visibility = View.VISIBLE
//                tv_video_title?.visibility = View.VISIBLE
//            } else {
//                iv_video_back?.visibility = View.GONE
//                tv_video_title?.visibility = View.GONE
//            }
//        }
//
//        layoutVideo.setOnClickListener {
//            if (playbackControl.visibility == View.VISIBLE) {
//                playbackControl.visibility = View.GONE
//                top_control_layout?.visibility = View.GONE
//                job?.let {
//                    it.cancel()
//                }
//            } else {
//                playbackControl.visibility = View.VISIBLE
//                top_control_layout?.visibility = View.GONE  // 暂时屏蔽录像/拍照/倍速的功能
//                if (!portrait) {
//                    iv_video_back?.visibility = View.VISIBLE
//                    tv_video_title?.visibility = View.VISIBLE
//                } else {
//                    iv_video_back?.visibility = View.GONE
//                    tv_video_title?.visibility = View.GONE
//                }
//                job = launch {
//                    delay(5 * 1000)
//                    playbackControl.visibility = View.GONE
//                    top_control_layout?.visibility = View.GONE
//                }
//            }
//        }
    }

    protected fun initVideoPlaybackView(vBinding: ViewBinding) {
        when(vBinding) {
            is FragmentVideoLocalPlaybackBinding -> initVideoLocalPlaybackView(vBinding)
            is FragmentVideoCloudPlaybackBinding -> initVideoCloudPlaybackView(vBinding)
        }
    }

    private fun initVideoLocalPlaybackView(vBinding: FragmentVideoLocalPlaybackBinding) {
        with(vBinding) {
            playbackControl.visibility = View.GONE
            topControlLayout.visibility = View.GONE

            playbackControlOrientation.setOnClickListener {
                portrait = !portrait
                onOrientationChangedListener?.onOrientationChanged(portrait)
                var moreSpace = 12
                var marginWidth = 0
                var topMoreSpace = 12
                if (portrait) {
                    layoutControl.visibility = View.VISIBLE
                    vSpace.visibility = View.VISIBLE
                } else {
                    layoutControl.visibility = View.GONE
                    vSpace.visibility = View.GONE
                    moreSpace = 32
                    marginWidth = 73
                    topMoreSpace = 32
                }

                var btnLayoutParams =
                    playbackControlOrientation.layoutParams as ConstraintLayout.LayoutParams
                btnLayoutParams.bottomMargin = dp2px(moreSpace)
                playbackControlOrientation.layoutParams = btnLayoutParams

                var spaceParams = ivVideoRecord.layoutParams as ConstraintLayout.LayoutParams
                spaceParams.topMargin = dp2px(topMoreSpace)
                ivVideoRecord.layoutParams = spaceParams

                videoViewNeeResize(dp2px(marginWidth), dp2px(marginWidth))

                if (!portrait) {
                    ivVideoBack.visibility = View.VISIBLE
                    tvVideoTitle.visibility = View.VISIBLE
                } else {
                    ivVideoBack.visibility = View.GONE
                    tvVideoTitle.visibility = View.GONE
                }
            }

            layoutVideo.setOnClickListener {
                if (playbackControl.visibility == View.VISIBLE) {
                    playbackControl.visibility = View.GONE
                    topControlLayout.visibility = View.GONE
                    job?.cancel()
                } else {
                    playbackControl.visibility = View.VISIBLE
                    topControlLayout.visibility = View.GONE  // 暂时屏蔽录像/拍照/倍速的功能
                    if (!portrait) {
                        ivVideoBack.visibility = View.VISIBLE
                        tvVideoTitle.visibility = View.VISIBLE
                    } else {
                        ivVideoBack.visibility = View.GONE
                        tvVideoTitle.visibility = View.GONE
                    }

                    job = launch {
                        delay(5 * 1000)
                        playbackControl.visibility = View.GONE
                        topControlLayout.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun initVideoCloudPlaybackView(vBinding: FragmentVideoCloudPlaybackBinding) {
        with(vBinding) {
            playbackControl.visibility = View.GONE

            playbackControlOrientation.setOnClickListener {
                portrait = !portrait
                onOrientationChangedListener?.onOrientationChanged(portrait)
                var moreSpace = 12
                var marginWidth = 0
                var topMoreSpace = 12
                if (portrait) {
                    layoutControl.visibility = View.VISIBLE
                    vSpace.visibility = View.VISIBLE
                } else {
                    layoutControl.visibility = View.GONE
                    vSpace.visibility = View.GONE
                    moreSpace = 32
                    marginWidth = 73
                    topMoreSpace = 32
                }

                var btnLayoutParams =
                    playbackControlOrientation.layoutParams as ConstraintLayout.LayoutParams
                btnLayoutParams.bottomMargin = dp2px(moreSpace)
                playbackControlOrientation.layoutParams = btnLayoutParams

                videoViewNeeResize(dp2px(marginWidth), dp2px(marginWidth))
            }

            layoutVideo.setOnClickListener {
                if (playbackControl.visibility == View.VISIBLE) {
                    playbackControl.visibility = View.GONE
                    job?.cancel()
                } else {
                    playbackControl.visibility = View.VISIBLE

                    job = launch {
                        delay(5 * 1000)
                        playbackControl.visibility = View.GONE
                    }
                }
            }
        }
    }

    open fun videoViewNeeResize(marginStart: Int, marginEnd: Int) {}

    abstract fun getLayoutVideo(): ConstraintLayout

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}