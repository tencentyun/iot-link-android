package com.tencent.iot.explorer.link.core.demo.video.activity

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import com.tencent.iot.explorer.link.core.demo.video.adapter.DevPreviewAdapter
import com.tencent.iot.explorer.link.core.demo.video.entity.DevUrl2Preview
import com.tencent.iot.video.link.consts.VideoConst
import kotlinx.android.synthetic.main.activity_video_multi_preview.*


class VideoMultiPreviewActivity : BaseActivity() {
    var adapter : DevPreviewAdapter? = null
    lateinit var gridLayoutManager : GridLayoutManager
    lateinit var linearLayoutManager : LinearLayoutManager

    override fun getContentView(): Int {
        return R.layout.activity_video_multi_preview
    }

    override fun initView() {

        gridLayoutManager = GridLayoutManager(this@VideoMultiPreviewActivity, 2)
        linearLayoutManager = LinearLayoutManager(this@VideoMultiPreviewActivity)
        var bundle = intent.getBundleExtra(VideoConst.VIDEO_URLS)
        bundle?.let {
            var jsonArrStr = it.getString(VideoConst.VIDEO_URLS)
            jsonArrStr?.let {
                try {
                    var allUrl = JSONArray.parseArray(it, DevUrl2Preview::class.java)
                    var column = 2
                    if (allUrl.size <= 1) column = 1  // 当只有一个元素的时候，网格只有一列
                    gridLayoutManager = GridLayoutManager(this@VideoMultiPreviewActivity, column)
                    linearLayoutManager = LinearLayoutManager(this@VideoMultiPreviewActivity)
                    adapter = DevPreviewAdapter(this@VideoMultiPreviewActivity, allUrl)
                    gl_video.layoutManager = linearLayoutManager
                    gl_video.adapter = adapter
                } catch (e : Exception) {
                    e.printStackTrace()
                }
            }
        }
        switchOrientation(true)
        rg_orientation.check(radio_orientation_v.id)
    }

    override fun setListener() {
        rg_orientation.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                radio_orientation_h.id -> {
                    switchOrientation(false)
                }
                radio_orientation_v.id -> {
                    switchOrientation(true)
                }
            }
        }
    }

    private fun switchOrientation(orientationV : Boolean) {
        if (orientationV) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            radio_orientation_v.visibility = View.GONE
            radio_orientation_h.visibility = View.VISIBLE
            gl_video.layoutManager = linearLayoutManager
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            radio_orientation_v.visibility = View.VISIBLE
            radio_orientation_h.visibility = View.GONE
            gl_video.layoutManager = gridLayoutManager
        }
        adapter?.notifyDataSetChanged()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //横竖屏切换前调用，保存用户想要保存的数据
        outState.putString("key", "value")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        // 屏幕切换完毕后调用用户存储的数据
        if (savedInstanceState != null) { }
    }

}