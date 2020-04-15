package com.kitlink.popup

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.kitlink.R
import kotlinx.android.synthetic.main.popup_offline.view.*

/**
 *  离线提示框
 */
class OfflinePopupWindow : ParentPopupWindow {

    var onToHomeListener: OnToHomeListener? = null

    constructor(context: Context) : super(context)

    override fun getLayoutId(): Int {
        return R.layout.popup_offline
    }

    override fun getAnimation(): Int {
        return R.style.PopupWindowCommon
    }

    override fun show(parentView: View) {
        super.show(parentView)
        this.showAtLocation(parentView, Gravity.CENTER, 0, 0)
    }

    override fun initView() {
        this.width = context.resources.displayMetrics.widthPixels - 2 * dp2px(30)
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        contentView.let {
            it.iv_offline_close.setOnClickListener { dismiss() }
            it.tv_offline_popup_to_home.setOnClickListener {
                onToHomeListener?.toHome(this)
            }
            it.tv_offline_popup_to_feedback.setOnClickListener {
                onToHomeListener?.toFeedback(this)
            }
        }
    }

    interface OnToHomeListener {
        fun toFeedback(popupWindow: OfflinePopupWindow)
        fun toHome(popupWindow: OfflinePopupWindow)
    }
}