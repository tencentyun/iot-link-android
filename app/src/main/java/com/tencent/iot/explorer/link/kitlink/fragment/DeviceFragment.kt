package com.tencent.iot.explorer.link.kitlink.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.alibaba.fastjson.JSON
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.customview.FullGridView
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.consts.LoadViewTxtType
import com.tencent.iot.explorer.link.kitlink.entity.CategoryDeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.ProdConfigDetailEntity
import com.tencent.iot.explorer.link.kitlink.entity.RecommDeviceEntity
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.response.RecommDeviceListResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.activity.SmartConfigStepActivity
import com.tencent.iot.explorer.link.kitlink.activity.SoftApStepActivity
import kotlinx.android.synthetic.main.fragment_devices.*


class DeviceFragment() : BaseFragment(), MyCallback, AdapterView.OnItemClickListener{

    private var mContext : Context? = null
    private var devicesGridView : FullGridView? = null
    private var recommendDevicesGridView : FullGridView? = null
    private var productList = arrayListOf<RecommDeviceEntity>()
    private var isRecommDeviceClicked = false
    @Volatile
    private var recommDeviceIndex = 0

    private var permissions = arrayOf(
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    enum class ConfigType (val id:Int) {
        SoftAp(1), SmartConfig(0);
    }

    constructor(c: Context):this() {
        mContext = c
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_devices
    }

    override fun startHere(view: View) {
        val categoryKey = arguments!!.getString("CategoryKey", "CategoryKey")
        devicesGridView = view.findViewById(R.id.gv_devices)
        recommendDevicesGridView = view.findViewById(R.id.gv_recommend_devices)
        setListener()
        HttpRequest.instance.getRecommList(categoryKey, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.get_recommend_device_list -> {
                if (response.isSuccess()) {
                    response.parse(RecommDeviceListResponse::class.java)?.run {
                        if (ProductList.size > 0) {
                            productList = ProductList
                            recommendDevicesGridView?.adapter = GridAdapter(mContext!!, ProductList, true)
                        } else {
                            if (tv_recommend != null) tv_recommend.visibility = View.GONE
                            if (split_line != null) split_line.visibility = View.GONE
                            if (gv_recommend_devices != null) gv_recommend_devices.visibility = View.GONE
                        }
                        if (devicesGridView != null && mContext != null) {
                            devicesGridView!!.adapter = GridAdapter(mContext!!, CategoryList, false)
                        }
                    }
                }
            }
            RequestCode.get_products_config -> {
                if (response.isSuccess()) {
                    response.parse(ProductsConfigResponse::class.java)?.run {
                        val config = JsonManager.parseJson(Data[0].Config, ProdConfigDetailEntity::class.java)
                        val wifiConfigTypeList = config.WifiConfTypeList
                        var productId = ""
                        if (!TextUtils.isEmpty(config.profile)) {
                            val jsonProFile = JSON.parseObject(config.profile)
                            if (jsonProFile != null && jsonProFile.containsKey("ProductId") &&
                                !TextUtils.isEmpty(jsonProFile.getString("ProductId"))) {
                                productId = jsonProFile.getString("ProductId")
                            }
                        }

                        if (wifiConfigTypeList == "{}" || TextUtils.isEmpty(wifiConfigTypeList)) {
                            startActivityWithExtra(SmartConfigStepActivity::class.java, productId)
                        } else if (wifiConfigTypeList.contains("[")) {
                            val typeList = JsonManager.parseArray(wifiConfigTypeList)
                            if (typeList.size > 0 && typeList[0] == "softap") {
                                startActivityWithExtra(SoftApStepActivity::class.java, productId)
                            } else {
                                startActivityWithExtra(SmartConfigStepActivity::class.java, productId)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startActivityWithExtra(cls: Class<*>?, productId: String) {
        val intent = Intent(context, cls)
        if (!TextUtils.isEmpty(productId)) {
            intent.putExtra(CommonField.LOAD_VIEW_TXT_TYPE, LoadViewTxtType.LoadRemoteViewTxt.ordinal)
            intent.putExtra(CommonField.PRODUCT_ID, productId)
        }
        startActivity(intent)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (view != null && parent != null) {
            if(ContextCompat.checkSelfPermission(mContext!!, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                when (parent.id) {
                    R.id.gv_recommend_devices->{
                        isRecommDeviceClicked = true
                        recommDeviceIndex = position
                    }
                    R.id.gv_devices->{
                        isRecommDeviceClicked = false
                    }
                }
                requestPermissions(permissions,1)
            } else {
                when (parent.id) {
                    R.id.gv_recommend_devices->{ // 根据推荐设备的配网方式，跳转到SmartConfig或者SoftAp配网界面
                        val productsList  = arrayListOf<String>()
                        productsList.add(productList[position].ProductId)
                        HttpRequest.instance.getProductsConfig(productsList, this)
                    }
                    R.id.gv_devices->{
                        startActivityWithExtra(SmartConfigStepActivity::class.java, "")
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (isRecommDeviceClicked) {
                val productsList  = arrayListOf<String>()
                productsList.add(productList[recommDeviceIndex].ProductId)
                HttpRequest.instance.getProductsConfig(productsList, this)
            } else {
                jumpActivity(SmartConfigStepActivity::class.java)
            }
        }
    }

    fun setListener() {
        devicesGridView?.onItemClickListener = this
        recommendDevicesGridView?.onItemClickListener = this
    }

    class GridAdapter : BaseAdapter {
        var deviceList : List<Any>? = null
        var context : Context? = null
        var inflater : LayoutInflater? = null
        var isRecommDeviceList : Boolean = false

        constructor(contxt : Context, list : List<Any>, value : Boolean) {
            context = contxt
            deviceList = list
            isRecommDeviceList = value
            inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val viewHolder : ViewHolder
            val retView : View
            if (convertView == null) {
                viewHolder = ViewHolder()
                retView = LayoutInflater.from(parent?.context). inflate(R.layout.device_item, parent, false)
                viewHolder.image = retView.findViewById(R.id.iv_device_icon)
                viewHolder.text = retView.findViewById(R.id.tv_device_name)
                retView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
                retView = convertView
            }

            val url: String
            if (isRecommDeviceList) {
                val entity = deviceList?.get(position) as RecommDeviceEntity
                viewHolder.text.text = entity.ProductName
                url = entity.IconUrl
            } else {
                val entity = deviceList?.get(position) as CategoryDeviceEntity
                viewHolder.text.text = entity.CategoryName
                url = entity.IconUrl
            }
            if (TextUtils.isEmpty(url)) {
                Picasso.get().load(R.drawable.device_placeholder).placeholder(R.drawable.device_placeholder)
                    .resize(App.data.screenWith / 5, App.data.screenWith / 5).centerCrop()
                    .into(viewHolder.image)
            } else {
                Picasso.get().load(url).placeholder(R.drawable.device_placeholder)
                    .resize(App.data.screenWith / 5, App.data.screenWith / 5).centerCrop()
                    .into(viewHolder.image)
            }
            return retView
        }

        override fun getItem(position: Int): Any? {
            return deviceList?.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return deviceList?.size?: 0
        }

        inner class ViewHolder {
            lateinit var image: ImageView
            lateinit var text: TextView
        }
    }

}