package com.tencent.iot.explorer.link.kitlink.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.customview.dialog.PermissionDialog
import com.tencent.iot.explorer.link.kitlink.activity.BleConfigHardwareActivity
import com.tencent.iot.explorer.link.kitlink.activity.BuleToothActivity
import com.tencent.iot.explorer.link.kitlink.activity.SmartConfigStepActivity
import com.tencent.iot.explorer.link.kitlink.activity.SoftApStepActivity
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.consts.LoadViewTxtType
import com.tencent.iot.explorer.link.kitlink.entity.ProdConfigDetailEntity
import com.tencent.iot.explorer.link.kitlink.response.CategoryDeviceInfo
import com.tencent.iot.explorer.link.kitlink.response.CategoryDeviceResponse
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.response.SecondDeviceCategoryResponse
import com.tencent.iot.explorer.link.kitlink.response.SecondLevelCategoryInfo
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.BleToGoPresenter
import com.tencent.iot.explorer.link.mvp.view.BleToGoView
import kotlinx.android.synthetic.main.new_fragment_devices.second_category_container


class SecondDeviceCategoryFragment() : BaseFragment(), MyCallback {

    companion object {
        const val NOT_NEED_CHECK_H5_CONDITION = 0
        const val CHECK_H5_CONDITION = 1
    }

    private var goBleH5Presenter: BleToGoPresenter? = null
    private var mContext: Context? = null
    private var mllRecommendTitle: LinearLayout? = null
    private var mRvRecommend: RecyclerView? = null
    private var mRecommendDeviceList = arrayListOf<CategoryDeviceInfo>()
    private var mLlTitleMap = linkedMapOf<String, LinearLayout>()
    private var mGridAdapterMap = linkedMapOf<String, RecyclerView>()
    private val secondLevelCategoryList = arrayListOf<SecondLevelCategoryInfo>()
    private var isRecommDeviceClicked = false

    @Volatile
    private var clickDeviceInfo: CategoryDeviceInfo? = null
    private var checkH5Type = 0  // 0 不进行 H5 页面的调用，用于智能联动页面额设备显示； 1 扫码、添加设备页面进入 H5 页面的条件检查

    @Volatile
    private var conditionPrefix = false

