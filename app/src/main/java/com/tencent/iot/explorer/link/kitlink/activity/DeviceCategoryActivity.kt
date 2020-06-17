package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.qrcode.Constant
import com.example.qrcode.ScannerActivity
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.device.DeviceInfo
import com.tencent.iot.explorer.link.kitlink.fragment.DeviceFragment
import com.tencent.iot.explorer.link.kitlink.holder.DeviceListViewHolder
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.response.DeviceCategoryListResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.kitlink.customview.MyScrollView
import com.tencent.iot.explorer.link.util.L
import com.tencent.iot.explorer.link.util.T
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.activity_device_category.*
import kotlinx.android.synthetic.main.bluetooth_adapter_invalid.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.android.synthetic.main.menu_cancel_layout.tv_title
import kotlinx.android.synthetic.main.not_found_device.*
import kotlinx.android.synthetic.main.scanning.*
import q.rorbin.verticaltablayout.VerticalTabLayout
import q.rorbin.verticaltablayout.adapter.TabAdapter
import q.rorbin.verticaltablayout.widget.ITabView
import q.rorbin.verticaltablayout.widget.TabView


class DeviceCategoryActivity  : PActivity(), MyCallback, CRecyclerView.RecyclerItemView, View.OnClickListener, VerticalTabLayout.OnTabSelectedListener{

    private val handler = Handler()

    private var permissions = arrayOf(
        Manifest.permission.CAMERA
    )

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


    private val runnable = Runnable {
        iv_loading_cirecle.clearAnimation()
        scanning.visibility = View.GONE
        not_found_dev.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beginScanning()
    }

    override fun onResume() {
        super.onResume()
        App.data.screenWith = getScreenWidth()
        HttpRequest.instance.getParentCategoryList(this)
        Thread.sleep(150)
        vtab_device_category.setTabSelected(App.data.tabPosition)
    }

