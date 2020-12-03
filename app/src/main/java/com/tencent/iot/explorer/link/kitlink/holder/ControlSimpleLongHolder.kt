package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_simple_long.view.*

/**
 * 暗黑主题长按钮：布尔类型之外
 */
class ControlSimpleLongHolder : CRecyclerView.CViewHolder<DevicePropertyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_simple_long_name.text = name
            itemView.tv_simple_long_value.text = getValueText()
            //特殊处理，当设备为trtc设备时。虽然call_status是枚举类型，但产品要求只显示语音呼叫和视频呼叫，隐藏值和箭头，点击即拨打语音或视频通话。
            if (id == MessageConst.TRTC_AUDIO_CALL_STATUS || id == MessageConst.TRTC_VIDEO_CALL_STATUS) {
                itemView.tv_simple_long_value.visibility = View.GONE
                itemView.iv_simple_long_arrow.visibility = View.GONE
                var text = name
                if (text.length > 2) {
                    text = text.substring(0, text.length - 2)
                    itemView.tv_simple_long_name.text = text
                }
            }
            when (id) {
                "color" -> itemView.iv_simple_long.setImageResource(R.mipmap.icon_control_color)
                "power_switch" -> itemView.iv_simple_long.setImageResource(R.mipmap.icon_control_switch)
                else -> itemView.iv_simple_long.setImageResource(R.mipmap.icon_control_brightness)
            }
        }
        itemView.setOnClickListener { recyclerItemView?.doAction(this, it, position) }
    }


}