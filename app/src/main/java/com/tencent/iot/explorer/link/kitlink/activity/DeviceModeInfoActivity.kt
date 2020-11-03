package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.customview.dialog.DevModeSetDialog
import com.tencent.iot.explorer.link.customview.dialog.KeyBooleanValue
import com.tencent.iot.explorer.link.kitlink.adapter.DevModeAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.*
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import kotlinx.android.synthetic.main.activity_device_mode_info.*
import kotlinx.android.synthetic.main.menu_back_layout.*


class DeviceModeInfoActivity : BaseActivity(), MyCallback {

    private var devModes = ArrayList<DevModeInfo>()
    private var adapter: DevModeAdapter = DevModeAdapter(devModes)
    private var deviceEntity: DeviceEntity? = null

    override fun getContentView(): Int {
        return R.layout.activity_device_mode_info
    }

    override fun initView() {
        tv_title.setText("")

        var deviceEntityStr = intent.getStringExtra(CommonField.EXTRA_PRODUCT_ID)
        deviceEntity = JSON.parseObject(deviceEntityStr, DeviceEntity::class.java)

        val layoutManager = LinearLayoutManager(this)
        lv_dev_mode.setLayoutManager(layoutManager)
        adapter?.setOnItemClicked(onListItemClicked)
        lv_dev_mode.setAdapter(adapter)

        loadView()
    }

    private var onListItemClicked = object : DevModeAdapter.OnItemClicked{
        override fun onItemClicked(pos: Int, devModeInfo: DevModeInfo) {
            if (devModeInfo.define!!.get("type") == "bool" ||devModeInfo.define!!.get("type") == "enum") {

                var keyBooleanValues = ArrayList<KeyBooleanValue>()
                var mapJson = devModeInfo.define!!.getJSONObject("mapping")

                var startIndex = -1
                var i = 0
                for (key in mapJson.keys) {
                    var keyBooleanValue = KeyBooleanValue()
                    keyBooleanValue.key = key
                    keyBooleanValue.value = mapJson[key].toString()
                    keyBooleanValues.add(keyBooleanValue)
                    if (!TextUtils.isEmpty(devModes.get(pos).value) &&
                        devModes.get(pos).value == keyBooleanValue.value) {  // 当对应界面存在进度值时候，使用存在的进度值做数据
                        startIndex = i
                    }
                    i++
                }
                var dialog = DevModeSetDialog(this@DeviceModeInfoActivity, keyBooleanValues, devModeInfo.name, startIndex)
                dialog.show()
                dialog.setOnDismisListener(object : DevModeSetDialog.OnDismisListener{
                    override fun onSaveClicked() {
                        if (dialog.currentIndex >= 0) {
                            devModes.get(pos).value = keyBooleanValues.get(dialog.currentIndex).value
                            devModes.get(pos).key = keyBooleanValues.get(dialog.currentIndex).key
                            adapter.notifyDataSetChanged()
                        }
                    }

                    override fun onCancelClicked() {}
                })

            } else if (devModeInfo.define!!.get("type") == "int") {
                var modeInt = JSON.parseObject(devModeInfo.define!!.toJSONString(), ModeInt::class.java)
                if (!TextUtils.isEmpty(devModes.get(pos).value)) {  // 当对应界面存在进度值时候，使用存在的进度值做数据
                    modeInt.start = Integer.valueOf(devModes.get(pos).value)
                }
                var dialog = DevModeSetDialog(this@DeviceModeInfoActivity, devModeInfo.name, modeInt)
                dialog.show()
                dialog.setOnDismisListener(object : DevModeSetDialog.OnDismisListener{
                    override fun onSaveClicked() {
                        devModes.get(pos).value = dialog.progress.toString()
                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelClicked() {}
                })
            }
        }

    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_cancel.setOnClickListener { finish() }
        tv_ok.setOnClickListener {
            if (devModes == null || devModes.size <= 0) { // 没有数据禁止点击
                return@setOnClickListener
            }

            // 用于手机所有被修改的项，未被修改的项目不需要传递
            var passDevModes = ArrayList<DevModeInfo>()
            for (i in 0 until devModes.size) {
                if (!TextUtils.isEmpty(devModes.get(i).value)) {
                    passDevModes.add(devModes.get(i))
                }
            }
            var intent = Intent(this@DeviceModeInfoActivity, AddManualTaskActivity::class.java)
            intent.putExtra(CommonField.EXTRA_DEV_MODES, JSON.toJSONString(passDevModes))
            intent.putExtra(CommonField.EXTRA_DEV_DETAIL, JSON.toJSONString(deviceEntity))
            startActivity(intent)
        }
    }

    fun loadView() {
        if (deviceEntity == null) {
            return
        }
        var products = ArrayList<String>()
        products.add(deviceEntity!!.ProductId)
        HttpRequest.instance.deviceProducts(products, this)
        tv_title.setText(deviceEntity!!.getAlias())

    }

    override fun fail(msg: String?, reqCode: Int) {

    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when(reqCode) {
            RequestCode.device_product -> {
                if (response.isSuccess()) {

                    var dataTemplate: DataTemplate? = null
                    if (!TextUtils.isEmpty(response.data.toString())) {
                        var products = JSON.parseObject(response.data.toString(), ProductsEntity::class.java)
                        Log.e("XXX", "products " + JSONObject.toJSONString(products))
                        if (products == null || products.Products == null) {
                            return
                        }

                        for (i in 0 until products!!.Products!!.size) {
                            var productEntity = JSON.parseObject(products!!.Products!!.getString(i), ProductEntity::class.java)

                            if (productEntity.DataTemplate != null) {
                                dataTemplate = JSON.parseObject(productEntity.DataTemplate.toString(), DataTemplate::class.java)
                            }
                        }
                    }

                    if (dataTemplate == null || dataTemplate.properties == null || dataTemplate.properties!!.size == 0) {
                        return
                    }

                    for (i in 0 until dataTemplate.properties!!.size) {
                        var devModeInfo = JSON.parseObject(dataTemplate.properties!!.get(i).toString(), DevModeInfo::class.java)
                        Log.e("XXX", "devModeInfo " + JSONObject.toJSONString(devModeInfo))
                        if (devModeInfo != null && !devModeInfo.required && devModeInfo.mode == "rw"
                            && (devModeInfo.define != null && devModeInfo.define!!.get("type") != "string")) {
                            devModes.add(devModeInfo)
                        }
                    }
                    if (devModes == null || devModes.size <= 0) {
                        layout_no_data.visibility = View.VISIBLE
                        tv_ok.isClickable = false
                    } else {
                        layout_no_data.visibility = View.GONE
                        tv_ok.isClickable = true
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

}