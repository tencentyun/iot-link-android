package com.tencent.iot.explorer.link.core.demo.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.video.link.entity.PlaybackVideoEntity
import kotlinx.android.synthetic.main.item_playback_video.view.*

class VideoHolder : BaseHolder<PlaybackVideoEntity>{
    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)
    override fun show(holder: BaseHolder<*>, position: Int) {
        data.run {
            itemView.tv_video_name.text = file_name
        }
        itemView.setOnClickListener { clickItem(this, itemView, position) }
    }

}