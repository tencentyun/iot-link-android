package com.tencent.iot.explorer.link.core.demo.activity

import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.adapter.VideoMessageAdapter
import com.tencent.iot.explorer.link.core.demo.entity.VideoMessageEntity
import kotlinx.android.synthetic.main.activity_video_message.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class VideoMessageActivity : BaseActivity() {

    private lateinit var adapter: VideoMessageAdapter

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
        val entity = VideoMessageEntity()
        videoMessageList.add(entity)
        tv_title.text = "IoT video 行业版"

        rv_video_message.layoutManager = LinearLayoutManager(this)
        adapter = VideoMessageAdapter(this, videoMessageList)
        rv_video_message.adapter = adapter
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    /**
     *  获取家庭列表
     */
    private fun refreshVideoMessageList() {
    }

}
