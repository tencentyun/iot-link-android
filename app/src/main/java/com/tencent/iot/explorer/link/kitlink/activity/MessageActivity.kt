package com.tencent.iot.explorer.link.kitlink.activity

import com.google.android.material.tabs.TabLayout
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.PageAdapter
import com.tencent.iot.explorer.link.kitlink.fragment.BaseFragment
import com.tencent.iot.explorer.link.kitlink.fragment.MessageFragment
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.util.*

class MessageActivity : BaseActivity() {

    private val page1 = MessageFragment(1)
    private val page2 = MessageFragment(2)
    private val page3 = MessageFragment(3)
    private val mPageList: MutableList<BaseFragment> = ArrayList()
    private lateinit var mAdapter :PageAdapter

    override fun getContentView(): Int {
        return R.layout.activity_message
    }

    override fun initView() {
        tv_title.text = getString(R.string.message_notification)

        mPageList.add(page1)
        mPageList.add(page2)
        mPageList.add(page3)
        mAdapter = PageAdapter(this.supportFragmentManager, mPageList)
        fragment_pager.setAdapter(mAdapter)
        fragment_pager.setPagingEnabled(false)
        fragment_pager.setOffscreenPageLimit(3)
        fragment_pager.setCurrentItem(0)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tab_message.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabSelected(tab: TabLayout.Tab) {
                fragment_pager?.setCurrentItem(tab.position)
            }
        })
    }
}
