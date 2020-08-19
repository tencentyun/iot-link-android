package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.MySideBarView
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.RegionEntity
import com.tencent.iot.explorer.link.kitlink.entity.TimeZoneEntity
import com.tencent.iot.explorer.link.kitlink.holder.RegionKeyViewHolder
import com.tencent.iot.explorer.link.kitlink.holder.RegionViewHolder
import com.tencent.iot.explorer.link.kitlink.holder.TimeZoneKeyViewHolder
import com.tencent.iot.explorer.link.kitlink.holder.TimeZoneViewHolder
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.JsonManager
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_region.*
import kotlinx.android.synthetic.main.activity_time_zone.*
import kotlinx.android.synthetic.main.activity_time_zone.my_side_bar
import kotlinx.android.synthetic.main.activity_time_zone.tv_show_key
import kotlinx.android.synthetic.main.menu_back_layout.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class RegionActivity: PActivity(),
    MySideBarView.OnTouchingLetterChangedListener, CRecyclerView.RecyclerItemView, MyCallback {

    private var regionList = ArrayList<RegionEntity>()
    private var flags = IntArray(26)

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.activity_region
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.setTextColor(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.country_code)
        crv_region.setList(regionList)
        crv_region.addRecyclerItemView(this)
        my_side_bar.setTextView(tv_show_key)
        my_side_bar.context = this
        getRegionList()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        my_side_bar.setOnTouchingLetterChangedListener(this)
    }

    override fun onTouchingLetterChanged(key: String?, position: Int) {
        tv_show_key.text = key
        tv_show_key.visibility = View.VISIBLE
        run outSide@{
            regionList.forEachIndexed { index, entity ->
                if (entity.Region.startsWith(key.toString().toLowerCase(Locale.ROOT))) {
                    crv_time_zone.scrollPosition(index)
                    return@outSide
                }
            }
        }
    }

    override fun getViewType(position: Int): Int {
        return if (!TextUtils.isEmpty(regionList[position].Region)) {
            0
        } else {
            1
        }
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        when (viewType) {
            0 -> {
                return RegionViewHolder(
                    LayoutInflater.from(this)
                        .inflate(R.layout.item_time_zone, parent, false)
                )
            }
            else -> {
                return RegionKeyViewHolder(
                    LayoutInflater.from(this)
                        .inflate(R.layout.item_time_zone_key, parent, false)
                )
            }
        }
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        val entity = regionList[position]
        val intent = Intent()
        intent.putExtra(CommonField.REGION_ID, "${entity.Title}+${entity.RegionID}+${entity.Region}")
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.get_global_config -> {// 拉取时区列表
                if (response.isSuccess()) {
                    parseJson(response.data.toString())
                    crv_region.notifyDataChanged()
                }
            }
        }
    }

    private fun getRegionList() {
        if (this.resources.configuration.locale == Locale.SIMPLIFIED_CHINESE) {// 中文
            HttpRequest.instance.getGlobalConfig(CommonField.REGISTER_REGION_LIST_CN, this)
            saveLanguage(CommonField.CHINESE)
        } else {// 外文
            HttpRequest.instance.getGlobalConfig(CommonField.REGISTER_REGION_LIST_EN, this)
            saveLanguage(CommonField.ENGLISH)
        }
    }

    private fun parseJson(jsonStr: String) {
        var json = JSONObject(jsonStr)
        val configs = json.getJSONArray("Configs")
        json = JSONObject(configs[0].toString())
        val regionArray = json.getString("Value")
        val list = JsonManager.parseJsonArray(regionArray, RegionEntity::class.java)
        val tempList = ArrayList<RegionEntity>()
        tempList.addAll(list)
        tempList.sort()
        tempList.forEach {
            val index = it.Region[0] - 'a'
            if (flags[index] == 0) {
                val entity = RegionEntity()
                entity.Title = it.Region[0].toString().toUpperCase(Locale.ROOT)
                regionList.add(entity)
                flags[index] = 1
            }
            regionList.add(it)
        }
    }
}