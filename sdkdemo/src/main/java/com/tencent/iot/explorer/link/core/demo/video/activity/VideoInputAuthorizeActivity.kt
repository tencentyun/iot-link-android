package com.tencent.iot.explorer.link.core.demo.video.activity

import android.text.TextUtils
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import com.tencent.iot.explorer.link.core.demo.video.dialog.HistoryAccessInfoDialog
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.video.link.consts.VideoConst
import kotlinx.android.synthetic.main.activity_video_input_authorize.*
import kotlinx.android.synthetic.main.input_item_layout.view.*
import kotlinx.android.synthetic.main.title_layout.*


class VideoInputAuthorizeActivity : BaseActivity() {

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
        access_token_layout.iv_more.visibility = View.GONE
        product_id_layout.iv_more.visibility = View.GONE
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

            checkAccessInfo()
        }
    }

    private fun checkAccessInfo() {
        var jsonEle = JSONObject()
        jsonEle.put(VideoConst.VIDEO_SECRET_ID, access_id_layout.ev_content.text)
        jsonEle.put(VideoConst.VIDEO_SECRET_KEY, access_token_layout.ev_content.text)
        jsonEle.put(VideoConst.VIDEO_PRODUCT_ID, product_id_layout.ev_content.text)

        var jsonArrStr = SharePreferenceUtil.getString(this@VideoInputAuthorizeActivity, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_ACCESS_INFOS)
        var jsonArr = JSONArray()
        jsonArrStr?.let {
            jsonArr = JSONArray.parseArray(jsonArrStr)
            jsonArr?.let {
                if (jsonArr.contains(jsonEle)) return
            }
        }

        jsonArr.add(jsonEle)
        SharePreferenceUtil.saveString(this@VideoInputAuthorizeActivity, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_ACCESS_INFOS, jsonArr.toJSONString())

    }

    var onDlgDismissListener = object : HistoryAccessInfoDialog.OnDismisListener {
        override fun onOkClicked() {}

        override fun onCancelClicked() {}
    }

    fun onRadioButtonClicked(view: View) {
        val button = view as RadioButton
        val isChecked = button.isChecked()
        when (view.id) {
            R.id.radio_contain -> if (isChecked) {

            }
            R.id.radio_not_contain -> if (isChecked) {

            }
        }
    }

}
