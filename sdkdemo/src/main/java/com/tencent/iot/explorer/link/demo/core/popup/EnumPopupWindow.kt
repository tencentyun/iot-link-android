package com.tencent.iot.explorer.link.demo.core.popup

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.core.auth.entity.BoolDefine
import com.tencent.iot.explorer.link.core.auth.entity.EnumDefine
import com.tencent.iot.explorer.link.core.auth.entity.Mapping
import com.tencent.iot.explorer.link.core.auth.entity.ProductDefine
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.adapter.EnumAdapter
import com.tencent.iot.explorer.link.demo.core.adapter.OnItemListener
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.EnumHolder
import com.tencent.iot.explorer.link.demo.databinding.PopupEnumBinding

/**
 * 枚举、布尔型弹框
 */
class EnumPopupWindow(activity: Activity) : ParentPopupWindow<PopupEnumBinding>(activity) {

    var onUploadListener: OnUploadListener? = null
    var onDeleteListener: OnDeleteListener? = null

    private val list = arrayListOf<Mapping>()
    var selectValue = ""

    private var adapter: EnumAdapter? = null

    override fun getViewBinding(): PopupEnumBinding = PopupEnumBinding.inflate(mInflater)

    override fun getAnimation(): Int {
        return R.style.PopupWindowCamera
    }

    override fun initView() {
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.run {
            tvPopupEnumDelete.setOnClickListener { onDeleteListener?.onDelete() }
            tvEnumCommit.setOnClickListener { onUploadListener?.upload(selectValue) }
        }
    }

    fun showTitle(title: String) {
        binding.tvPopupEnumTitle.text = title
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
            binding.rvPopupEnum.layoutManager = LinearLayoutManager(mActivity)
            binding.rvPopupEnum.adapter = adapter
        }
        adapter?.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*, *>, clickView: View, position: Int) {
                (holder as? EnumHolder)?.run {
                    data.let {
                        if (list[position].value != selectValue) {
                            selectValue = list[position].value
                            adapter.notifyDataSetChanged()
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
        binding.tvPopupEnumDelete.visibility = if (isShow) View.VISIBLE else View.INVISIBLE
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