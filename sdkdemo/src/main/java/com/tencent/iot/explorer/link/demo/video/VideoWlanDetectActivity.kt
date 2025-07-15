package com.tencent.iot.explorer.link.demo.video

import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityVideoDetectDevsBinding
import com.tencent.iot.explorer.link.demo.video.preview.WlanVideoPreviewActivity
import com.tencent.iot.video.link.callback.OnWlanDevicesDetectedCallback
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.entity.DeviceServerInfo
import com.tencent.iot.video.link.entity.WlanDetectBody
import com.tencent.iot.video.link.entity.WlanRespBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class VideoWlanDetectActivity : VideoBaseActivity<ActivityVideoDetectDevsBinding>() , CoroutineScope by MainScope() {

    var datas: MutableList<DeviceServerInfo> = ArrayList()
    var adapter: WlanDevsAdapter? = null

    override fun getViewBinding(): ActivityVideoDetectDevsBinding = ActivityVideoDetectDevsBinding.inflate(layoutInflater)

    override fun initView() {
        with(binding) {
            vTitle.tvTitle.setText(R.string.video_wlan)
            productIdLayout.tvTip.setText(R.string.product_id)
            productIdLayout.evContent.setHint(R.string.hint_product_id)
            productIdLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            productIdLayout.ivMore.visibility = View.GONE

            clientTokenLayout.tvTip.setText(R.string.video_client_token)
            clientTokenLayout.evContent.setHint(R.string.hint_client_token)
            clientTokenLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            clientTokenLayout.ivMore.visibility = View.GONE

            launch(Dispatchers.Main) {
                val jsonArrStr = SharePreferenceUtil.getString(
                    this@VideoWlanDetectActivity,
                    VideoConst.VIDEO_WLAN_CONFIG,
                    VideoConst.VIDEO_ACCESS_INFOS
                )
                jsonArrStr?.let {
                    val accessInfos = JSONArray.parseArray(jsonArrStr, AccessInfo::class.java)
                    accessInfos?.let {
                        if (accessInfos.size > 0) {
                            val accessInfo = accessInfos.get(accessInfos.size - 1)
                            clientTokenLayout.evContent.setText(accessInfo.accessToken)
                            productIdLayout.evContent.setText(accessInfo.productId)
                        }
                    }
                }
            }

            adapter = WlanDevsAdapter(this@VideoWlanDetectActivity, datas)
            devsLv.adapter = adapter
            adapter?.setOnItemClicked(onItemClicked)
        }
    }

    private var onItemClicked = object : WlanDevsAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int) {
            App.data.accessInfo = AccessInfo()
            App.data.accessInfo?.productId = binding.productIdLayout.evContent.text.toString()

            val dev = DevInfo()
            dev.DeviceName = datas.get(pos).deviceName
            dev.Channel = 0
            dev.Status = 1
            WlanVideoPreviewActivity.startPreviewActivity(this@VideoWlanDetectActivity, dev)
        }
    }

    override fun setListener() {
        binding.vTitle.ivBack.setOnClickListener { finish() }
        binding.btnDetect.setOnClickListener(searchClickedListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onPause() {
        super.onPause()
    }

    private var searchClickedListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            if (TextUtils.isEmpty(binding.productIdLayout.evContent.text)) {
                Toast.makeText(this@VideoWlanDetectActivity, R.string.hint_product_id, Toast.LENGTH_SHORT).show()
                return
            }

            if (TextUtils.isEmpty(binding.clientTokenLayout.evContent.text)) {
                Toast.makeText(this@VideoWlanDetectActivity, R.string.hint_client_token, Toast.LENGTH_SHORT).show()
                return
            }

            val accessInfo = AccessInfo()
            accessInfo.accessToken = binding.clientTokenLayout.evContent.text.toString()
            accessInfo.productId = binding.productIdLayout.evContent.text.toString()

            launch (Dispatchers.Main) {
                checkAccessInfo(accessInfo)
            }

            datas.clear()
            val detectBody = WlanDetectBody()
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
        val accessInfos: MutableList<AccessInfo> = ArrayList()
        accessInfos.add(accessInfo)
        SharePreferenceUtil.saveString(this@VideoWlanDetectActivity, VideoConst.VIDEO_WLAN_CONFIG, VideoConst.VIDEO_ACCESS_INFOS, JSONArray.toJSONString(accessInfos))
    }

}
