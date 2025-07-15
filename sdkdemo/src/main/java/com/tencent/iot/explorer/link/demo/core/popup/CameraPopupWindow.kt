package com.tencent.iot.explorer.link.demo.core.popup

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.util.ImageSelect
import com.tencent.iot.explorer.link.demo.databinding.PopupSelectCameraBinding

/**
 * 选择相机或相册弹框
 */
class CameraPopupWindow(activity: Activity) : ParentPopupWindow<PopupSelectCameraBinding>(activity) {


    private var count = 1

    override fun getViewBinding(): PopupSelectCameraBinding = PopupSelectCameraBinding.inflate(mInflater)

    override fun getAnimation(): Int {
        return R.style.PopupWindowCamera
    }

    override fun initView() {
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT

        with(binding) {
            popupTakePhoto.setOnClickListener {
                ImageSelect.showCamera(mActivity)
                dismiss()
            }

            popupSelectLocalAlbum.setOnClickListener {
                ImageSelect.showSingle(mActivity)
                dismiss()
            }

            tvPopupSelectCancel.setOnClickListener { dismiss() }
        }
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