package com.kitlink.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.kitlink.App
import com.kitlink.R
import com.kitlink.activity.BaseActivity
import com.kitlink.activity.DeviceCategoryActivity
import com.kitlink.activity.SmartConnectActivity
import com.kitlink.activity.SoftApActivity
import com.kitlink.entity.CategoryDeviceEntity
import com.kitlink.entity.ProdConfigDetailEntity
import com.kitlink.entity.RecommDeviceEntity
import com.kitlink.response.BaseResponse
import com.kitlink.response.ProductsConfigResponse
import com.kitlink.response.RecommDeviceListResponse
import com.kitlink.util.HttpRequest
import com.kitlink.util.JsonManager
import com.kitlink.util.MyCallback
import com.kitlink.util.RequestCode
import com.mvp.IPresenter
import com.squareup.picasso.Picasso
import com.util.L
import kotlinx.android.synthetic.main.fragment_devices.*
import kotlin.math.ceil


class DeviceFragment(c: Context) : BaseFragment(), MyCallback, AdapterView.OnItemClickListener{

    private var mContext : Context = c
    private var devicesGridView : GridView? = null
    private var recommendDevicesGridView : GridView? = null
    private var categoryList = arrayListOf<CategoryDeviceEntity>()
    private var productList = arrayListOf<RecommDeviceEntity>()
    private var isRecommDeviceClicked = false
    private var recommDeviceIndex = 0


    private var permissions = arrayOf(
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_devices
    }

    override fun startHere(view: View) {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val categoryKey = arguments!!.getString("CategoryKey", "CategoryKey")
        val view = inflater.inflate(getContentView(), container, false)
        if (getContentView() != 0) {
            devicesGridView = view.findViewById(R.id.gv_devices)
            recommendDevicesGridView = view.findViewById(R.id.gv_recommend_devices)
            setListener()
            HttpRequest.instance.getRecommList(categoryKey, this)
        }
        return view
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.get_recommend_device_list -> {
                if (response.isSuccess()) {
                    response.parse(RecommDeviceListResponse::class.java)?.run {
                        if (ProductList.size > 0) {
                            productList = ProductList
                            recommendDevicesGridView!!.adapter = GridAdapter(mContext, ProductList, true)
                            setGridViewHeightByChildren(recommendDevicesGridView!!)
                        } else {
                            tv_recommend.visibility = View.GONE
                            split_line.visibility = View.GONE
                            gv_recommend_devices.visibility = View.GONE
                        }
                        categoryList = CategoryList
                        devicesGridView!!.adapter = GridAdapter(mContext, CategoryList, false)
                        setGridViewHeightByChildren(devicesGridView!!)
                    }
                }
            }
            RequestCode.get_products_config -> {
                if (response.isSuccess()) {
                    response.parse(ProductsConfigResponse::class.java)?.run {
                        val config = JsonManager.parseJson(Data[0].Config, ProdConfigDetailEntity::class.java)
                        val wifiConfigTypeList = config.WifiConfTypeList
                        if (wifiConfigTypeList == "{}") {
                            jumpActivity(SmartConnectActivity::class.java)
                        } else if (wifiConfigTypeList.contains("[")) {
                            val typeList = JsonManager.parseArray(wifiConfigTypeList)
                            if (typeList.size > 0 && typeList[0] == "softap") {
                                jumpActivity(SoftApActivity::class.java)
                            } else {
                                jumpActivity(SmartConnectActivity::class.java)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (view != null && parent != null) {

            if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
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
                    R.id.gv_recommend_devices->{
                        val productsList  = arrayListOf<String>()
                        productsList.add(productList[position].ProductId)
                        HttpRequest.instance.getProductsConfig(productsList, this)
                    }
                    R.id.gv_devices->{
                        jumpActivity(SmartConnectActivity::class.java)
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
                jumpActivity(SmartConnectActivity::class.java)
            }
        }
    }

    fun setListener() {
        devicesGridView?.onItemClickListener = this
        recommendDevicesGridView?.onItemClickListener = this
    }

    private fun setGridViewHeightByChildren(gridView : GridView) {
        val adaper: ListAdapter? = gridView.adapter ?: return
        var totalHeight = 0
        val lineNum = ceil((adaper?.count?.toDouble() ?: 0.0) / 3.0)
        val item: View? = adaper?.getView(0,null, gridView)
        if (item != null) {
            totalHeight = ((App.data.screenWith/5 + if (lineNum > 1) 120 else 50) * lineNum).toInt()
        }
        val params = gridView.layoutParams
        params.height = totalHeight
        gridView.layoutParams = params
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
            Picasso.with(context).load(url).placeholder(R.drawable.device_placeholder)
                .resize(App.data.screenWith/5,App.data.screenWith/5).centerCrop().into(viewHolder.image)
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