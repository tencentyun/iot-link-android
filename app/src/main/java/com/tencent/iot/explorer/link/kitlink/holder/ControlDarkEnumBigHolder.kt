package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.shapes.Shape
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.kitlink.entity.PropertyEntity
import com.view.recyclerview.CRecyclerDivider
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_dark_big_enum.view.*
import java.lang.Exception

/**
 * 暗黑主题大按钮：枚举
 */
class ControlDarkEnumBigHolder : CRecyclerView.CViewHolder<DevicePropertyEntity>,
    CRecyclerView.RecyclerItemView {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    private var list: List<PropertyEntity.MappingEntity>? = null
    var selectPosition = 0

    override fun show(position: Int) {
        entity?.run {
            parseInt(getValue())//转化为数字
            itemView.tv_dark_big_enum_name.text = name
            itemView.tv_dark_big_enum_value.text = enumEntity?.getValueText(getValue()) ?: ""
            enumEntity?.run {
                itemView.crv_dark_big_enum.let {
                    if (list == null) {
                        list = parseList()
                        it.layoutManager =
                            LinearLayoutManager(
                                itemView.context,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        it.setList(list!!)
                        it.addRecyclerItemView(this@ControlDarkEnumBigHolder)
                        it.addItemDecoration(getDivider())
                    } else {
                        it.notifyDataChanged()
                    }
                }
            }
        }
    }

    private fun parseInt(value: Any?) {
        selectPosition = try {
            value?.toString()?.toInt() ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private var divider: RecyclerView.ItemDecoration? = null

    /**
     * 分割线
     */
    private fun getDivider(): RecyclerView.ItemDecoration {
        if (divider == null) {
            divider = CRecyclerDivider(0, dp2px(16), dp2px(16))
        }
        return divider!!
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        recyclerItemView?.doAction(this, clickView, position)
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return ControlDarkEnumBigItemHolder(
            itemView.context, parent, R.layout.control_dark_big_enum_item, this
        )
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

    private fun dp2px(dp: Int): Int {
        return (itemView.resources.displayMetrics.density * dp + 0.5).toInt()
    }
}