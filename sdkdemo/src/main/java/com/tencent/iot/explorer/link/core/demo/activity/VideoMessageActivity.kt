package com.tencent.iot.explorer.link.core.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
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
import kotlinx.android.synthetic.main.activity_video_message.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class VideoMessageActivity : BaseActivity() {

    private lateinit var adapter: VideoMessageAdapter

    private var mContext: Context? = null

    private val videoMessageList by lazy {
        arrayListOf<VideoMessageEntity>()
    }

    private var isRefresh = false

    override fun onResume() {
        super.onResume()
        if (isRefresh)
            refreshVideoMessageList()
    }

    override fun onStop() {
        super.onStop()
        isRefresh = true
    }

    override fun getContentView(): Int {
        return R.layout.activity_video_message
    }

    override fun initView() {
        mContext = applicationContext
        tv_title.text = "录像回放"

        rv_video_message.layoutManager = LinearLayoutManager(this)
        adapter = VideoMessageAdapter(this, videoMessageList)
        rv_video_message.adapter = adapter
        refreshVideoMessageList()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        adapter.buttonSetOnclick(object : ButtonInterface {
            //实时监控按钮点击
            override fun onRealtimeMonitorButtonClick(
                holder: BaseHolder<*>,
                clickView: View,
                position: Int
            ) {
                val secretId = SharePreferenceUtil.getString(this@VideoMessageActivity, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SECRET_ID)
                val secretKey = SharePreferenceUtil.getString(this@VideoMessageActivity, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SECRET_KEY)
                val productId = SharePreferenceUtil.getString(this@VideoMessageActivity, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_PRODUCT_ID)
                val deviceName = videoMessageList[position].deviceName
                val intent = Intent(this@VideoMessageActivity, VideoActivity::class.java)
                val bundle = Bundle()
                bundle.putString(VideoConst.VIDEO_SECRET_ID, secretId)
                bundle.putString(VideoConst.VIDEO_SECRET_KEY, secretKey)
                bundle.putString(VideoConst.VIDEO_PRODUCT_ID, productId)
                bundle.putString(VideoConst.VIDEO_DEVICE_NAME, deviceName)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            //本地回放按钮点击
            override fun onLocalPlaybackButtonClick(
                holder: BaseHolder<*>,
                clickView: View,
                position: Int
            ) {
                val secretId = SharePreferenceUtil.getString(this@VideoMessageActivity, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SECRET_ID)
                val secretKey = SharePreferenceUtil.getString(this@VideoMessageActivity, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SECRET_KEY)
                val productId = SharePreferenceUtil.getString(this@VideoMessageActivity, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_PRODUCT_ID)
                val deviceName = videoMessageList[position].deviceName
                val intent = Intent(this@VideoMessageActivity, VideoActivity::class.java)
                val bundle = Bundle()
                bundle.putString(VideoConst.VIDEO_SECRET_ID, secretId)
                bundle.putString(VideoConst.VIDEO_SECRET_KEY, secretKey)
                bundle.putString(VideoConst.VIDEO_PRODUCT_ID, productId)
                bundle.putString(VideoConst.VIDEO_DEVICE_NAME, deviceName)
                bundle.putString(VideoConst.VIDEO_PLAYBACK, VideoConst.VIDEO_PLAYBACK)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            //云端存储按钮点击
            override fun onCloudSaveButtonClick(
                holder: BaseHolder<*>,
                clickView: View,
                position: Int
            ) {
                val secretId = SharePreferenceUtil.getString(this@VideoMessageActivity, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SECRET_ID)
                val secretKey = SharePreferenceUtil.getString(this@VideoMessageActivity, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SECRET_KEY)
                val productId = SharePreferenceUtil.getString(this@VideoMessageActivity, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_PRODUCT_ID)
                val deviceName = videoMessageList[position].deviceName
                var intent = Intent(this@VideoMessageActivity, IPCActivity::class.java)
                jumpActivity(InputAuthorizeActivity::class.java)
                intent.putExtra(IPCActivity.URL, "")
                startActivity(intent)
            }

        })
    }

    /**
     *  获取摄像头列表
     */
    private fun refreshVideoMessageList() {
        val secretId = SharePreferenceUtil.getString(this, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SECRET_ID)
        val secretKey = SharePreferenceUtil.getString(this, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SECRET_KEY)
        val productId = SharePreferenceUtil.getString(this, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_PRODUCT_ID)
        VideoBaseService(secretId, secretKey).describeDevices(productId, object:
            VideoCallback {
            override fun fail(msg: String?, reqCode: Int) {

            }

            override fun success(response: String?, reqCode: Int) {
                val jsonObject = JSON.parse(response) as JSONObject
                val jsonResponset = jsonObject.getJSONObject("Response") as JSONObject
                if (jsonResponset.containsKey("Devices")) {
                    val dataArray: JSONArray = jsonResponset.getJSONArray("Devices")
                    videoMessageList.clear()
                    for (i in 0 until dataArray.size) {
                        var device = dataArray.get(i) as JSONObject
                        val entity = VideoMessageEntity()
                        entity.deviceName = device.getString("DeviceName")
                        videoMessageList.add(entity)
                    }
                    runOnUiThread {
                        if (mContext != null) {
                            adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }

        })
    }

}
