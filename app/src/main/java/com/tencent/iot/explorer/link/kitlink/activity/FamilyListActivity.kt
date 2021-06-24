package com.tencent.iot.explorer.link.kitlink.activity

import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.FamilyListResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.holder.FamilyListFootHolder
import com.tencent.iot.explorer.link.kitlink.holder.FamilyListViewHolder
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import kotlinx.android.synthetic.main.activity_family_list.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 家庭列表管理
 */
class FamilyListActivity : BaseActivity(), MyCallback, CRecyclerView.RecyclerItemView {

    private var total = 0
    private lateinit var familyListFootHolder: FamilyListFootHolder

    override fun getContentView(): Int {
        return R.layout.activity_family_list
    }

    override fun onResume() {
        super.onResume()
        refreshFamilyList()
    }

    override fun initView() {
        tv_title.text = getString(R.string.family_manager)
        crv_family_list.setList(App.data.familyList)
        crv_family_list.addRecyclerItemView(this)
        addFooter()
    }

    private fun addFooter() {
        familyListFootHolder =
            FamilyListFootHolder(this, crv_family_list, R.layout.foot_family_list)

        familyListFootHolder.footListener = object : CRecyclerView.FootListener {
            override fun doAction(
                holder: CRecyclerView.FootViewHolder<*>,
                clickView: View,
                position: Int
            ) {
                addFamily()
            }
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    /**
     * 添加家庭
     */
    private fun addFamily() {
        jumpActivity(AddFamilyActivity::class.java)
    }

    /**
     *  获取家庭列表
     */
    private fun refreshFamilyList() {
        App.data.familyList.clear()
        loadFamilyList()
    }

    /**
     *  获取家庭列表
     */
    private fun loadFamilyList() {
        if (App.data.familyList.size > 0 && App.data.familyList.size >= total) return
        HttpRequest.instance.familyList(App.data.familyList.size, this)
    }

    /**
     * 显示家庭列表
     */
    private fun showFamily(){
        crv_family_list.notifyDataChanged()
        crv_family_list.addFooter(familyListFootHolder)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            response.parse(FamilyListResponse::class.java)?.run {
                total = Total
                L.e("家庭列表：${JsonManager.toJson(FamilyList)}")
                App.data.familyList.addAll(FamilyList)
                App.data.getCurrentFamily()
                showFamily()
            }
        }
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        if (position >= App.data.familyList.size) return

        put("family", App.data.familyList[position])
        jumpActivity(FamilyActivity::class.java)
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return FamilyListViewHolder(this, parent, R.layout.item_family_list)
    }

    override fun getViewType(position: Int): Int {
        return 0
    }
}
