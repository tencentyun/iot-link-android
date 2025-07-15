package com.tencent.iot.explorer.link.demo.core.popup

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.databinding.PopupNumberBinding

/**
 * 设置进度弹框
 */
class NumberPopupWindow(activity: Activity) : ParentPopupWindow<PopupNumberBinding>(activity) {

    var onUploadListener: OnUploadListener? = null
    var onDeleteListener: OnDeleteListener? = null
    private var progress = 0
    private var unit = "%"

    override fun getViewBinding(): PopupNumberBinding = PopupNumberBinding.inflate(mInflater)

    override fun getAnimation(): Int {
        return R.style.PopupWindowCamera
    }

    override fun initView() {
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        this.height = dp2px(226)

        with(binding) {
            spPopupSeekProgress.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    progress = seekBar.progress
                    tvNumberValue.text = "$progress$unit"
                }
            })
            tvNumberCommit.setOnClickListener { onUploadListener?.upload(progress) }
            tvNumberDelete.setOnClickListener { onDeleteListener?.onDelete() }
        }
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        binding.spPopupSeekProgress.progress = progress
        binding.tvNumberValue.text = "$progress$unit"
    }

    fun setBackground(drawable: Drawable) {
        binding.numberPopup.background = drawable
    }

    fun showTitle(title: String) {
        binding.tvNumberTitle.text = title
    }

    /**
     *  显示删除按钮
     */
    fun showDeleteButton(isShow: Boolean) {
        binding.tvNumberDelete.visibility = if (isShow) View.VISIBLE else View.INVISIBLE
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