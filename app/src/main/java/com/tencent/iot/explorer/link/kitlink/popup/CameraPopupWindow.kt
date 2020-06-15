package com.tencent.iot.explorer.link.kitlink.popup

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.yho.image.imageselectorbrowser.ImageSelectorActivity
import com.yho.image.imp.ImageSelectorUtils
import kotlinx.android.synthetic.main.popup_select_camera.view.*

/**
 * 选择相机或相册弹框
 */
class CameraPopupWindow(context: Context) : ParentPopupWindow(context) {

    private var count = 1

    override fun getLayoutId(): Int {
        return R.layout.popup_select_camera
    }

    override fun getAnimation(): Int {
        return R.style.PopupWindowCamera
    }

    override fun initView() {
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        contentView.popup_take_photo.setOnClickListener {
            ImageSelectorUtils.show(context, ImageSelectorActivity.Mode.MODE_SINGLE, true, 1)
            dismiss()
        }
        contentView.popup_select_local_album.setOnClickListener {
            ImageSelectorUtils.show(context, ImageSelectorActivity.Mode.MODE_MULTI, false, count)
            dismiss()
        }
        contentView.tv_popup_select_cancel.setOnClickListener { dismiss() }
    }

    fun show(parentView: View,count:Int){
        this.count = count
        this.show(parentView)
    }

    override fun show(parentView: View) {
        super.show(parentView)
        this.showAtLocation(parentView, Gravity.BOTTOM, 0, 0)
    }
}