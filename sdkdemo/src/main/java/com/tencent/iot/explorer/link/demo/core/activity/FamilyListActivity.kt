package com.tencent.iot.explorer.link.demo.core.activity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.FamilyListResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.adapter.FamilyListAdapter
import com.tencent.iot.explorer.link.demo.core.adapter.OnItemListener
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.databinding.ActivityFamilyListBinding

/**
 * 家庭列表
 */
class FamilyListActivity : BaseActivity<ActivityFamilyListBinding>() {

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

    override fun getViewBinding(): ActivityFamilyListBinding = ActivityFamilyListBinding.inflate(layoutInflater)

    override fun initView() {
        with(binding) {
            familyListMenu.tvTitle.text = getString(R.string.family_manager)

            rvFamilyList.layoutManager = LinearLayoutManager(this@FamilyListActivity)
            adapter = FamilyListAdapter(this@FamilyListActivity, IoTAuth.familyList)
            rvFamilyList.adapter = adapter
        }
    }

    override fun setListener() {
        binding.familyListMenu.ivBack.setOnClickListener { finish() }
        binding.tvAddFamily.setOnClickListener {
            jumpActivity(AddFamilyActivity::class.java)
        }
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*, *>, clickView: View, position: Int) {
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
