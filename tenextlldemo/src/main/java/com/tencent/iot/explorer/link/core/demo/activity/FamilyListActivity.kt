package com.tenext.demo.activity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.response.BaseResponse
import com.tenext.auth.response.FamilyListResponse
import com.tenext.auth.util.JsonManager
import com.tenext.demo.App
import com.tenext.demo.R
import com.tenext.demo.adapter.FamilyListAdapter
import com.tenext.demo.adapter.OnItemListener
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.log.L
import kotlinx.android.synthetic.main.activity_family_list.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 家庭列表
 */
class FamilyListActivity : BaseActivity() {

    private lateinit var adapter: FamilyListAdapter

    private var isRefresh = false

    override fun onResume() {
        super.onResume()
        if (isRefresh)
            refreshFamilyList()
    }

    override fun onStop() {
        super.onStop()
        isRefresh = true
    }

    override fun getContentView(): Int {
        return R.layout.activity_family_list
    }

    override fun initView() {
        tv_title.text = getString(R.string.family_manager)

        rv_family_list.layoutManager = LinearLayoutManager(this)
        adapter = FamilyListAdapter(this, IoTAuth.familyList)
        rv_family_list.adapter = adapter
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_add_family.setOnClickListener {
            jumpActivity(AddFamilyActivity::class.java)
        }
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                put("family", IoTAuth.familyList[position])
                jumpActivity(FamilyActivity::class.java)
            }
        })
    }

    /**
     *  获取家庭列表
     */
    private fun refreshFamilyList() {
        IoTAuth.familyImpl.familyList(0, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.e(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    IoTAuth.familyList.clear()
                    response.parse(FamilyListResponse::class.java)?.run {
                        L.e("家庭列表：${JsonManager.toJson(FamilyList)}")
                        IoTAuth.familyList.addAll(FamilyList)
                        App.data.getCurrentFamily()
                        showFamilyList()
                    }
                }
            }
        })
    }

    private fun showFamilyList() {
        runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }
}
