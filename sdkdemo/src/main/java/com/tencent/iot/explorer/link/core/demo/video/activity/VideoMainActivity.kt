package com.tencent.iot.explorer.link.core.demo.video.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.alibaba.fastjson.JSONObject
import com.google.android.material.tabs.TabLayout
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import com.tencent.iot.explorer.link.core.demo.fragment.DeviceFragment
import com.tencent.iot.explorer.link.core.demo.video.entity.AccessInfo
import com.tencent.iot.explorer.link.core.demo.video.fragment.VideoDeviceFragment
import com.tencent.iot.video.link.consts.VideoConst
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.title_layout.*
import java.lang.Exception

class VideoMainActivity : BaseActivity() {

    private val fragments = arrayListOf<Fragment>()
    private var accessInfo : AccessInfo? = null

    override fun getContentView(): Int {
        return R.layout.activity_video_main
    }

    override fun initView() {

        var intent = getIntent()
        intent?.let {
            var bundle = it.getBundleExtra(VideoConst.VIDEO_CONFIG)
            bundle?.let {
                var infoStr = bundle.getString(VideoConst.VIDEO_CONFIG)
                infoStr?.let {
                    try {
                        accessInfo = JSONObject.parseObject(it, AccessInfo::class.java)
                    } catch (e : Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        tv_title.setText(R.string.iot_demo_name)
        tb_bottom.visibility = View.GONE
        fragments.clear()
        fragments.add(VideoDeviceFragment(accessInfo))
        this.supportFragmentManager.beginTransaction()
            .add(R.id.main_container, fragments[0])
            .show(fragments[0])
            .commit()
    }

    override fun setListener() {
        tb_bottom.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabSelected(tab: TabLayout.Tab) {}
        })
    }

}



