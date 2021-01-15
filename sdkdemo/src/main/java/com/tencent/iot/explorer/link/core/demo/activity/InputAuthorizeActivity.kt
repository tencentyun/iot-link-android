package com.tencent.iot.explorer.link.core.demo.activity

import android.util.Log
import android.widget.Toast
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.video.link.consts.VideoConst
import kotlinx.android.synthetic.main.activity_input_authorize.*

class InputAuthorizeActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_input_authorize
    }

    override fun initView() {
        val secretId = SharePreferenceUtil.getString(this, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SECRET_ID)
        val secretKey = SharePreferenceUtil.getString(this, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SECRET_KEY)
        val productId = SharePreferenceUtil.getString(this, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_PRODUCT_ID)
        if (secretId != null) {
            et_secretid.setText(secretId)
        }
        if (secretKey != null) {
            et_secretkey.setText(secretKey)
        }
        if (productId != null) {
            et_productid.setText(productId)
        }
    }

    override fun setListener() {
        btn_1_vedio.setOnClickListener {
            if (checkInput()) {
                jumpActivity(VideoMessageActivity::class.java)
            }
        }

    }

    private fun checkInput(): Boolean {
        val inputSecretId: String = et_secretid.text.toString()
        if (inputSecretId == "") {
            val toast =
                Toast.makeText(applicationContext, "请输入SecretId", Toast.LENGTH_LONG)
            toast.show()
            return false
        }
        val inputSecretKey: String = et_secretkey.text.toString()
        if (inputSecretKey == "") {
            val toast =
                Toast.makeText(applicationContext, "请输入SecretKey", Toast.LENGTH_LONG)
            toast.show()
            return false
        }
        val inputProductId: String = et_productid.text.toString()
        if (inputProductId == "") {
            val toast =
                Toast.makeText(applicationContext, "请输入productId", Toast.LENGTH_LONG)
            toast.show()
            return false
        }

        SharePreferenceUtil.saveString(this, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SECRET_ID, inputSecretId)
        SharePreferenceUtil.saveString(this, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SECRET_KEY, inputSecretKey)
        SharePreferenceUtil.saveString(this, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_PRODUCT_ID, inputProductId)

        return true
    }

}
