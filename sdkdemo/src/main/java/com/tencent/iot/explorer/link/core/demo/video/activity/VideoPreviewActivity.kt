package com.tencent.iot.explorer.link.core.demo.video.activity

import android.content.pm.ActivityInfo
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import com.tencent.iot.explorer.link.core.demo.video.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.core.demo.video.dialog.VideoQualityDialog
import com.tencent.iot.explorer.link.core.demo.video.entity.DevUrl2Preview
import com.tencent.iot.explorer.link.rtc.ui.utils.Utils
import com.tencent.iot.video.link.consts.VideoConst
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.title_layout.*
import java.util.*


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
        today_tip.setText(getString(R.string.today) + " " + getweekDay())
    }

    private fun getweekDay() : String {
        var calendar = Calendar.getInstance()
        calendar.setTimeInMillis(System.currentTimeMillis())
        var day = calendar.get(Calendar.DAY_OF_WEEK)

        var retStr = getString(R.string.week)
        when(day % 7) {
            0 -> {
                retStr += getString(R.string.saturday)
            }
            1 -> {
                retStr += getString(R.string.sunday)
            }
            2 -> {
                retStr += getString(R.string.monday)
            }
            3 -> {
                retStr += getString(R.string.tuesday)
            }
            4 -> {
                retStr += getString(R.string.wednesday)
            }
            5 -> {
                retStr += getString(R.string.thursday)
            }
            6 -> {
                retStr += getString(R.string.friday)
            }
        }
        return retStr
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        iv_orientation.setOnClickListener {
            orientationV = !orientationV
            switchOrientation(orientationV)
        }
        tv_video_quality.setOnClickListener(switchVideoQualityListener)
        radio_talk.setOnCheckedChangeListener { buttonView, isChecked ->
        }
        radio_record.setOnCheckedChangeListener { buttonView, isChecked ->
        }
        radio_playback.setOnCheckedChangeListener { buttonView, isChecked ->
        }
        radio_photo.setOnCheckedChangeListener { buttonView, isChecked ->
        }
        iv_up.setOnClickListener {  }
        iv_down.setOnClickListener {  }
        iv_right.setOnClickListener {  }
        iv_left.setOnClickListener {  }
    }

    private var switchVideoQualityListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            if (orientationV) {
                showVVideoQualityDialog()
            } else {
                showHVideoQualityDialog()
            }
        }
    }

    private fun showVVideoQualityDialog() {
        var options = arrayListOf(
                getString(R.string.video_quality_high_str) + " " + getString(R.string.video_quality_high),
                getString(R.string.video_quality_medium_str) + " " + getString(R.string.video_quality_medium),
                getString(R.string.video_quality_low_str) + " " + getString(R.string.video_quality_low)
        )
        var dlg = ListOptionsDialog(this@VideoPreviewActivity, options)
        dlg.show()
        dlg.setOnDismisListener {
            chgTextState(it)
        }
    }

    private fun showHVideoQualityDialog() {
        var pos = -1
        when(tv_video_quality.text.toString()) {
            getString(R.string.video_quality_high_str) -> pos = 2
            getString(R.string.video_quality_medium_str) -> pos = 1
            getString(R.string.video_quality_low_str) -> pos = 0
        }
        var dlg = VideoQualityDialog(this@VideoPreviewActivity, pos)
        dlg.show()
        btn_layout.visibility = View.GONE
        dlg.setOnDismisListener(object : VideoQualityDialog.OnDismisListener {
            override fun onItemClicked(pos: Int) {
                chgTextState(pos)
            }

            override fun onDismiss() {
                btn_layout.visibility = View.VISIBLE
            }

        })
    }

    private fun chgTextState(value: Int) {
        when(value) {
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

    private fun switchOrientation(orientation : Boolean) {
        var marginWidth = 0
        var layoutParams = layout_video_preview.layoutParams as ConstraintLayout.LayoutParams
        var fitSize = 0
        var visibility = View.VISIBLE
        var moreSpace = 10
        if (orientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            visibility = View.GONE
            fitSize = ConstraintLayout.LayoutParams.MATCH_PARENT
            marginWidth = 73
            moreSpace = 32
        }

        v_title.visibility = visibility
        layout_content.visibility = visibility

        layoutParams.height = fitSize
        layoutParams.width = fitSize
        layout_video_preview.layoutParams = layoutParams

        var videoLayoutParams = v_preview.layoutParams as ConstraintLayout.LayoutParams
        videoLayoutParams.marginStart = dp2px(marginWidth)
        videoLayoutParams.marginEnd = dp2px(marginWidth)
        v_preview.layoutParams = videoLayoutParams

        var btnLayoutParams = btn_layout.layoutParams as ConstraintLayout.LayoutParams
        btnLayoutParams.bottomMargin = dp2px(moreSpace)
        btn_layout.layoutParams = btnLayoutParams
    }


}