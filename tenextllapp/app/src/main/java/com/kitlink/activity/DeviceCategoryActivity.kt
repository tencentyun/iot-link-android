package com.kitlink.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kitlink.App
import com.kitlink.R
import com.kitlink.fragment.DeviceFragment
import com.kitlink.holder.DeviceListViewHolder
import com.kitlink.response.BaseResponse
import com.kitlink.response.DeviceCategoryListResponse
import com.kitlink.util.HttpRequest
import com.kitlink.util.JsonManager
import com.kitlink.util.MyCallback
import com.kitlink.util.RequestCode
import com.mvp.IPresenter
import com.util.L
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.activity_device_category.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.android.synthetic.main.menu_cancel_layout.tv_title
import kotlinx.android.synthetic.main.scanning.*
import q.rorbin.verticaltablayout.adapter.TabAdapter
import q.rorbin.verticaltablayout.widget.TabView


class DeviceCategoryActivity  : PActivity(), MyCallback, CRecyclerView.RecyclerItemView{
    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.activity_device_category
    }

    override fun initView() {
        tv_title.text = getString(R.string.add_device)
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
    }

    override fun onResume() {
        super.onResume()
        val rotateAnimation : Animation = AnimationUtils.loadAnimation(this, R.anim.circle_rotate)
        val interpolator =  LinearInterpolator()
        rotateAnimation.interpolator = interpolator
        iv_loading_cirecle.startAnimation(rotateAnimation)
        HttpRequest.instance.getParentCategoryList(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        iv_loading_cirecle.clearAnimation()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.get_parent_category_list-> {
                if (response.isSuccess()) {
                    response.parse(DeviceCategoryListResponse::class.java)?.run {
                        L.e("推荐设备分类列表：${JsonManager.toJson(List)}")
                        App.data.recommendDeviceCategoryList.clear()
                        App.data.recommendDeviceCategoryList.addAll(List)
                        val adapter = MyTabAdapter()
                        for (item in List) {
                            adapter.titleList.add(item.CategoryName)
                        }
                        vtab_device_category.setupWithFragment(
                            supportFragmentManager,
                            R.id.devce_fragment_container,
                            generateFragments(),
                            adapter
                        )
                    }
                }
            }
        }
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        Toast.makeText(this,  "position $position is clicked", Toast.LENGTH_LONG).show()
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return DeviceListViewHolder(this, parent, R.layout.item_scanned_device)
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

    private fun generateFragments() : List<Fragment>{
        val fragmentList = arrayListOf<Fragment>()
        for (item in App.data.recommendDeviceCategoryList) {
            val fragment = DeviceFragment(this)
            val bundle = Bundle()
            bundle.putString("CategoryKey", item.CategoryKey)
            fragment.arguments = bundle
            fragmentList.add(fragment)
        }
        return fragmentList
    }

    class MyTabAdapter : TabAdapter {
        var titleList = arrayListOf<String>()

        override fun getIcon(position: Int): TabView.TabIcon? {
            return null
        }

        override fun getBadge(position: Int): TabView.TabBadge? {
            return null
        }

        override fun getBackground(position: Int): Int {
            return 0
        }

        override fun getTitle(position: Int): TabView.TabTitle {
            return TabView.TabTitle.Builder().setContent(titleList[position]).build()
        }

        override fun getCount(): Int {
            return titleList.size
        }
    }
}
