package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.kitlink.entity.DeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.kitlink.util.DataHolder
import com.util.L
import com.view.progress.SeekProgress
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_standard_big_int.view.*

/**
 * 控制面板数字类型：大按钮
 */
class ControlStandardNumberBigHolder : CRecyclerView.CViewHolder<DevicePropertyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_standard_big_int_name.text = name
            itemView.tv_standard_big_int_value.text = getValueText()
            val progress = getValue().toDouble().toInt()
            L.e("progress=$progress,min=${numberEntity?.min},max=${numberEntity?.max}")
            itemView.sp_standard_big_int.setProgress(progress)
            itemView.sp_standard_big_int.setRange(
                numberEntity?.min?.toDouble()?.toInt() ?: 0,
                numberEntity?.max?.toDouble()?.toInt() ?: 100
            )
            itemView.sp_standard_big_int.setStepValue(numberEntity?.step?.toInt() ?: 1)
            if (DataHolder.instance.get<DeviceEntity>("device")?.online ?: 0 == 1) {
                itemView.sp_standard_big_int.onProgressListener =
                    object : SeekProgress.OnProgressListener {
                        override fun onProgress(progress: Int, step: Int, keyUp: Boolean) {
                            itemView.tv_standard_big_int_value.text =
                                "$progress${numberEntity?.getNumUnit() ?: ""}"
                            if (keyUp) {
                                recyclerItemView?.doAction(
                                    this@ControlStandardNumberBigHolder,
                                    itemView.sp_standard_big_int, position
                                )
                            }
                        }
                    }
            }
        }
    }

}