package com.tencent.iot.explorer.link.kitlink.popup

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.util.L
import com.view.progress.SeekProgress
import kotlinx.android.synthetic.main.popup_number.view.*

/**
 * 设置进度弹框
 */
class NumberPopupWindow(context: Context) : ParentPopupWindow(context) {

    var onUploadListener: OnUploadListener? = null
    var onDeleteListener: OnDeleteListener? = null
    private var progress = 0
    private var unit = "%"

    override fun getLayoutId(): Int {
        return R.layout.popup_number
    }

    override fun getAnimation(): Int {
        return R.style.PopupWindowCamera
    }

    override fun initView() {
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        this.height = dp2px(226)
        contentView.sp_popup_seek_progress.onProgressListener =
            object : SeekProgress.OnProgressListener {
                override fun onProgress(progress: Int, step: Int, keyUp: Boolean) {
                    this@NumberPopupWindow.progress = progress
                    contentView.tv_number_value.text = "$progress$unit"
                }
            }
        contentView.tv_number_commit.setOnClickListener { onUploadListener?.upload(progress) }
        contentView.tv_number_delete.setOnClickListener { onDeleteListener?.onDelete() }
    }

    fun setProgress(progress: Int) {
        L.e("progress=$progress")
        this.progress = progress
        contentView.sp_popup_seek_progress.setProgress(progress)
        contentView.tv_number_value.text = "$progress$unit"
    }

    fun setUnit(unit: String) {
        this.unit = unit
        contentView.sp_popup_seek_progress.setUnit(unit)
    }

    fun setRange(min: Int, max: Int) {
        L.e("min=$min,max=$max")
        contentView.sp_popup_seek_progress.setRange(min, max)
    }

    fun setBackground(drawable: Drawable) {
        contentView.number_popup.background = drawable
    }

    fun showTitle(title: String) {
        contentView.tv_number_title.text = title
    }

    /**
     *  显示删除按钮
     */
    fun showDeleteButton(isShow: Boolean) {
        contentView.tv_number_delete.visibility = if (isShow) View.VISIBLE else View.INVISIBLE
    }

    override fun show(parentView: View) {
        super.show(parentView)
        this.showAtLocation(parentView, Gravity.BOTTOM, 0, 0)
    }

    interface OnUploadListener {
        fun upload(progress: Int)
    }

    interface OnDeleteListener {
        fun onDelete()
    }
}