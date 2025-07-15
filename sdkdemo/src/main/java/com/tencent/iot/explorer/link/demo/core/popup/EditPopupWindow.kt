package com.tencent.iot.explorer.link.demo.core.popup

import android.app.Activity
import android.view.Gravity
import android.view.View
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.databinding.PopupEditBinding

/**
 * 文字编辑弹框
 */
class EditPopupWindow(activity: Activity) : ParentPopupWindow<PopupEditBinding>(activity) {

    var onVerifyListener: OnVerifyListener? = null

    override fun getViewBinding(): PopupEditBinding = PopupEditBinding.inflate(mInflater)

    override fun getAnimation(): Int {
        return R.style.PopupWindowCommon
    }

    override fun initView() {
        this.isFocusable = true
        this.width = mActivity.resources.displayMetrics.widthPixels - 2 * dp2px(30)
        this.height = dp2px(236)

        with(binding) {
            tvPopupEditCancel.setOnClickListener { this@EditPopupWindow.dismiss() }
            tvPopupEditFinish.setOnClickListener {
                onVerifyListener?.onVerify(etPopupEdit.text.trim().toString())
            }
        }
    }


    fun setShowData(title: String, content: String) {
        binding.run {
            tvPopupEditTitle.text = title
            etPopupEdit.setText(content)
            etPopupEdit.setSelection(content.length)
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