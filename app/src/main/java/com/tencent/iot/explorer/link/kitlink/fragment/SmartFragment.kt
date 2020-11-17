package com.tencent.iot.explorer.link.kitlink.fragment

import android.app.Activity
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.PageAdapter
import com.tencent.iot.explorer.link.customview.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.kitlink.activity.AddAutoicTaskActivity
import com.tencent.iot.explorer.link.kitlink.activity.AddManualTaskActivity
import com.tencent.iot.explorer.link.kitlink.util.StatusBarUtil
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.fragment_smart.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.util.ArrayList

/**
 *  智能联动界面
 */
class SmartFragment : BaseFragment(), View.OnClickListener {
    private val page1 = MySmartFragment()
    private val page2 = SmartLogFragment()
    private val mPageList: MutableList<BaseFragment> = ArrayList()
    private var addDialog: ListOptionsDialog? = null
    private lateinit var mAdapter :PageAdapter

    override fun getContentView(): Int {
        return R.layout.fragment_smart
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    // 每次窗口返回刷新一次
    override fun onResume() {
        super.onResume()
    }

    // 每次刷新显示，重新获取一次
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {

        }
    }

    override fun startHere(view: View) {
        initView()
        setListener()
    }

    private fun setListener() {
        fragment_pager.setOnPageChangeListener(pageSelectListener)
        fragment_pager.setPagingEnabled(false)
        tv_login_now_btn.setOnClickListener(this)
        iv_right_btn.setOnClickListener(this)

        tab_smart.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                if (fragment_pager != null) {
                    fragment_pager.setCurrentItem(tab.position)
                }
            }
        })
    }

    private fun initView() {
        iv_back.visibility = View.INVISIBLE
        iv_right_btn.visibility = View.VISIBLE
        iv_right_btn.setImageResource(R.mipmap.icon_more)
        tv_title.setText(R.string.main_tab_5)
        if (!TextUtils.isEmpty(App.data.getToken())) {
            mPageList.add(page1)
            mPageList.add(page2)
            mAdapter = PageAdapter(childFragmentManager, mPageList)
            fragment_pager.setAdapter(mAdapter)
            fragment_pager.setPagingEnabled(true)
            fragment_pager.setOffscreenPageLimit(2)
            fragment_pager.setCurrentItem(0)
            fragment_pager.visibility = View.VISIBLE
        } else {
            fragment_pager.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_login_now_btn -> {
                App.toLogin()
            }
            iv_right_btn -> {
                showOptionDialog()
            }
        }
    }

    private fun showOptionDialog() {
        if (addDialog == null) {
            var options = ArrayList<String>()
            options.add(getString(R.string.smart_option_1))
            options.add(getString(R.string.smart_option_2))
            addDialog = ListOptionsDialog(context, options)
            addDialog?.setOnDismisListener(onItemClickedListener)
        }
        addDialog?.show()
    }

    private var onItemClickedListener = ListOptionsDialog.OnDismisListener {
        if (it == 0) {
            jumpActivity(AddManualTaskActivity::class.java)
        } else if (it == 1) {
            jumpActivity(AddAutoicTaskActivity::class.java)
        }
    }

    private var pageSelectListener = object : ViewPager.OnPageChangeListener{
        override fun onPageScrollStateChanged(state: Int) {}

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            if (tab_smart != null) {
                tab_smart.setScrollPosition(position, positionOffset, true)
            }
        }

        override fun onPageSelected(position: Int) {
            tab_smart.getTabAt(position)?.select()
        }

    }


}