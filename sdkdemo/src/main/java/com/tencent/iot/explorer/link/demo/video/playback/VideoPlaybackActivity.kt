package com.tencent.iot.explorer.link.demo.video.playback

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.alibaba.fastjson.JSON
import com.google.android.material.tabs.TabLayout
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.common.customView.PageAdapter
import com.tencent.iot.explorer.link.demo.core.fragment.BaseFragment
import com.tencent.iot.explorer.link.demo.databinding.ActivityVideoPlaybackBinding
import com.tencent.iot.explorer.link.demo.video.DevInfo
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.VideoCloudPlaybackFragment
import com.tencent.iot.explorer.link.demo.video.playback.localPlayback.VideoLocalPlaybackFragment
import com.tencent.iot.video.link.consts.VideoConst
import java.util.*


class VideoPlaybackActivity : VideoBaseActivity<ActivityVideoPlaybackBinding>() {

    private val page1 = VideoCloudPlaybackFragment()
    private val page2 = VideoLocalPlaybackFragment()
    private val mPageList: MutableList<BaseFragment<*>> = ArrayList()
    private lateinit var mAdapter : PageAdapter
    private var pageIndex = 0

    override fun getViewBinding(): ActivityVideoPlaybackBinding = ActivityVideoPlaybackBinding.inflate(layoutInflater)

    override fun initView() {
        with(binding) {
            vTitle.tvTitle.setText(R.string.playback)

            mPageList.add(page1)
            mPageList.add(page2)
            mAdapter = PageAdapter(this@VideoPlaybackActivity.supportFragmentManager, mPageList)
            fragmentPager.adapter = mAdapter
            fragmentPager.setPagingEnabled(false)
            fragmentPager.offscreenPageLimit = 2
//        // 禁止 tab 点击
//        val tabStrip = tab_playback.getChildAt(0) as LinearLayout
//        for (i in 0 until tabStrip.childCount) {
//            tabStrip.getChildAt(i).setOnTouchListener { v, event -> true }
//        }

            intent.getBundleExtra(VideoConst.VIDEO_CONFIG)?.let {
                var jsonStr = it.getString(VideoConst.VIDEO_CONFIG)
                if (TextUtils.isEmpty(jsonStr)) return@let

                page1.devInfo = JSON.parseObject(jsonStr, DevInfo::class.java)
                page2.devInfo = JSON.parseObject(jsonStr, DevInfo::class.java)
                pageIndex = it.getInt(VideoConst.VIDEO_PAGE_INDEX)
            }
            fragmentPager.currentItem = pageIndex
        }
    }

    override fun setListener() {
        binding.vTitle.ivBack.setOnClickListener { finish() }
        binding.tabPlayback.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.fragmentPager.currentItem = tab.position
            }
        })

        page2.onOrientationChangedListener = onOrientationChangedListener
        page1.onOrientationChangedListener = onOrientationChangedListener
    }

    private var onOrientationChangedListener = object : OnOrientationChangedListener {
        override fun onOrientationChanged(portrait: Boolean) {
            var layoutParams = page1.getLayoutVideo().layoutParams as ConstraintLayout.LayoutParams
            var localLayoutParams = page2.getLayoutVideo().layoutParams as ConstraintLayout.LayoutParams
            var fitSize = 0
            var visibility = View.VISIBLE

            if (portrait) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                visibility = View.GONE
                fitSize = ConstraintLayout.LayoutParams.MATCH_PARENT
            }

            with(binding) {
                vTitle.root.visibility = visibility
                tabPlayback.visibility = visibility
                vLine.visibility = visibility
            }

            layoutParams.height = fitSize
            layoutParams.width = fitSize
            localLayoutParams.height = fitSize
            localLayoutParams.width = fitSize
            page1.getLayoutVideo().layoutParams = layoutParams
            page2.getLayoutVideo().layoutParams = localLayoutParams
        }
    }

    companion object {
        fun startPlaybackActivity(context: Context?, dev: DevInfo, pageIndex: Int) {
            var intent = Intent(context, VideoPlaybackActivity::class.java)
            var bundle = Bundle()
            bundle.putString(VideoConst.VIDEO_CONFIG, JSON.toJSONString(dev))
            bundle.putInt(VideoConst.VIDEO_PAGE_INDEX, pageIndex)
            intent.putExtra(VideoConst.VIDEO_CONFIG, bundle)
            context?.startActivity(intent)
        }

        fun startPlaybackActivity(context: Context?, dev: DevInfo) {
            startPlaybackActivity(context, dev, 0)
        }
    }
}