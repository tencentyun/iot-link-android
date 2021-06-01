package com.tencent.iot.explorer.link.core.demo.video.activity

import android.content.pm.ActivityInfo
import android.text.TextUtils
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import com.tencent.iot.explorer.link.core.demo.video.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.core.demo.video.entity.DevUrl2Preview
import com.tencent.iot.video.link.consts.VideoConst
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.title_layout.*


class VideoPreviewActivity : BaseActivity() {
    private var orientationV = true

    override fun getContentView(): Int {
        return R.layout.activity_video_preview
    }

    override fun initView() {
        var bundle = intent.getBundleExtra(VideoConst.VIDEO_CONFIG)
        bundle?.let {
            var videoConfig = bundle.getString(VideoConst.VIDEO_CONFIG)
            if (TextUtils.isEmpty(videoConfig)) return@let

            var devInfo = JSON.parseObject(videoConfig, DevUrl2Preview::class.java)
            devInfo?.let {
                tv_title.setText(it.devName)
            }
        }

        tv_video_quality.setText(R.string.video_quality_medium_str)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        iv_orientation.setOnClickListener {
            orientationV = !orientationV
            switchOrientation(orientationV)
        }
        tv_video_quality.setOnClickListener(switchVideoQualityListener)
    }

    private var switchVideoQualityListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            if (orientationV) {
                var options = arrayListOf(
                    getString(R.string.video_quality_high),
                    getString(R.string.video_quality_medium),
                    getString(R.string.video_quality_low)
                )
                var dlg = ListOptionsDialog(this@VideoPreviewActivity, options)
                dlg.show()
                dlg.setOnDismisListener {
                    when(it) {
                        0 -> {
                            tv_video_quality.setText(R.string.video_quality_high_str)
                        }
                        1 -> {
                            tv_video_quality.setText(R.string.video_quality_medium_str)
                        }
                        2 -> {
                            tv_video_quality.setText(R.string.video_quality_low_str)
                        }
                    }
                }
            } else {

            }
        }

    }

    private fun switchOrientation(orientation : Boolean) {
        if (orientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            v_title.visibility = View.VISIBLE
            layout_content.visibility = View.VISIBLE
            var layoutParams = layout_video_preview.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.height = 0
            layoutParams.width = 0
            layout_video_preview.layoutParams = layoutParams

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            v_title.visibility = View.GONE
            layout_content.visibility = View.GONE
            var layoutParams = layout_video_preview.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT
            layoutParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT
            layout_video_preview.layoutParams = layoutParams
        }
    }


}