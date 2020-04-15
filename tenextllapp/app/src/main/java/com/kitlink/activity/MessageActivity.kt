package com.kitlink.activity

import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.kitlink.R
import com.kitlink.fragment.MessageFragment
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.menu_back_layout.*


/**
 * 消息列表
 */
class MessageActivity : BaseActivity() {

    private var previousPosition = 0
    private val fragments = arrayListOf<Fragment>()

    override fun getContentView(): Int {
        return R.layout.activity_message
    }


    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.message_notification)

        fragments.clear()
        fragments.add(MessageFragment(1))
        fragments.add(MessageFragment(2))
        fragments.add(MessageFragment(3))
        this.supportFragmentManager.beginTransaction()
            .add(R.id.message_container, fragments[0])
            .show(fragments[0])
            .commit()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tab_message.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                showFragment(tab.position)
            }
        })
    }

    private fun showFragment(position: Int) {
        val transaction = this.supportFragmentManager.beginTransaction()
        if (fragments[position].isAdded) {
            transaction.show(fragments[position]).hide(fragments[previousPosition]).commit()
        } else {
            transaction.add(R.id.message_container, fragments[position])
                .show(fragments[position]).hide(fragments[previousPosition])
                .commit()
        }
        previousPosition = position
    }

}
