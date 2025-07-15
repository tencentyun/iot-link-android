package com.tencent.iot.explorer.link.demo.video

import android.view.View
import androidx.fragment.app.Fragment
import com.alibaba.fastjson.JSONObject
import com.google.android.material.tabs.TabLayout
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityVideoMainBinding
import com.tencent.iot.video.link.consts.VideoConst

class VideoMainActivity : VideoBaseActivity<ActivityVideoMainBinding>() {
    private val fragments = arrayListOf<Fragment>()

    override fun getViewBinding(): ActivityVideoMainBinding = ActivityVideoMainBinding.inflate(layoutInflater)

    override fun initView() {
        intent?.let {
            it.getBundleExtra(VideoConst.VIDEO_CONFIG)?.let {
                it.getString(VideoConst.VIDEO_CONFIG)?.let {
                    try {
                        App.data.accessInfo = JSONObject.parseObject(it, AccessInfo::class.java)
                    } catch (e : Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        binding.vTitle.tvTitle.setText(R.string.iot_demo_name)
        binding.tbBottom.visibility = View.GONE
        fragments.clear()
        fragments.add(VideoDeviceFragment())
        this.supportFragmentManager.beginTransaction()
            .add(R.id.main_container, fragments[0])
            .show(fragments[0])
            .commit()
    }

    override fun setListener() {
        binding.tbBottom.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabSelected(tab: TabLayout.Tab) {}
        })
    }
}



