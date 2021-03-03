package com.tencent.iot.explorer.link.core.demo.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.adapter.ButtonInterface
import com.tencent.iot.explorer.link.core.demo.adapter.VideoMessageAdapter
import com.tencent.iot.explorer.link.core.demo.entity.VideoMessageEntity
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.video.link.callback.VideoCallback
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.service.VideoBaseService
import com.tencent.xnet.XP2P
import kotlinx.android.synthetic.main.activity_video_message.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class VideoMessageActivity : BaseActivity() {

    private var PAGE_SIZE = 10

    private lateinit var adapter: VideoMessageAdapter
    private var offset = 0
    private var secretId = ""
    private var secretKey = ""
    private var productId = ""

    private val videoMessageList by lazy {
        arrayListOf<VideoMessageEntity>()
    }

    override fun getContentView(): Int {
        return R.layout.activity_video_message
    }

    override fun initView() {
        secretId = SharePreferenceUtil.getString(this, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SECRET_ID)
        secretKey = SharePreferenceUtil.getString(this, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SECRET_KEY)
        productId = SharePreferenceUtil.getString(this, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_PRODUCT_ID)
        tv_title.text = "摄像头列表"
        tv_p2p_version.text = XP2P.getVersion()
        rv_video_message.layoutManager = LinearLayoutManager(this)
        adapter = VideoMessageAdapter(this, videoMessageList)
        rv_video_message.adapter = adapter
        refreshVideoMessageList()
    }

    override fun setListener() {
        btn_load_more.setOnClickListener { loadMoreVideoMessageList() }
        iv_back.setOnClickListener { finish() }
        adapter.buttonSetOnclick(object : ButtonInterface {
            //实时监控按钮点击
            override fun onRealtimeMonitorButtonClick(holder: BaseHolder<*>, clickView: View, position: Int) {

                val intent = Intent(this@VideoMessageActivity, VideoActivity::class.java)
                val bundle = Bundle()
                bundle.putString(VideoConst.VIDEO_SECRET_ID, secretId)
                bundle.putString(VideoConst.VIDEO_SECRET_KEY, secretKey)
                bundle.putString(VideoConst.VIDEO_PRODUCT_ID, productId)
                bundle.putString(VideoConst.VIDEO_DEVICE_NAME, videoMessageList[position].deviceName)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            //本地回放按钮点击
            override fun onLocalPlaybackButtonClick(holder: BaseHolder<*>, clickView: View, position: Int) {

                val intent = Intent(this@VideoMessageActivity, PlaybackVideoActivity::class.java)
                val bundle = Bundle()
                bundle.putString(VideoConst.VIDEO_SECRET_ID, secretId)
                bundle.putString(VideoConst.VIDEO_SECRET_KEY, secretKey)
                bundle.putString(VideoConst.VIDEO_PRODUCT_ID, productId)
                bundle.putString(VideoConst.VIDEO_DEVICE_NAME, videoMessageList[position].deviceName)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            //云端存储按钮点击
            override fun onCloudSaveButtonClick(holder: BaseHolder<*>, clickView: View, position: Int) {

                var intent = Intent(this@VideoMessageActivity, DateIPCActivity::class.java)
                intent.putExtra(DateIPCActivity.PRODUCTID, productId)
                intent.putExtra(DateIPCActivity.DEV_NAME, videoMessageList[position].deviceName)
                intent.putExtra(DateIPCActivity.SCRE_KEY, secretKey)
                intent.putExtra(DateIPCActivity.SCRE_ID, secretId)
                startActivity(intent)
            }

        })
    }

    private fun refreshVideoMessageList() {
        offset = 0
        videoMessageList.clear()
        loadMoreVideoMessageList()
    }

    private fun loadMoreVideoMessageList() {
        VideoBaseService(secretId, secretKey).getDeviceList(productId, 0, 99,
            object: VideoCallback {
                override fun fail(msg: String?, reqCode: Int) {
                    Toast.makeText(this@VideoMessageActivity, msg, Toast.LENGTH_SHORT).show()
                }

                override fun success(response: String?, reqCode: Int) {
                    val jsonObject = JSON.parse(response) as JSONObject
                    val jsonResponset = jsonObject.getJSONObject("Response") as JSONObject

                    val total = jsonResponset.getIntValue("Total")
                    if (offset >= total) return // 没有更多数据了

                    if (jsonResponset.containsKey("Devices")) {
                        val dataArray: JSONArray = jsonResponset.getJSONArray("Devices")
                        for (i in 0 until dataArray.size) {
                            var device = dataArray.get(i) as JSONObject
                            val entity = VideoMessageEntity()
                            entity.deviceName = device.getString("DeviceName")
                            videoMessageList.add(entity)
                        }

                        if (dataArray != null) offset += dataArray.size

                        runOnUiThread {
                            if (adapter != null) adapter?.notifyDataSetChanged()
                        }
                    }
                }
            })
    }

}
