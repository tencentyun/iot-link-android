package com.tencent.iot.explorer.link.demo.video

import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.video.preview.WlanVideoPreviewActivity
import com.tencent.iot.video.link.callback.OnWlanDevicesDetectedCallback
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.entity.DeviceServerInfo
import com.tencent.iot.video.link.entity.WlanDetectBody
import com.tencent.iot.video.link.entity.WlanRespBody
import kotlinx.android.synthetic.main.activity_video_detect_devs.*
import kotlinx.android.synthetic.main.activity_video_detect_devs.product_id_layout
import kotlinx.android.synthetic.main.activity_video_input_authorize.*
import kotlinx.android.synthetic.main.blue_title_layout.*
import kotlinx.android.synthetic.main.fragment_video_device.*
import kotlinx.android.synthetic.main.input_item_layout.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class VideoWlanDetectActivity : VideoBaseActivity() , CoroutineScope by MainScope() {

    var datas: MutableList<DeviceServerInfo> = ArrayList()
    var adapter: WlanDevsAdapter? = null

    override fun getContentView(): Int {
        return R.layout.activity_video_detect_devs
    }

    override fun initView() {

        tv_title.setText(R.string.video_wlan)
        product_id_layout.tv_tip.setText(R.string.product_id)
        product_id_layout.ev_content.setHint(R.string.hint_product_id)
        product_id_layout.ev_content.inputType = InputType.TYPE_CLASS_TEXT
        product_id_layout.iv_more.visibility = View.GONE

        client_token_layout.tv_tip.setText(R.string.video_client_token)
        client_token_layout.ev_content.setHint(R.string.hint_client_token)
        client_token_layout.ev_content.inputType = InputType.TYPE_CLASS_TEXT
        client_token_layout.iv_more.visibility = View.GONE

        launch (Dispatchers.Main) {
            var jsonArrStr = SharePreferenceUtil.getString(this@VideoWlanDetectActivity, VideoConst.VIDEO_WLAN_CONFIG, VideoConst.VIDEO_ACCESS_INFOS)
            jsonArrStr?.let {
                var accessInfos = JSONArray.parseArray(jsonArrStr, AccessInfo::class.java)
                accessInfos?.let {
                    if (accessInfos.size > 0) {
                        var accessInfo = accessInfos.get(accessInfos.size - 1)
                        client_token_layout.ev_content.setText(accessInfo.accessToken)
                        product_id_layout.ev_content.setText(accessInfo.productId)
                    }
                }
            }
        }

        var layoutManager = LinearLayoutManager(this@VideoWlanDetectActivity)
        adapter = WlanDevsAdapter(this@VideoWlanDetectActivity, datas)
        devs_lv.setLayoutManager(layoutManager)
        devs_lv.setAdapter(adapter)
        adapter?.setOnItemClicked(onItemClicked)
    }

    private var onItemClicked = object : WlanDevsAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int) {
            App.data.accessInfo = AccessInfo()
            App.data.accessInfo?.productId = product_id_layout.ev_content.text.toString()

            var dev = DevInfo()
            dev.DeviceName = datas.get(pos).deviceName
            dev.Channel = 0
            dev.Status = 1
            WlanVideoPreviewActivity.startPreviewActivity(this@VideoWlanDetectActivity, dev)
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        btn_detect.setOnClickListener(searchClickedListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onPause() {
        super.onPause()
    }

    var searchClickedListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            if (TextUtils.isEmpty(product_id_layout.ev_content.text)) {
                Toast.makeText(this@VideoWlanDetectActivity, R.string.hint_product_id, Toast.LENGTH_SHORT).show()
                return
            }

            if (TextUtils.isEmpty(client_token_layout.ev_content.text)) {
                Toast.makeText(this@VideoWlanDetectActivity, R.string.hint_client_token, Toast.LENGTH_SHORT).show()
                return
            }

            var accessInfo = AccessInfo()
            accessInfo.accessToken = client_token_layout.ev_content.text.toString()
            accessInfo.productId = product_id_layout.ev_content.text.toString()

            launch (Dispatchers.Main) {
                checkAccessInfo(accessInfo)
            }

            datas.clear()
            var detectBody = WlanDetectBody()
            detectBody.productId = accessInfo.productId
            detectBody.clientToken = accessInfo.accessToken
        }
    }

    private var detectMesssageCallback = object: OnWlanDevicesDetectedCallback {
        override fun onMessage(version: String, resp: WlanRespBody): Boolean {
            if (!datas.contains(resp.params)) {
                datas.add(resp.params)
                runOnUiThread(Runnable {
                    adapter?.notifyDataSetChanged()
                })
            }
            return true
        }
    }

    private fun checkAccessInfo(accessInfo: AccessInfo) {
        var accessInfos: MutableList<AccessInfo> = ArrayList()
        accessInfos.add(accessInfo)
        SharePreferenceUtil.saveString(this@VideoWlanDetectActivity, VideoConst.VIDEO_WLAN_CONFIG, VideoConst.VIDEO_ACCESS_INFOS, JSONArray.toJSONString(accessInfos))
    }

}
