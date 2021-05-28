package com.tencent.iot.explorer.link.core.demo.video.activity

import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import com.tencent.iot.explorer.link.core.demo.fragment.BaseFragment
import com.tencent.iot.explorer.link.core.demo.video.fragment.VideoCloudPlaybackFragment
import com.tencent.iot.explorer.link.core.demo.view.PageAdapter
import kotlinx.android.synthetic.main.activity_video_playback.*
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