    private var permissionDialog: PermissionDialog? = null
    private var permissions = arrayOf(
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    constructor(c: Context) : this() {
        mContext = c
    }

    constructor(c: Context, checkType: Int) : this(c) {
        checkH5Type = checkType
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.new_fragment_devices
    }

    override fun startHere(view: View) {
        if (checkH5Type == 1) {
            goBleH5Presenter = BleToGoPresenter(bleToGoView)
        }
        val categoryKey = arguments!!.getString("CategoryKey", "CategoryKey")
        mllRecommendTitle = view.findViewById(R.id.ll_recommend_title)
        mRvRecommend = view.findViewById(R.id.rv_recommend_devices)
        mRvRecommend?.adapter = GridAdapter(onItemClickListener)
        HttpRequest.instance.getSecondLevelCategoryList(categoryKey, this)
    }

    var bleToGoView = object : BleToGoView {
        override fun onGoH5Ble(productId: String) {
            var bundle = Bundle()
            bundle.putString(CommonField.EXTRA_INFO, productId)
            var intent =
                Intent(this@SecondDeviceCategoryFragment.context, BuleToothActivity::class.java)
            intent.putExtra(CommonField.EXTRA_INFO, bundle)
            startActivity(intent)
            return
        }

        override fun onNeedCheckProductConfig(productId: String) {
            conditionPrefix = true
            val productsList = arrayListOf<String>()
            productsList.add(productId)
            HttpRequest.instance.getProductsConfig(productsList, this@SecondDeviceCategoryFragment)
        }

        override fun onGoNormalPage(productId: String) {
            val productsList = arrayListOf<String>()
            productsList.add(productId)
            HttpRequest.instance.getProductsConfig(productsList, this@SecondDeviceCategoryFragment)
        }

        override fun onRequestFailed(msg: String?) {
            T.show(msg)
        }

    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (!response.isSuccess()) return
        when (reqCode) {
            RequestCode.get_second_level_category_list -> {
                response.jsonParse(SecondDeviceCategoryResponse::class.java)?.let {
                    addSecondCategoryContent(it.Infos)
                }
            }

            RequestCode.get_category_device_list -> {
                response.jsonParse(CategoryDeviceResponse::class.java)?.let {
                    showCategoryDeviceInfo(it.Infos)
                }
            }

            RequestCode.get_products_config -> {
                response.parse(ProductsConfigResponse::class.java)?.run {
                    val config = JsonManager.parseJson(
                        Data[0].Config,
                        ProdConfigDetailEntity::class.java
                    )
                    if (config == null) return

                    val wifiConfigTypeList = config.WifiConfTypeList
                    var productId = ""
                    if (!TextUtils.isEmpty(config.profile)) {
                        val jsonProFile = JSON.parseObject(config.profile)
                        if (jsonProFile != null && jsonProFile.containsKey("ProductId") &&
                            !TextUtils.isEmpty(jsonProFile.getString("ProductId"))
                        ) {
                            productId = jsonProFile.getString("ProductId")
                        }
                    }

                    if (checkH5Type == CHECK_H5_CONDITION && conditionPrefix &&
                        config.bleConfig != null && config.bleConfig!!.protocolType == "custom"
                    ) {
                        val bundle = Bundle()
                        bundle.putString(CommonField.EXTRA_INFO, productId)
                        val intent = Intent(
                            this@SecondDeviceCategoryFragment.context,
                            BuleToothActivity::class.java
                        )
                        intent.putExtra(CommonField.EXTRA_INFO, bundle)
                        startActivity(intent)
                        return
                    }

                    if (wifiConfigTypeList == "{}" || TextUtils.isEmpty(wifiConfigTypeList)) {
                        startActivityWithExtra(SmartConfigStepActivity::class.java, productId)
                    } else if (wifiConfigTypeList.contains("[")) {
                        val typeList = JsonManager.parseArray(wifiConfigTypeList)
                        if (typeList.size > 0 && typeList[0] == "softap") {
                            startActivityWithExtra(SoftApStepActivity::class.java, productId)
                        } else if (typeList.size > 0 && typeList[0] == "llsyncble") {
                            this@SecondDeviceCategoryFragment.context?.let {
                                BleConfigHardwareActivity.startWithProductid(it, productId)
                            }
                        } else {
                            startActivityWithExtra(
                                SmartConfigStepActivity::class.java,
                                productId
                            )
                        }
                    }
                }
            }
        }
    }

    private fun addSecondCategoryContent(infoList: List<SecondLevelCategoryInfo>) {
        secondLevelCategoryList.clear()
        secondLevelCategoryList.addAll(infoList)
        secondLevelCategoryList.forEach {
            HttpRequest.instance.getCategoryDeviceList(it.SubCategoryKey, this)
            val categoryName =
                if (Utils.getLang() == "zh-CN") it.SubCategoryName else it.SubCategoryEnName
            val llTitle = createCategoryTitle(categoryName)
            llTitle.orientation = LinearLayout.HORIZONTAL
            llTitle.gravity = Gravity.CENTER_VERTICAL
            llTitle.tag = "${it.SubCategoryKey}_textview_tag"
            llTitle.isVisible = false
            val llTitleLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            llTitleLp.topMargin = Utils.dp2px(requireContext(), 13)
            llTitleLp.leftMargin = Utils.dp2px(requireContext(), 10)
            llTitleLp.rightMargin = Utils.dp2px(requireContext(), 10)
            mLlTitleMap[it.SubCategoryKey] = llTitle
            second_category_container.addView(llTitle, llTitleLp)
            val deviceGridView = RecyclerView(requireContext())
            deviceGridView.tag = "${it.SubCategoryKey}_gridview_tag"
            val gridAdapter = GridAdapter(onItemClickListener)
            deviceGridView.adapter = gridAdapter
            deviceGridView.layoutManager = GridLayoutManager(requireContext(), 3)
            deviceGridView.isVisible = false
            mGridAdapterMap[it.SubCategoryKey] = deviceGridView
            second_category_container.addView(deviceGridView)
        }
    }

    private fun createCategoryTitle(title: String) = LinearLayout(requireContext()).apply {
        val leftLine = View(requireContext())
        leftLine.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray_A1A7B2))
        val leftLineLp = LinearLayout.LayoutParams(0, 1)
        leftLineLp.weight = 1f
        addView(leftLine, leftLineLp)
        val tvTitle = TextView(requireContext())
        tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_A1A7B2))
        tvTitle.textSize = 12f
        tvTitle.text = title
        tvTitle.setPadding(
            Utils.dp2px(requireContext(), 10), 0, Utils.dp2px(requireContext(), 10), 0
        )
        addView(
            tvTitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        val rightLine = View(requireContext())
        rightLine.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray_A1A7B2))
        val rightLineLp = LinearLayout.LayoutParams(0, 1)
        rightLineLp.weight = 1f
        addView(rightLine, rightLineLp)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showCategoryDeviceInfo(infoList: List<CategoryDeviceInfo>) {
        val subCategoryKey = infoList.firstOrNull()?.SubCategoryKey
        if (subCategoryKey.isNullOrEmpty()) return
        val gridView = mGridAdapterMap[subCategoryKey]
        val deviceInfo = arrayListOf<CategoryDeviceInfo>()
        var isRefreshRecommend = false
        infoList.forEach {
            if (it.IsRecommend == 1) {
                isRefreshRecommend = true
                mRecommendDeviceList.add(it)
            } else {
                deviceInfo.add(it)
            }
        }
        if (deviceInfo.isEmpty()) {
            mLlTitleMap[subCategoryKey]?.isVisible = false
            gridView?.isVisible = false
        } else {
            mLlTitleMap[subCategoryKey]?.isVisible = true
            gridView?.isVisible = true
            val adapter = gridView?.adapter
            if (adapter is GridAdapter) {
                adapter.submitList(deviceInfo)
                adapter.notifyDataSetChanged()
            }
        }
        if (mRecommendDeviceList.isEmpty()) {
            mllRecommendTitle?.isVisible = false
            mRvRecommend?.isVisible = false
        } else if (isRefreshRecommend) {
            mllRecommendTitle?.isVisible = true
            mRvRecommend?.isVisible = true
            val adapter = mRvRecommend?.adapter
            if (adapter is GridAdapter) {
                adapter.submitList(mRecommendDeviceList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun startActivityWithExtra(cls: Class<*>?, productId: String) {
        val intent = Intent(context, cls)
        if (!TextUtils.isEmpty(productId)) {
            intent.putExtra(
                CommonField.LOAD_VIEW_TXT_TYPE,
                LoadViewTxtType.LoadRemoteViewTxt.ordinal
            )
            intent.putExtra(CommonField.PRODUCT_ID, productId)
        }
        startActivity(intent)
    }

    private val onItemClickListener = object : OnItemClickListener<CategoryDeviceInfo> {
        override fun onItemClick(position: Int, data: CategoryDeviceInfo) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                clickDeviceInfo = data
                // 查看请求location权限的时间是否大于48小时
                var locationJsonString = Utils.getStringValueFromXml(
                    T.getContext(),
                    CommonField.PERMISSION_LOCATION,
                    CommonField.PERMISSION_LOCATION
                )
                var locationJson: JSONObject? = JSONObject.parse(locationJsonString) as JSONObject?
                val lasttime = locationJson?.getLong(CommonField.PERMISSION_LOCATION)
                if (lasttime != null && lasttime > 0 && System.currentTimeMillis() / 1000 - lasttime < 48 * 60 * 60) {
                    T.show(getString(R.string.permission_of_location_add_device_refuse))
                    return
                }
                permissionDialog = PermissionDialog(
                    App.activity,
                    R.mipmap.permission_location,
                    getString(R.string.permission_location_lips),
                    getString(R.string.permission_location_ssid)
                )
                permissionDialog!!.show()
                requestPermissions(permissions, 1)

                // 记录请求location权限的时间
                val json = JSONObject()
                json[CommonField.PERMISSION_LOCATION] = System.currentTimeMillis() / 1000
                Utils.setXmlStringValue(
                    T.getContext(),
                    CommonField.PERMISSION_LOCATION,
                    CommonField.PERMISSION_LOCATION,
                    json.toJSONString()
                )

            } else {
                if (data.IsRelatedProduct){// 根据推荐设备的配网方式，跳转到SmartConfig或者SoftAp配网界面
                    if (goBleH5Presenter != null) {
                        conditionPrefix = false
                        goBleH5Presenter!!.checkProductConfig(data.ProductId)
                    } else {
                        HttpRequest.instance.getProductsConfig(
                            listOf(data.ProductId), this@SecondDeviceCategoryFragment
                        )
                    }
                }else{
                    startActivityWithExtra(SmartConfigStepActivity::class.java, "")
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
            if (clickDeviceInfo == null) {
                return
            } else if (clickDeviceInfo!!.IsRelatedProduct) {
                if (goBleH5Presenter != null) {
                    conditionPrefix = false
                    goBleH5Presenter!!.checkProductConfig(clickDeviceInfo!!.ProductId)
                } else {
                    HttpRequest.instance.getProductsConfig(
                        listOf(clickDeviceInfo!!.ProductId), this@SecondDeviceCategoryFragment
                    )
                }
            } else {
                jumpActivity(SmartConfigStepActivity::class.java)
            }
        }
        permissionDialog?.dismiss()
        permissionDialog = null
    }

    class GridAdapter(private val itemClickListener: OnItemClickListener<CategoryDeviceInfo>) :
        ListAdapter<CategoryDeviceInfo, GridAdapter.GridViewHolder>(
            ItemCallback()
        ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
            return GridViewHolder(itemView)
        }

        override fun onBindViewHolder(
            holder: GridViewHolder, position: Int
        ) {
            holder.bind(getItem(position), itemClickListener)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        inner class GridViewHolder : ViewHolder {
            var image: ImageView? = null
            var text: TextView? = null

            constructor(itemView: View) : super(itemView) {
                image = itemView.findViewById(R.id.iv_device_icon)
                text = itemView.findViewById(R.id.tv_device_name)
            }

            fun bind(
                entity: CategoryDeviceInfo,
                itemClickListener: OnItemClickListener<CategoryDeviceInfo>
            ) {
                itemView.setOnClickListener {
                    itemClickListener.onItemClick(layoutPosition, entity)
                }
                text?.text = entity.ProductName
                val url = entity.ProductUrl
                if (TextUtils.isEmpty(url)) {
                    Picasso.get().load(R.mipmap.device_placeholder)
                        .placeholder(R.mipmap.device_placeholder)
                        .resize(App.data.screenWith / 5, App.data.screenWith / 5).centerCrop()
                        .into(image)
                } else {
                    Picasso.get().load(url).placeholder(R.mipmap.device_placeholder)
                        .resize(App.data.screenWith / 5, App.data.screenWith / 5).centerCrop()
                        .into(image)
                }
            }
        }

        internal class ItemCallback : DiffUtil.ItemCallback<CategoryDeviceInfo>() {
            override fun areItemsTheSame(
                oldItem: CategoryDeviceInfo,
                newItem: CategoryDeviceInfo
            ): Boolean {
                return oldItem == newItem
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: CategoryDeviceInfo,
                newItem: CategoryDeviceInfo
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    interface OnItemClickListener<in T> {
        fun onItemClick(position: Int, data: T)
    }
}