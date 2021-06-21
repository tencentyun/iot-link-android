package com.tencent.iot.explorer.link.core.demo.video.activity

import android.content.pm.ActivityInfo
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.ViewPager
import com.alibaba.fastjson.JSON
import com.google.android.material.tabs.TabLayout
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import com.tencent.iot.explorer.link.core.demo.fragment.BaseFragment
import com.tencent.iot.explorer.link.core.demo.video.entity.DevInfo
import com.tencent.iot.explorer.link.core.demo.video.fragment.VideoCloudPlaybackFragment
import com.tencent.iot.explorer.link.core.demo.view.PageAdapter
import com.tencent.iot.video.link.consts.VideoConst
import kotlinx.android.synthetic.main.activity_video_playback.*
import kotlinx.android.synthetic.main.activity_video_playback.v_title
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.*
import kotlinx.android.synthetic.main.title_layout.*
import java.util.*


class VideoPlaybackActivity : BaseActivity() {

    private val page1 = VideoCloudPlaybackFragment()
    private val page2 = VideoCloudPlaybackFragment()
    private val mPageList: MutableList<BaseFragment> = ArrayList()
    private lateinit var mAdapter : PageAdapter

    override fun getContentView(): Int {
        return R.layout.activity_video_playback
    }

    override fun initView() {
        tv_title.setText(R.string.playback)

        mPageList.add(page1)
        mPageList.add(page2)
        mAdapter = PageAdapter(this.supportFragmentManager, mPageList)
        fragment_pager.setAdapter(mAdapter)
        fragment_pager.setPagingEnabled(false)
        fragment_pager.setOffscreenPageLimit(2)
        fragment_pager.setCurrentItem(0)

        val tabStrip = tab_playback.getChildAt(0) as LinearLayout
        for (i in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(i).setOnTouchListener { v, event -> true }
        }

        var bundle = intent.getBundleExtra(VideoConst.VIDEO_CONFIG)
        bundle?.let {
            var jsonStr = it.getString(VideoConst.VIDEO_CONFIG)
            if (TextUtils.isEmpty(jsonStr)) return@let

            page1.devInfo = JSON.parseObject(jsonStr, DevInfo::class.java)
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tab_playback.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}
            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                fragment_pager?.setCurrentItem(tab.position)
            }
        })

        fragment_pager.setOnPageChangeListener(pageSelectListener)
        page1.setOnOrientationChangedListener(onOrientationChangedListener)
    }

    private var onOrientationChangedListener = object : VideoCloudPlaybackFragment.OnOrientationChangedListener {
        override fun onOrientationChanged(portrait: Boolean) {
            var layoutParams = layout_video.layoutParams as ConstraintLayout.LayoutParams
            var fitSize = 0
            var visibility = View.VISIBLE

            if (portrait) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                visibility = View.GONE
                fitSize = ConstraintLayout.LayoutParams.MATCH_PARENT
            }
            v_title.visibility = visibility
            tab_playback.visibility = visibility
            v_line.visibility = visibility

            layoutParams.height = fitSize
            layoutParams.width = fitSize
            layout_video.layoutParams = layoutParams
        }
    }

    private var pageSelectListener = object : ViewPager.OnPageChangeListener{
        override fun onPageScrollStateChanged(state: Int) {}

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            tab_playback?.setScrollPosition(position, positionOffset, true)
        }

        override fun onPageSelected(position: Int) {
            tab_playback.getTabAt(position)?.select()
        }

    }

}