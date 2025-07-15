package com.tencent.iot.explorer.link.demo.core.popup

import android.app.Activity
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.databinding.PopupCommonBinding

class CommonPopupWindow(activity: Activity) : ParentPopupWindow<PopupCommonBinding>(activity) {

    var onKeyListener: OnKeyListener? = null

    override fun getViewBinding(): PopupCommonBinding = PopupCommonBinding.inflate(mInflater)

    override fun getAnimation(): Int {
        return R.style.PopupWindowCommon
    }

    fun setCommonParams(title: String, content: String) {
        binding.let {
            it.tvCommonPopupTitle.text = title
            it.tvCommonPopupContent.text = content
        }
    }

    fun setMenuText(cancel: String, confirm: String) {
        binding.let {
            if (!TextUtils.isEmpty(cancel))
                it.tvCommonPopupCancel.text = cancel
            if (!TextUtils.isEmpty(confirm))
                it.tvCommonPopupConfirm.text = confirm
        }
    }

    override fun show(parentView: View) {
        super.show(parentView)
        this.showAtLocation(parentView, Gravity.CENTER, 0, 0)
    }

    override fun initView() {
        this.width = mActivity.resources.displayMetrics.widthPixels - 2 * dp2px(30)
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        this.binding.tvCommonPopupCancel.setOnClickListener {
            onKeyListener?.let {
                it.cancel(this)
                return@setOnClickListener
            }
            this.dismiss()
        }
        this.binding.tvCommonPopupConfirm.setOnClickListener {
            onKeyListener?.confirm(this)
        }
    }

    interface OnKeyListener {
        fun cancel(popupWindow: CommonPopupWindow)
        fun confirm(popupWindow: CommonPopupWindow)
    }
}