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
import com.tencent.iot.explorer.link.core.demo.holder.VideoMessageHolder
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.video.link.callback.VideoCallback
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.service.VideoBaseService
import com.tencent.xnet.XP2P
import kotlinx.android.synthetic.main.activity_video_message.*
import kotlinx.android.synthetic.main.item_video_message.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class VideoMessageActivity : BaseActivity(), View.OnClickListener {

    private var PAGE_SIZE = 10

    private lateinit var adapter: VideoMessageAdapter
    private var offset = 0
    private var secretId = ""
    private var secretKey = ""
    private var productId = ""
    private var selectedArray: IntArray? = null

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
        btn_multi_video.setOnClickListener(this)
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

            override fun onSelectButtonClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                val item = holder as VideoMessageHolder
                if (selectedArray!![position] == 0) {
                    selectedArray!![position] = 1
                    item.itemView.btn_select.text = "已选"
                } else if(selectedArray!![position] == 1) {
                    selectedArray!![position] = 0
                    item.itemView.btn_select.text = "未选"
                }
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
                        selectedArray = IntArray(dataArray.size)
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

    override fun onClick(v: View?) {
        var selectedCount = 0
        val deviceArray = ArrayList<VideoMessageEntity>()
        for (i in selectedArray!!.indices) {
            if (selectedArray!![i] == 1) {
                selectedCount++
                deviceArray.add(videoMessageList[i])
            }
        }
        if (selectedCount == 2) {
            val intent = Intent(this@VideoMessageActivity, MultiVideoActivity::class.java)
            intent.putExtra(VideoConst.MULTI_VIDEO_PROD_ID, productId)
            intent.putExtra(VideoConst.MULTI_VIDEO_SECRET_KEY, secretKey)
            intent.putExtra(VideoConst.MULTI_VIDEO_SECRET_ID, secretId)
            intent.putExtra(VideoConst.MULTI_VIDEO_DEVICE_NAME01, deviceArray[0].deviceName)
            intent.putExtra(VideoConst.MULTI_VIDEO_DEVICE_NAME02, deviceArray[1].deviceName)
            startActivity(intent)
        } else {
            Toast.makeText(this, "请选择两个设备观看多路直播", Toast.LENGTH_LONG).show()
        }
    }
}
