package com.tencent.iot.explorer.link.demo.core.popup

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.tencent.iot.explorer.link.demo.R
import kotlinx.android.synthetic.main.popup_number.view.*

/**
 * 设置进度弹框
 */
class NumberPopupWindow(activity: Activity) : ParentPopupWindow(activity) {

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
        contentView.sp_popup_seek_progress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                progress = seekBar.progress
                contentView.tv_number_value.text = "$progress$unit"
            }
        })
        contentView.tv_number_commit.setOnClickListener { onUploadListener?.upload(progress) }
        contentView.tv_number_delete.setOnClickListener { onDeleteListener?.onDelete() }
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        contentView.sp_popup_seek_progress.progress = progress
        contentView.tv_number_value.text = "$progress$unit"
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