package com.tencent.iot.explorer.link.kitlink.popup

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.holder.EnumViewHolder
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.popup_enum.view.*

/**
 * 枚举、布尔型弹框
 */
class EnumPopupWindow(context: Context) : ParentPopupWindow(context),
    CRecyclerView.RecyclerItemView {

    var onUploadListener: OnUploadListener? = null
    var onDeleteListener: OnDeleteListener? = null
    private var data: JSONObject? = null

    private val list = arrayListOf<String>()
    var selectKey = ""

    override fun getLayoutId(): Int {
        return R.layout.popup_enum
    }

    override fun getAnimation(): Int {
        return R.style.PopupWindowCamera
    }

    override fun initView() {
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        contentView.run {
            crv_popup_enum.addRecyclerItemView(this@EnumPopupWindow)
            tv_popup_enum_delete.setOnClickListener { onDeleteListener?.onDelete() }
            tv_enum_commit.setOnClickListener { onUploadListener?.upload(selectKey) }
        }
    }

    fun showTitle(title: String) {
        contentView.tv_popup_enum_title.text = title
    }

    fun getShowText(key: String): String {
        data?.run {
            forEach {
                if (it.key == key) {
                    return it.value.toString()
                }
            }
        }
        return ""
    }

    fun setList(obj: JSONObject?) {
        list.clear()
        data = obj
        data?.let {
            list.addAll(it.keys)
        }
        contentView.crv_popup_enum.setList(list)
        contentView.crv_popup_enum.notifyDataChanged()
    }

    /**
     *  显示删除按钮
     */
    fun showDeleteButton(isShow: Boolean) {
        contentView.tv_popup_enum_delete.visibility = if (isShow) View.VISIBLE else View.INVISIBLE
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return EnumViewHolder(
            this,
            LayoutInflater.from(this.context)
                .inflate(R.layout.item_enum, parent, false)
        )
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        (viewHolder as? EnumViewHolder)?.run {
            data?.let {
                if (list[position] != selectKey) {
                    selectKey = list[position]
                    contentView.crv_popup_enum.notifyDataChanged()
                }
            }
        }
    }

    override fun show(parentView: View) {
        super.show(parentView)
        this.showAtLocation(parentView, Gravity.BOTTOM, 0, 0)
    }

    interface OnUploadListener {
        fun upload(value: String)
    }

    interface OnDeleteListener {
        fun onDelete()
    }
}