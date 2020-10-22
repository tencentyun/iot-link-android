package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.DataHolder
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.entity.ProductProperty
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_simple_big_enum_item.view.*

/**
 * 标准主题：枚举类型大按钮item
 */
class ControlSimpleEnumBigItemHolder : CRecyclerView.CViewHolder<ProductProperty.MappingEntity> {

    constructor(
        context: Context,
        parent: ViewGroup,
        resId: Int,
        holder: ControlSimpleEnumBigHolder
    ) : super(context, parent, resId) {
        this.holder = holder
    }

    private var holder: ControlSimpleEnumBigHolder? = null

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_simple_big_enum_item_name.text = key
            if (holder!!.selectPosition == position) {
                itemView.tv_simple_big_enum_item_name.setTextColor(itemView.resources.getColor(R.color.white))
                itemView.iv_simple_big_enum.setImageResource(R.mipmap.icon_selected)
                itemView.setBackgroundResource(R.drawable.dark_big_enum_checked)
            } else {
                itemView.tv_simple_big_enum_item_name.setTextColor(itemView.resources.getColor(R.color.black_333333))
                itemView.iv_simple_big_enum.setImageResource(R.mipmap.icon_unselected)
                itemView.setBackgroundResource(R.drawable.dark_big_enum_unchecked)
            }
        }
        DataHolder.instance.get<DeviceEntity>("device")?.let {
            if (it.online == 1) {
                itemView.setOnClickListener { recyclerItemView?.doAction(this, it, position) }
            }
        }
    }

}