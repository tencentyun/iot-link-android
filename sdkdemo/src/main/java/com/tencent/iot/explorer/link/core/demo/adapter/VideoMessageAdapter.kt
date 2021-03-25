package com.tencent.iot.explorer.link.core.demo.adapter

import android.content.Context
import android.util.SparseBooleanArray
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.entity.VideoMessageEntity
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder
import com.tencent.iot.explorer.link.core.demo.holder.VideoMessageHolder
import com.tencent.iot.explorer.link.core.demo.log.L

class VideoMessageAdapter(context: Context, list: List<VideoMessageEntity>) : BaseAdapter(context, list) {

    private var mButtonInterface: ButtonInterface? = null

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return VideoMessageHolder(mContext, parent, R.layout.item_video_message)
    }

    /**
     * 按钮点击事件需要的方法
     */
    fun buttonSetOnclick(buttonInterface: ButtonInterface) {
        mButtonInterface = buttonInterface
    }

    /**
     * 调用点击
     */
    fun onClickButton(holder: BaseHolder<*>, clickView: View, position: Int, tag: Int) {
        when (tag) {
            0 -> {
                mButtonInterface?.onRealtimeMonitorButtonClick(holder, clickView, position)
            }
            1 -> {
                mButtonInterface?.onLocalPlaybackButtonClick(holder, clickView, position)
            }
            2 -> {
                mButtonInterface?.onCloudSaveButtonClick(holder, clickView, position)
            }
            3 -> {
                mButtonInterface?.onSelectButtonClick(holder, clickView, position)
            }
        }
    }

}

interface ButtonInterface {
    fun onRealtimeMonitorButtonClick(holder: BaseHolder<*>, clickView: View, position: Int)
    fun onLocalPlaybackButtonClick(holder: BaseHolder<*>, clickView: View, position: Int)
    fun onCloudSaveButtonClick(holder: BaseHolder<*>, clickView: View, position: Int)
    fun onSelectButtonClick(holder: BaseHolder<*>, clickView: View, position: Int)
}