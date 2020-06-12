package com.tencent.iot.explorer.link.core.demo.activity

import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.fragment.DeviceFragment
import com.tencent.iot.explorer.link.core.demo.fragment.MeFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private lateinit var previousTab: TabLayout.Tab
    private val fragments = arrayListOf<Fragment>()

    override fun getContentView(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        tb_bottom.addTab(tb_bottom.newTab().setIcon(R.mipmap.main_tab_1_hover).setText("设备"), true)
        previousTab = tb_bottom.getTabAt(0)!!
        tb_bottom.addTab(tb_bottom.newTab().setIcon(R.mipmap.main_tab_3_normal).setText("我的"))

        fragments.clear()
        fragments.add(DeviceFragment())
        fragments.add(MeFragment())
        this.supportFragmentManager.beginTransaction()
            .add(R.id.main_container, fragments[0])
            .show(fragments[0])
            .commit()
    }

    override fun setListener() {
        tb_bottom.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                when (previousTab.position) {
                    0 -> previousTab.setIcon(R.mipmap.main_tab_1_normal)
                    1 -> previousTab.setIcon(R.mipmap.main_tab_3_normal)
                }
                when (tab.position) {
                    0 -> tab.setIcon(R.mipmap.main_tab_1_hover)
                    1 -> tab.setIcon(R.mipmap.main_tab_3_hover)
                }
                showFragment(tab.position, previousTab.position)
                previousTab = tab
            }
        })
    }

    private fun showFragment(position: Int, previewPosition: Int) {
        val transaction = this.supportFragmentManager.beginTransaction()
        if (fragments[position].isAdded) {
            transaction.show(fragments[position]).hide(fragments[previewPosition]).commit()
        } else {
            transaction.add(R.id.main_container, fragments[position])
                .show(fragments[position]).hide(fragments[previewPosition])
                .commit()
        }
    }

}



