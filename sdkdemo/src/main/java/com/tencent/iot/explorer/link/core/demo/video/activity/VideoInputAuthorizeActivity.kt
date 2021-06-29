package com.tencent.iot.explorer.link.core.demo.video.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.demo.BuildConfig
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import com.tencent.iot.explorer.link.core.demo.video.dialog.HistoryAccessInfoDialog
import com.tencent.iot.explorer.link.core.demo.video.entity.AccessInfo
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.video.link.consts.VideoConst
import kotlinx.android.synthetic.main.activity_video_input_authorize.*
import kotlinx.android.synthetic.main.blue_title_layout.*
import kotlinx.android.synthetic.main.input_item_layout.view.*
import kotlinx.coroutines.*

class VideoInputAuthorizeActivity : BaseActivity() , CoroutineScope by MainScope() {

    override fun getContentView(): Int {
        
        return R.layout.activity_video_input_authorize
    }

    override fun initView() {
        tv_title.setText(R.string.iot_demo_name)
        access_id_layout.tv_tip.setText(R.string.access_id)
        access_token_layout.tv_tip.setText(R.string.access_token)
        product_id_layout.tv_tip.setText(R.string.product_id)
        access_id_layout.ev_content.setHint(R.string.hint_access_id)
        access_token_layout.ev_content.setHint(R.string.hint_access_token)
        product_id_layout.ev_content.setHint(R.string.hint_product_id)
        access_id_layout.ev_content.inputType = InputType.TYPE_CLASS_TEXT
        access_token_layout.iv_more.visibility = View.GONE
        product_id_layout.iv_more.visibility = View.GONE

        launch (Dispatchers.Main) {
            var jsonArrStr = SharePreferenceUtil.getString(this@VideoInputAuthorizeActivity, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_ACCESS_INFOS)
            jsonArrStr?.let {
                var accessInfos = JSONArray.parseArray(jsonArrStr, AccessInfo::class.java)
                accessInfos?.let {
                    if (accessInfos.size > 0) {
                        var accessInfo = accessInfos.get(accessInfos.size - 1)
                        access_id_layout.ev_content.setText(accessInfo.accessId)
                        access_token_layout.ev_content.setText(accessInfo.accessToken)
                        product_id_layout.ev_content.setText(accessInfo.productId)
                        access_id_layout.ev_content.setSelection(accessInfo.accessId.length)
                    }
                }?:let{
                    access_id_layout.ev_content.setText(BuildConfig.TencentIotLinkVideoSDKDemoSecretId)
                    access_token_layout.ev_content.setText(BuildConfig.TencentIotLinkVideoSDKDemoSecretKey)
                    product_id_layout.ev_content.setText(BuildConfig.TencentIotLinkVideoSDKDemoProductId)
                    access_id_layout.ev_content.setSelection(BuildConfig.TencentIotLinkVideoSDKDemoSecretId.length)
                }
            }
        }

    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        btn_login.setOnClickListener(loginClickedListener)
        access_id_layout.setOnClickListener {
            var dlg = HistoryAccessInfoDialog(this@VideoInputAuthorizeActivity)
            dlg.show()
            dlg.setOnDismissListener(onDlgDismissListener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    var loginClickedListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            if (!btn_use_sdk.isChecked) {
                Toast.makeText(
                    this@VideoInputAuthorizeActivity,
                    R.string.please_use_sdk,
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            if (TextUtils.isEmpty(access_id_layout.ev_content.text)) {
                Toast.makeText(
                    this@VideoInputAuthorizeActivity,
                    R.string.hint_access_id,
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            if (TextUtils.isEmpty(access_token_layout.ev_content.text)) {
                Toast.makeText(
                    this@VideoInputAuthorizeActivity,
                    R.string.hint_access_token,
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            if (TextUtils.isEmpty(product_id_layout.ev_content.text)) {
                Toast.makeText(
                    this@VideoInputAuthorizeActivity,
                    R.string.hint_product_id,
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            var accessInfo = AccessInfo()
            accessInfo.accessId = access_id_layout.ev_content.text.toString()
            accessInfo.accessToken = access_token_layout.ev_content.text.toString()
            accessInfo.productId = product_id_layout.ev_content.text.toString()

            launch (Dispatchers.Main) {
                checkAccessInfo(accessInfo)
            }

            var intent = Intent(this@VideoInputAuthorizeActivity, VideoMainActivity::class.java)
            var bundle = Bundle()
            bundle.putString(VideoConst.VIDEO_CONFIG, JSONObject.toJSONString(accessInfo))
            intent.putExtra(VideoConst.VIDEO_CONFIG, bundle)
            startActivity(intent)
        }
    }

    private fun checkAccessInfo(accessInfo: AccessInfo) {

        var jsonArrStr = SharePreferenceUtil.getString(this@VideoInputAuthorizeActivity, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_ACCESS_INFOS)
        var accessInfos: MutableList<AccessInfo> = ArrayList()
        jsonArrStr?.let {
            try {
                accessInfos = JSONArray.parseArray(it, AccessInfo::class.java)
                accessInfos?.let {
                    if(it.contains(accessInfo)) it.remove(accessInfo)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                accessInfos = ArrayList()
            }
        }

        // 保证最后一条是最新使用过的数据
        accessInfos.add(accessInfo)
        SharePreferenceUtil.saveString(this@VideoInputAuthorizeActivity, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_ACCESS_INFOS, JSONArray.toJSONString(accessInfos))
    }

    var onDlgDismissListener = object : HistoryAccessInfoDialog.OnDismisListener {
        override fun onOkClicked(accessInfo: AccessInfo?) {
            accessInfo?.let {
                access_id_layout.ev_content.setText(accessInfo.accessId)
                access_token_layout.ev_content.setText(accessInfo.accessToken)
                product_id_layout.ev_content.setText(accessInfo.productId)
                access_id_layout.ev_content.setSelection(accessInfo.accessId.length);
            }
        }

        override fun onCancelClicked() {}
    }

}
