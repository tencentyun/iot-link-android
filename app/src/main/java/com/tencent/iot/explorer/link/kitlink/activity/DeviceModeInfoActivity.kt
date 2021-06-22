package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.customview.dialog.DevModeSetDialog
import com.tencent.iot.explorer.link.customview.dialog.KeyBooleanValue
import com.tencent.iot.explorer.link.kitlink.adapter.DevModeAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.*
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DevModeInfo
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import kotlinx.android.synthetic.main.activity_device_mode_info.*
import kotlinx.android.synthetic.main.menu_back_layout.*


class DeviceModeInfoActivity : BaseActivity(), MyCallback {

    private var devModes = ArrayList<DevModeInfo>()
    private var adapter: DevModeAdapter? = null
    private var deviceEntity: DeviceEntity? = null
    private var manualTask: ManualTask? = null
    private var routeType = RouteType.MANUAL_TASK_ROUTE
    private var keepList = ArrayList<String>()

    override fun getContentView(): Int {
        return R.layout.activity_device_mode_info
    }

    override fun initView() {
        routeType = intent.getIntExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.MANUAL_TASK_ROUTE)
        var deviceEntityStr = intent.getStringExtra(CommonField.EXTRA_PRODUCT_ID)
        var manualTaskStr = intent.getStringExtra(CommonField.EXTRA_DEV_MODES)
        deviceEntity = JSON.parseObject(deviceEntityStr, DeviceEntity::class.java)  // 新增依赖的对象
        manualTask = JSON.parseObject(manualTaskStr, ManualTask::class.java) // 修改依赖的对象

