package com.tenext.demo.popup

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import com.tenext.demo.R
import kotlinx.android.synthetic.main.popup_edit.view.*

/**
 * 文字编辑弹框
 */
class EditPopupWindow(activity: Activity) : ParentPopupWindow(activity) {

    var onVerifyListener: OnVerifyListener? = null

    override fun getLayoutId(): Int {
        return R.layout.popup_edit
    }

    override fun getAnimation(): Int {
        return R.style.PopupWindowCommon
    }

    override fun initView() {
        this.isFocusable = true
        this.width = mActivity.resources.displayMetrics.widthPixels - 2 * dp2px(30)
        this.height = dp2px(236)
        this.contentView.tv_popup_edit_cancel.setOnClickListener { this.dismiss() }
        this.contentView.tv_popup_edit_finish.setOnClickListener {
            onVerifyListener?.onVerify(this.contentView.et_popup_edit.text.trim().toString())
        }
    }


    fun setShowData(title: String, content: String) {
        this.contentView.run {
            tv_popup_edit_title.text = title
            et_popup_edit.setText(content)
            et_popup_edit.setSelection(content.length)
        }
    }

    override fun show(parentView: View) {
        super.show(parentView)
        this.showAtLocation(parentView, Gravity.CENTER, 0, 0)
    }

    interface OnVerifyListener {
        fun onVerify(text: String)
    }
}