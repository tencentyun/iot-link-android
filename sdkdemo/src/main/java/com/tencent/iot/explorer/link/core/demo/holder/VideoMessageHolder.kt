package com.tencent.iot.explorer.link.core.demo.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.demo.adapter.VideoMessageAdapter
import com.tencent.iot.explorer.link.core.demo.entity.VideoMessageEntity
import kotlinx.android.synthetic.main.item_video_message.view.*

class VideoMessageHolder : BaseHolder<VideoMessageEntity> {

    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        data.run {
            itemView.tv_device_name.text = deviceName
            itemView.btn_realtime_monitor.setOnClickListener {
                clickButton(this@VideoMessageHolder, it, position, 0)
            }
            itemView.btn_local_playback.setOnClickListener {
                clickButton(this@VideoMessageHolder, it, position, 1)
            }
            itemView.btn_cloud_save.setOnClickListener {
                clickButton(this@VideoMessageHolder, it, position, 2)
            }
            itemView.btn_select.setOnClickListener {
                clickButton(this@VideoMessageHolder, it, position, 3)
            }
        }
    }

    /**
     * 点击回调
     */
    fun clickButton(holder: BaseHolder<*>, clickView: View, position: Int, tag: Int) {
        (adapter as VideoMessageAdapter).onClickButton(holder, clickView, position, tag)
    }
}