        adapter = DevModeAdapter(devModes, routeType)
        val layoutManager = LinearLayoutManager(this)
        lv_dev_mode.setLayoutManager(layoutManager)
        adapter?.setOnItemClicked(onListItemClicked)
        lv_dev_mode.setAdapter(adapter)
        tv_title.setText("")
        loadView()
    }

    private fun loadProductConfig(productId: String) {
        var products = ArrayList<String>()
        products.add(productId)
        HttpRequest.instance.getProductsConfig(products, this)
    }

    private var onListItemClicked = object : DevModeAdapter.OnItemClicked{
        override fun onItemClicked(pos: Int, devModeInfo: DevModeInfo) {
            if (devModeInfo.define == null) return

            var type = devModeInfo.define!!.get("type")
            if (type == "bool" || type == "enum") {
                showMapDialog(pos, devModeInfo)
            } else if (type == "int") {
                showNumDialog(true, pos, devModeInfo)
            } else if (type == "float") {
                showNumDialog(false, pos, devModeInfo)
            }
        }
    }

    private fun showMapDialog(pos: Int, devModeInfo: DevModeInfo) {
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
                    adapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelClicked() {}
        })
    }

    private fun showNumDialog(ifInteger: Boolean, pos: Int, devModeInfo: DevModeInfo) {
        var modeInt = JSON.parseObject(devModeInfo.define!!.toJSONString(), ModeInt::class.java)
        if (!TextUtils.isEmpty(devModes.get(pos).value)) {  // 当对应界面存在进度值时候，使用存在的进度值做数据
            modeInt.start = devModes.get(pos).value.toFloat()
        }
        modeInt.ifInteger = ifInteger
        if (routeType == RouteType.EDIT_AUTOMIC_CONDITION_ROUTE ||
            routeType == RouteType.EDIT_AUTOMIC_CONDITION_DETAIL_ROUTE ||
            routeType == RouteType.AUTOMIC_CONDITION_ROUTE ||
            routeType == RouteType.ADD_AUTOMIC_CONDITION_DETAIL_ROUTE) {
            modeInt.showOp = true
            modeInt.op = devModes.get(pos).op
        }

        var dialog = DevModeSetDialog(this@DeviceModeInfoActivity, devModeInfo.name, modeInt)
        dialog.show()
        dialog.setOnDismisListener(object : DevModeSetDialog.OnDismisListener{
            override fun onSaveClicked() {
                if (!modeInt.ifInteger) {
                    devModes.get(pos).value = String.format("%.1f", dialog.progress)//dialog.progress.toString()
                } else {
                    devModes.get(pos).value = dialog.progress.toInt().toString()
                }
                devModes.get(pos).unit = modeInt.unit
                devModes.get(pos).op = modeInt.op
                adapter?.notifyDataSetChanged()
            }

            override fun onCancelClicked() {}
        })
    }

    override fun setListener() {
        tv_add_now_btn.setOnClickListener { finish() }
        iv_back.setOnClickListener { finish() }
        tv_cancel.setOnClickListener { finish() }
        tv_ok.setOnClickListener {
            if (devModes == null || devModes.size <= 0) { // 没有数据禁止点击
                return@setOnClickListener
            }

            // 用于收集所有被修改的项，未被修改的项目不需要传递
            var passDevModes = ArrayList<DevModeInfo>()
            for (i in 0 until devModes.size) {
                if (!TextUtils.isEmpty(devModes.get(i).value)) {
                    passDevModes.add(devModes.get(i))
                }
            }
            if (passDevModes.size <= 0) {
                T.show(getString(R.string.please_set_first))
                return@setOnClickListener
            }

            var intent = Intent()
            if (routeType == RouteType.MANUAL_TASK_ROUTE || routeType == RouteType.EDIT_MANUAL_TASK_ROUTE) {
                intent = Intent(this@DeviceModeInfoActivity, AddManualTaskActivity::class.java)
            } else if (routeType == RouteType.AUTOMIC_TASK_ROUTE || routeType == RouteType.AUTOMIC_CONDITION_ROUTE
                || routeType == RouteType.EDIT_AUTOMIC_TASK_ROUTE || routeType == RouteType.EDIT_AUTOMIC_CONDITION_ROUTE) {
                intent = Intent(this@DeviceModeInfoActivity, AddAutoicTaskActivity::class.java)
            } else if (routeType == RouteType.EDIT_MANUAL_TASK_DETAIL_ROUTE || routeType == RouteType.ADD_MANUAL_TASK_DETAIL_ROUTE) {
                intent = Intent(this@DeviceModeInfoActivity, EditManualTaskActivity::class.java)
            } else if (routeType == RouteType.EDIT_AUTOMIC_CONDITION_DETAIL_ROUTE || routeType == RouteType.ADD_AUTOMIC_CONDITION_DETAIL_ROUTE ||
                routeType == RouteType.EDIT_AUTOMIC_TASK_DETAIL_ROUTE || routeType == RouteType.ADD_AUTOMIC_TASK_DETAIL_ROUTE) {
                intent = Intent(this@DeviceModeInfoActivity, EditAutoicTaskActivity::class.java)
            }

            intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, routeType)
            intent.putExtra(CommonField.EXTRA_DEV_MODES, JSON.toJSONString(passDevModes))
            intent.putExtra(CommonField.EXTRA_DEV_DETAIL, JSON.toJSONString(deviceEntity))
            startActivity(intent)
        }
    }

    fun loadView() {
        if (routeType == RouteType.AUTOMIC_CONDITION_ROUTE ||
            routeType == RouteType.MANUAL_TASK_ROUTE ||
            routeType == RouteType.AUTOMIC_TASK_ROUTE ||
            routeType == RouteType.ADD_MANUAL_TASK_DETAIL_ROUTE ||
            routeType == RouteType.ADD_AUTOMIC_CONDITION_DETAIL_ROUTE ||
            routeType == RouteType.ADD_AUTOMIC_TASK_DETAIL_ROUTE) {
            if (deviceEntity == null) return

            var products = ArrayList<String>()
            products.add(deviceEntity!!.ProductId)
            HttpRequest.instance.deviceProducts(products, this)
            tv_title.setText(deviceEntity!!.getAlias())

        } else if (routeType == RouteType.EDIT_MANUAL_TASK_ROUTE ||
            routeType == RouteType.EDIT_AUTOMIC_TASK_ROUTE ||
            routeType == RouteType.EDIT_MANUAL_TASK_DETAIL_ROUTE ||
            routeType == RouteType.EDIT_AUTOMIC_CONDITION_ROUTE ||
            routeType == RouteType.EDIT_AUTOMIC_CONDITION_DETAIL_ROUTE ||
            routeType == RouteType.EDIT_AUTOMIC_TASK_DETAIL_ROUTE) {

            if (manualTask == null) return

            tv_title.setText(manualTask!!.getAlias())
            deviceEntity = DeviceEntity()
            deviceEntity!!.IconUrl = manualTask!!.iconUrl
            deviceEntity!!.ProductId = manualTask!!.productId
            deviceEntity!!.DeviceName = manualTask!!.deviceName
            deviceEntity!!.AliasName = manualTask!!.aliasName
            keepList.add(manualTask!!.actionId)
            var products = ArrayList<String>()
            products.add(deviceEntity!!.ProductId)
            HttpRequest.instance.deviceProducts(products, this)
        }
    }

    private fun refreshView() {
        if (devModes == null || devModes.size <= 0) {
            layout_no_data.visibility = View.VISIBLE
            tv_ok.isClickable = false
            layout_btn.visibility = View.GONE
        } else {
            layout_no_data.visibility = View.GONE
            tv_ok.isClickable = true
            layout_btn.visibility = View.VISIBLE
        }
        adapter?.notifyDataSetChanged()
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
        refreshView()
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when(reqCode) {
            RequestCode.get_products_config -> {
                response.parse(ProductsConfigResponse::class.java)?.run {
                    var config = JsonManager.parseJson(Data[0].Config, ProdConfigDetailEntity::class.java)
                    if (config == null) {
                        return@run
                    }
                    keepActions(config)
                    keepConditions(config)
                    keepOne4Edit()
                    refreshView()
                }
            }

            RequestCode.device_product -> {
                if (response.isSuccess()) {

                    var dataTemplate: DataTemplate? = null
                    if (!TextUtils.isEmpty(response.data.toString())) {
                        var products = JSON.parseObject(response.data.toString(), ProductsEntity::class.java)
                        if (products == null || products.Products == null) return

                        for (i in 0 until products!!.Products!!.size) {
                            var productEntity = JSON.parseObject(products!!.Products!!.getString(i), ProductEntity::class.java)

                            if (productEntity.DataTemplate != null) {
                                dataTemplate = JSON.parseObject(productEntity.DataTemplate.toString(), DataTemplate::class.java)
                            }
                        }
                    }

                    if (dataTemplate == null || dataTemplate.properties == null || dataTemplate.properties!!.size == 0) {
                        refreshView()
                        return
                    }
                    for (i in 0 until dataTemplate.properties!!.size) {
                        var devModeInfo = JSON.parseObject(dataTemplate.properties!!.get(i).toString(), DevModeInfo::class.java)
                        devModes.add(devModeInfo)
                    }

                    loadProductConfig(deviceEntity!!.ProductId)
                }
            }
        }
    }

    private fun keepActions(config: ProdConfigDetailEntity) {
        if (routeType == RouteType.MANUAL_TASK_ROUTE ||
            routeType == RouteType.AUTOMIC_TASK_ROUTE ||
            routeType == RouteType.EDIT_MANUAL_TASK_ROUTE ||
            routeType == RouteType.EDIT_AUTOMIC_TASK_ROUTE ||
            routeType == RouteType.EDIT_MANUAL_TASK_DETAIL_ROUTE ||
            routeType == RouteType.ADD_MANUAL_TASK_DETAIL_ROUTE ||
            routeType == RouteType.EDIT_AUTOMIC_TASK_DETAIL_ROUTE ||
            routeType == RouteType.ADD_AUTOMIC_TASK_DETAIL_ROUTE) {
            if (config.AppAutomation == null) {
                config.AppAutomation = AppAutomation()
            }
            if (config != null && config.AppAutomation != null && config.AppAutomation!!.actions != null) {
                var it = devModes.iterator()
                while(it.hasNext()) {
                    var devInfo = it.next()
                    if (!config.AppAutomation!!.actions.contains(devInfo.id)) {
                        it.remove()
                    }
                }
            }
        }
    }

    private fun keepConditions(config: ProdConfigDetailEntity) {
        if (routeType == RouteType.AUTOMIC_CONDITION_ROUTE ||
            routeType == RouteType.EDIT_AUTOMIC_CONDITION_ROUTE ||
            routeType == RouteType.EDIT_AUTOMIC_CONDITION_DETAIL_ROUTE ||
            routeType == RouteType.ADD_AUTOMIC_CONDITION_DETAIL_ROUTE) {
            if (config.AppAutomation == null) {
                config.AppAutomation = AppAutomation()
            }
            if (config != null && config.AppAutomation != null && config.AppAutomation!!.conditions != null) {
                var it = devModes.iterator()
                while(it.hasNext()) {
                    var devInfo = it.next()
                    if (!config.AppAutomation!!.conditions.contains(devInfo.id)) {
                        it.remove()
                    }
                }
            }
        }
    }

    // 编辑方式进入该界面，只保留一条
    private fun keepOne4Edit() {
        if (routeType == RouteType.EDIT_MANUAL_TASK_ROUTE ||
            routeType == RouteType.EDIT_AUTOMIC_TASK_ROUTE ||
            routeType == RouteType.EDIT_MANUAL_TASK_DETAIL_ROUTE ||
            routeType == RouteType.EDIT_AUTOMIC_CONDITION_ROUTE ||
            routeType == RouteType.EDIT_AUTOMIC_CONDITION_DETAIL_ROUTE ||
            routeType == RouteType.EDIT_AUTOMIC_TASK_DETAIL_ROUTE) {

            var it = devModes.iterator()
            while(it.hasNext()) {
                var devInfo = it.next()
                if (!keepList.contains(devInfo.id)) {
                    it.remove()
                } else {
                    devInfo.id = manualTask!!.actionId
                    devInfo.name = manualTask!!.taskTip
                    devInfo.value = manualTask!!.task
                    devInfo.key = manualTask!!.taskKey
                    devInfo.pos = manualTask!!.pos
                    devInfo.unit = manualTask!!.unit
                    devInfo.op = manualTask!!.op
                }
            }
        }
    }

}