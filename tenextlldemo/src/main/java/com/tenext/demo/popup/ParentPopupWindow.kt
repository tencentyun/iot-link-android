package com.tenext.demo.popup

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow

abstract class ParentPopupWindow : PopupWindow {

    var mActivity: Activity
    private var bg: View? = null

    constructor(activity: Activity) : super(activity) {
        this.mActivity = activity
        init(activity)
    }

    abstract fun getLayoutId(): Int
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
        this.contentView = LayoutInflater.from(context).inflate(getLayoutId(), null)
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