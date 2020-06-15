package com.tencent.iot.explorer.link.kitlink.popup

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginStart
import com.tencent.iot.explorer.link.R
import kotlinx.android.synthetic.main.popup_common.view.*

class CommonPopupWindow(context: Context) : ParentPopupWindow(context) {

    var onKeyListener: OnKeyListener? = null

    override fun getLayoutId(): Int {
        return R.layout.popup_common
    }

    override fun getAnimation(): Int {
        return R.style.PopupWindowCommon
    }

    fun setCommonParams(title: String, content: String) {
        contentView.let {
            it.tv_common_popup_title.text = title
            it.tv_common_popup_content.text = content
        }
    }

    fun setMenuText(cancel: String, confirm: String) {
        contentView.let {
            if (!TextUtils.isEmpty(cancel))
                it.tv_common_popup_cancel.text = cancel
            if (!TextUtils.isEmpty(confirm))
                it.tv_common_popup_confirm.text = confirm
        }
    }

    override fun show(parentView: View) {
        super.show(parentView)
        this.showAtLocation(parentView, Gravity.CENTER, 0, 0)
    }

    override fun initView() {
        this.width = context.resources.displayMetrics.widthPixels - 2 * dp2px(30)
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        this.contentView.tv_common_popup_cancel.setOnClickListener {
            onKeyListener?.let {
                it.cancel(this)
                return@setOnClickListener
            }
            this.dismiss()
        }
        this.contentView.tv_common_popup_confirm.setOnClickListener {
            onKeyListener?.let {
                it.confirm(this)
            }
        }
    }

    interface OnKeyListener {
        fun cancel(popupWindow: CommonPopupWindow)
        fun confirm(popupWindow: CommonPopupWindow)
    }
}