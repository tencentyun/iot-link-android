package com.tencent.iot.explorer.link.core.demo.activity

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.adapter.VideoMessageAdapter
import com.tencent.iot.explorer.link.core.demo.entity.VideoMessageEntity
import com.tencent.iot.explorer.link.core.test.callback.VideoCallback
import com.tencent.iot.explorer.link.core.test.service.VideoBaseService
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
    }

    /**
     *  获取摄像头列表
     */
    private fun refreshVideoMessageList() {
        VideoBaseService().describeDevices("", object:
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
                            adapter = VideoMessageAdapter(mContext!!, videoMessageList)
                            rv_video_message.adapter = adapter
                        }
                    }
                }
            }

        })
    }

}