    override fun onDestroy() {
        super.onDestroy()
        iv_loading_cirecle.clearAnimation()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        iv_scann.setOnClickListener(this)
        iv_question.setOnClickListener(this)
        vtab_device_category.addOnTabSelectedListener(this)
        retry_to_scann01.setOnClickListener(this)
        retry_to_scann02.setOnClickListener(this)
        my_scroll_view.setScrollChangedListener(object: MyScrollView.ScrollChangedListener{
            override fun onScrollChanged(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                val height = scanner_bar.height + gray_line_0.height + linearlayout_scann.height
                if (scrollY >= height && vtab_device_category.parent ==container_normal) {
                    container_normal.removeView(vtab_device_category)
                    gray_line_1.visibility = View.GONE
                    container_top.visibility = View.VISIBLE
                    container_top.addView(vtab_device_category)
                } else if (scrollY < height && vtab_device_category.parent ==container_top){
                    gray_line_1.visibility = View.VISIBLE
                    container_top.visibility = View.GONE
                    container_top.removeView(vtab_device_category)
                    container_normal.addView(vtab_device_category, 0)
                }
            }
        })
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.get_parent_category_list-> {
                if (response.isSuccess()) {
                    response.parse(DeviceCategoryListResponse::class.java)?.run {
                        App.data.recommendDeviceCategoryList = List
                        val adapter = MyTabAdapter()
                        for (item in List) {
                            adapter.titleList.add(item.CategoryName)
                        }
                        vtab_device_category.layoutParams.width = App.data.screenWith/4
                        vtab_device_category.setupWithFragment(
                            supportFragmentManager,
                            R.id.devce_fragment_container,
                            generateFragments(),
                            adapter
                        )
                    }
                }
            }
            RequestCode.scan_bind_device-> {
                if (response.isSuccess()) {
                    T.show("添加成功")
                    App.data.setRefreshLevel(2)
                    finish()
                } else {
                    T.show(response.msg)
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

    override fun permissionAllGranted() {
        startActivityForResult(Intent(this, ScannerActivity::class.java), 103)
    }

    override fun permissionDenied(permission: String) {
        requestPermission(arrayOf(permission))
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_scann -> {
                if (checkPermissions(permissions)) {
                    startActivityForResult(Intent(this, ScannerActivity::class.java), 103)
                } else {
                    requestPermission(permissions)
                }
            }
            iv_question -> {
                jumpActivity(HelpCenterActivity::class.java)
            }
            retry_to_scann01, retry_to_scann02 -> {
                beginScanning()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            if (resultCode == Activity.RESULT_OK) {
                val type = it.getStringExtra(Constant.EXTRA_RESULT_CODE_TYPE)
                it.getStringExtra(Constant.EXTRA_RESULT_CONTENT)?.run {
                    L.d("type=$type,content=$this")
                    when {
                        //https://iot.cloud.tencent.com/iotexplorer/device?page=virtual&signature=bb6364e6fd2b48d881be106756903c60
                        contains("signature=") -> {//虚拟设备
                            bindDevice(this.substringAfterLast("signature="))
                        }
                        //{"DeviceName":"big_light","ProductId":"9URPO59MDN","Signature":"9a3b3940581f4d03a2c633eb679719eb"}
                        contains("\"DeviceName\"") and contains("\"Signature\"") -> {//真实设备
                            val deviceInfo = DeviceInfo(this)
                            if (!TextUtils.isEmpty(deviceInfo.productId)) {
                                bindDevice(deviceInfo.signature)
                            }
                        }
                        contains("page=softap") -> {
                            jumpActivity(SoftApActivity::class.java)
                        }
                        contains("page=smartconfig") -> {
                            jumpActivity(SmartConnectActivity::class.java)
                        }
                        else -> {//之前旧版本虚拟设备二维码只有签名
                            bindDevice(this)
                        }
                    }
                }
            }
        }
    }

    /**
     * 绑定虚拟设备
     */
    private fun bindDevice(signature: String) {
        HttpRequest.instance.scanBindDevice(App.data.getCurrentFamily().FamilyId, App.data.getCurrentRoom().RoomId, signature, this)
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
        App.data.numOfCategories = fragmentList.size
        return fragmentList
    }

    private fun getScreenWidth() : Int {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels
    }

    private fun isBluetoothValid() : Boolean {
        val adapter  = BluetoothAdapter.getDefaultAdapter()
        return adapter?.isEnabled ?: false
    }

    private fun beginScanning() {
        val rotateAnimation : Animation = AnimationUtils.loadAnimation(this, R.anim.circle_rotate)
        val interpolator =  LinearInterpolator()
        rotateAnimation.interpolator = interpolator
        if (isBluetoothValid()) {
            scanning.visibility = View.VISIBLE
            not_found_dev.visibility = View.GONE
            scann_fail.visibility = View.GONE
            iv_loading_cirecle.startAnimation(rotateAnimation)
            handler.postDelayed(runnable, 15000)
        } else {
            scann_fail.visibility = View.VISIBLE
            scanning.visibility = View.GONE
            not_found_dev.visibility = View.GONE
        }
    }

    private fun stopScanning() {

    }

    class MyTabAdapter : TabAdapter {
        var titleList = arrayListOf<String>()

        override fun getIcon(position: Int): ITabView.TabIcon? {
            return null
        }

        override fun getBadge(position: Int): ITabView.TabBadge? {
            return null
        }

        override fun getBackground(position: Int): Int {
            if (position == 0) return R.drawable.tab
            return 0
        }

        override fun getTitle(position: Int): ITabView.TabTitle {
            return ITabView.TabTitle.Builder().setContent(titleList[position]).setTextColor(
                0xFF0052D9.toInt(),0xFF000000.toInt() // 蓝色:0xFF0052D9, 黑色:0xFF000000
            ).build()
        }

        override fun getCount(): Int {
            return titleList.size
        }
    }

    override fun onTabReselected(tab: TabView?, position: Int) {
    }

    override fun onTabSelected(tab: TabView?, position: Int) {
        App.data.tabPosition = position
        for (i in 0 until App.data.numOfCategories) {
            if (i != position)
                vtab_device_category.getTabAt(i).setBackgroundColor(resources.getColor(R.color.gray_F5F5F5))
        }
        tab?.setBackground(R.drawable.tab)
    }
}
