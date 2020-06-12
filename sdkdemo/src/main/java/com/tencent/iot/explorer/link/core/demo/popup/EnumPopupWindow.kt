package com.tencent.iot.explorer.link.core.demo.popup

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.core.auth.entity.BoolDefine
import com.tencent.iot.explorer.link.core.auth.entity.EnumDefine
import com.tencent.iot.explorer.link.core.auth.entity.Mapping
import com.tencent.iot.explorer.link.core.auth.entity.ProductDefine
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.adapter.EnumAdapter
import com.tencent.iot.explorer.link.core.demo.adapter.OnItemListener
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder
import com.tencent.iot.explorer.link.core.demo.holder.EnumHolder
import kotlinx.android.synthetic.main.popup_enum.view.*

/**
 * 枚举、布尔型弹框
 */
class EnumPopupWindow(activity: Activity) : ParentPopupWindow(activity) {

    var onUploadListener: OnUploadListener? = null
    var onDeleteListener: OnDeleteListener? = null

    private val list = arrayListOf<Mapping>()
    var selectValue = ""

    private var adapter: EnumAdapter? = null

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
            tv_popup_enum_delete.setOnClickListener { onDeleteListener?.onDelete() }
            tv_enum_commit.setOnClickListener { onUploadListener?.upload(selectValue) }
        }
    }

    fun showTitle(title: String) {
        contentView.tv_popup_enum_title.text = title
    }


    fun setList(define: ProductDefine?) {
        list.clear()
        adapter?.notifyDataSetChanged()
        (define as? EnumDefine)?.run {
            list.addAll(parseList())
        }
        (define as? BoolDefine)?.run {
            list.addAll(parseList())
        }
        if (adapter == null) {
            adapter = EnumAdapter(mActivity, this, list)
            contentView.rv_popup_enum.layoutManager = LinearLayoutManager(mActivity)
            contentView.rv_popup_enum.adapter = adapter
        }
        adapter?.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                (holder as? EnumHolder)?.run {
                    data.let {
                        if (list[position].value != selectValue) {
                            selectValue = list[position].value
                            adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        })
    }

    /**
     *  显示删除按钮
     */
    fun showDeleteButton(isShow: Boolean) {
        contentView.tv_popup_enum_delete.visibility = if (isShow) View.VISIBLE else View.INVISIBLE
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