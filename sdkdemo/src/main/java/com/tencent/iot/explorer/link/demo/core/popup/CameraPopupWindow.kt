package com.tencent.iot.explorer.link.demo.core.popup

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.demo.common.util.ImageSelect
import kotlinx.android.synthetic.main.popup_select_camera.view.*

/**
 * 选择相机或相册弹框
 */
class CameraPopupWindow(activity: Activity) : ParentPopupWindow(activity) {


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
            ImageSelect.showCamera(mActivity)
            dismiss()
        }
        contentView.popup_select_local_album.setOnClickListener {
            ImageSelect.showSingle(mActivity)
            dismiss()
        }
        contentView.tv_popup_select_cancel.setOnClickListener { dismiss() }
    }

    fun show(parentView: View, count: Int) {
        this.count = count
        this.show(parentView)
    }

    override fun show(parentView: View) {
        super.show(parentView)
        this.showAtLocation(parentView, Gravity.BOTTOM, 0, 0)
    }

    private fun loadMedia(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // 以startActivityForResult的方式启动一个activity用来获取返回的结果
        mActivity.startActivityForResult(intent, requestCode)
    }
}