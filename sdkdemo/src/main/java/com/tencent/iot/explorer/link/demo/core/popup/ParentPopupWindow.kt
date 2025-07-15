package com.tencent.iot.explorer.link.demo.core.popup

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.viewbinding.ViewBinding

abstract class ParentPopupWindow<VB: ViewBinding>(val mActivity: Activity) : PopupWindow(mActivity) {

    init {
        init(mActivity)
    }

    private var bg: View? = null
    protected val binding by lazy { getViewBinding() }
    protected val mInflater: LayoutInflater by lazy { LayoutInflater.from(mActivity) }

    abstract fun getViewBinding(): VB
    abstract fun getAnimation(): Int
    abstract fun initView()
    open fun show(parentView: View) {
        bg?.visibility = View.VISIBLE
    }

    fun setBg(bg: View) {
        this.bg = bg
        bg.setOnClickListener {
            this.dismiss()
        }
    }

    open fun hide() {
        bg?.visibility = View.GONE
    }

    private fun init(context: Context) {
        this.contentView = binding.root
        this.setBackgroundDrawable(ColorDrawable())
        this.isOutsideTouchable = false
        if (getAnimation() > 0) {
            this.animationStyle = getAnimation()
        }
        initView()
        this.setOnDismissListener { hide() }
    }

    fun dp2px(dp: Int): Int {
        return (mActivity.resources.displayMetrics.density * dp + 0.5).toInt()
    }

}