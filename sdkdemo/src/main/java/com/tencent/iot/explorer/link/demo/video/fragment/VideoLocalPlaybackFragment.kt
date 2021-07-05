package com.tencent.iot.explorer.link.demo.video.fragment

import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.*


class VideoLocalPlaybackFragment: VideoPlaybackBaseFragment() {

    override fun startHere(view: View) {
        super.startHere(view)
        tv_date.setText(dateFormat.format(System.currentTimeMillis()))
        action_list_view.visibility = View.GONE
        setListener()
    }

    private fun setListener() {
        playback_control.setOnClickListener {  }
        iv_left_go.setOnClickListener { time_line.last() }
        iv_right_go.setOnClickListener { time_line.next() }
    }

    private var onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {  // 是用户操作的，调整视频到指定的时间点
                palayback_video.seekTo(progress * 1000)
                return
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }
}