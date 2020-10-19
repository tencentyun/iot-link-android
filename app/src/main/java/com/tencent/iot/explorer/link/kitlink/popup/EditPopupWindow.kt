package com.tencent.iot.explorer.link.kitlink.popup

import android.content.Context
import android.view.Gravity
import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import kotlinx.android.synthetic.main.popup_edit.view.*

/**
 * 文字编辑弹框
 */
class EditPopupWindow(context: Context) : ParentPopupWindow(context) {

    var onVerifyListener: OnVerifyListener? = null

    override fun getLayoutId(): Int {
        return R.layout.popup_edit
    }

    override fun getAnimation(): Int {
        return R.style.PopupWindowCommon
    }

    override fun initView() {
        this.isFocusable = true
        this.width = context.resources.displayMetrics.widthPixels - 2 * dp2px(30)
        this.height = dp2px(236)
        this.contentView.tv_popup_edit_cancel.setOnClickListener { this.dismiss() }
        this.contentView.tv_popup_edit_finish.setOnClickListener {
            val text = this.contentView.et_popup_edit.text.trim().toString()
            if (text.length !in 1..20) {
                T.show(context.getString(R.string.toast_alias_length))
            } else {
                onVerifyListener?.onVerify(text)
            }
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