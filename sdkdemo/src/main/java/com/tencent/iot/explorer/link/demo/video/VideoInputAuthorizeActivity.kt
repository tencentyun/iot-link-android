package com.tencent.iot.explorer.link.demo.video

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.demo.BuildConfig
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityVideoInputAuthorizeBinding
import com.tencent.iot.video.link.consts.VideoConst
import kotlinx.coroutines.*

class VideoInputAuthorizeActivity : VideoBaseActivity<ActivityVideoInputAuthorizeBinding>(), CoroutineScope by MainScope() {

    override fun getViewBinding(): ActivityVideoInputAuthorizeBinding =
        ActivityVideoInputAuthorizeBinding.inflate(layoutInflater)

    override fun initView() {
        with(binding) {
            vTitle.tvTitle.setText(R.string.iot_demo_name)
            accessIdLayout.tvTip.setText(R.string.access_id)
            accessTokenLayout.tvTip.setText(R.string.access_token)
            productIdLayout.tvTip.setText(R.string.product_id)
            accessIdLayout.evContent.setHint(R.string.hint_access_id)
            accessTokenLayout.evContent.setHint(R.string.hint_access_token)
            productIdLayout.evContent.setHint(R.string.hint_product_id)
            accessIdLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            accessTokenLayout.ivMore.visibility = View.GONE
            productIdLayout.ivMore.visibility = View.GONE

            launch(Dispatchers.Main) {
                val jsonArrStr = SharePreferenceUtil.getString(
                    this@VideoInputAuthorizeActivity,
                    VideoConst.VIDEO_CONFIG,
                    VideoConst.VIDEO_ACCESS_INFOS
                )
                jsonArrStr?.let {
                    val accessInfos = JSONArray.parseArray(jsonArrStr, AccessInfo::class.java)
                    accessInfos?.let {
                        if (accessInfos.size > 0) {
                            val accessInfo = accessInfos.get(accessInfos.size - 1)
                            accessIdLayout.evContent.setText(accessInfo.accessId)
                            accessTokenLayout.evContent.setText(accessInfo.accessToken)
                            productIdLayout.evContent.setText(accessInfo.productId)
                            accessIdLayout.evContent.setSelection(accessInfo.accessId.length)
                        }

                    } ?: let {
                        accessIdLayout.evContent.setText(BuildConfig.TencentIotLinkVideoSDKDemoSecretId)
                        accessTokenLayout.evContent.setText(BuildConfig.TencentIotLinkVideoSDKDemoSecretKey)
                        productIdLayout.evContent.setText(BuildConfig.TencentIotLinkVideoSDKDemoProductId)
                        accessIdLayout.evContent.setSelection(BuildConfig.TencentIotLinkVideoSDKDemoSecretId.length)
                    }
                }
            }
        }
    }

    override fun setListener() {
        with(binding) {
            vTitle.ivBack.setOnClickListener { finish() }
            btnLogin.setOnClickListener(loginClickedListener)
            accessIdLayout.root.setOnClickListener {
                val dlg = HistoryAccessInfoDialog(this@VideoInputAuthorizeActivity)
                dlg.show()
                dlg.setOnDismissListener(onDlgDismissListener)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    private var loginClickedListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            if (!binding.btnUseSdk.isChecked) {
                Toast.makeText(
                    this@VideoInputAuthorizeActivity,
                    R.string.please_use_sdk,
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            if (TextUtils.isEmpty(binding.accessIdLayout.evContent.text)) {
                Toast.makeText(
                    this@VideoInputAuthorizeActivity,
                    R.string.hint_access_id,
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            if (TextUtils.isEmpty(binding.accessTokenLayout.evContent.text)) {
                Toast.makeText(
                    this@VideoInputAuthorizeActivity,
                    R.string.hint_access_token,
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            if (TextUtils.isEmpty(binding.productIdLayout.evContent.text)) {
                Toast.makeText(
                    this@VideoInputAuthorizeActivity,
                    R.string.hint_product_id,
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val accessInfo = AccessInfo()

            with(binding) {
                accessInfo.accessId = accessIdLayout.evContent.text.toString()
                accessInfo.accessToken = accessTokenLayout.evContent.text.toString()
                accessInfo.productId = productIdLayout.evContent.text.toString()
            }

            launch(Dispatchers.Main) {
                checkAccessInfo(accessInfo)
            }

            val intent = Intent(this@VideoInputAuthorizeActivity, VideoMainActivity::class.java)
            val bundle = Bundle()
            bundle.putString(VideoConst.VIDEO_CONFIG, JSONObject.toJSONString(accessInfo))
            intent.putExtra(VideoConst.VIDEO_CONFIG, bundle)
            startActivity(intent)
        }
    }

    private fun checkAccessInfo(accessInfo: AccessInfo) {

        val jsonArrStr = SharePreferenceUtil.getString(
            this@VideoInputAuthorizeActivity,
            VideoConst.VIDEO_CONFIG,
            VideoConst.VIDEO_ACCESS_INFOS
        )
        var accessInfos: MutableList<AccessInfo> = ArrayList()
        jsonArrStr?.let {
            try {
                accessInfos = JSONArray.parseArray(it, AccessInfo::class.java)
                accessInfos.let { info -> if (info.contains(accessInfo)) info.remove(accessInfo) }
            } catch (e: Exception) {
                e.printStackTrace()
                accessInfos = ArrayList()
            }
        }

        // 保证最后一条是最新使用过的数据
        accessInfos.add(accessInfo)
        SharePreferenceUtil.saveString(
            this@VideoInputAuthorizeActivity,
            VideoConst.VIDEO_CONFIG,
            VideoConst.VIDEO_ACCESS_INFOS,
            JSONArray.toJSONString(accessInfos)
        )
    }

    private var onDlgDismissListener = object : HistoryAccessInfoDialog.OnDismisListener {
        override fun onOkClicked(accessInfo: AccessInfo?) {
            accessInfo?.let {
                with(binding) {
                    accessIdLayout.evContent.setText(accessInfo.accessId)
                    accessTokenLayout.evContent.setText(accessInfo.accessToken)
                    productIdLayout.evContent.setText(accessInfo.productId)
                    accessIdLayout.evContent.setSelection(accessInfo.accessId.length);
                }
            }
        }

        override fun onCancelClicked() {}
    }

}
