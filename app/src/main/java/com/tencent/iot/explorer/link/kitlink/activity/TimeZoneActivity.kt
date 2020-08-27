package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.MySideBarView
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.CountryCodeEntity
import com.tencent.iot.explorer.link.kitlink.entity.TimeZoneEntity
import com.tencent.iot.explorer.link.kitlink.holder.CountryCodeKeyViewHolder
import com.tencent.iot.explorer.link.kitlink.holder.CountryCodeViewHolder
import com.tencent.iot.explorer.link.kitlink.holder.TimeZoneKeyViewHolder
import com.tencent.iot.explorer.link.kitlink.holder.TimeZoneViewHolder
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.*
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_country_code.*
import kotlinx.android.synthetic.main.activity_time_zone.*
import kotlinx.android.synthetic.main.activity_time_zone.my_side_bar
import kotlinx.android.synthetic.main.activity_time_zone.tv_show_key
import kotlinx.android.synthetic.main.menu_back_layout.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


/**
 * 选择时区
 */
class TimeZoneActivity: PActivity(),
    MySideBarView.OnTouchingLetterChangedListener, CRecyclerView.RecyclerItemView, MyCallback,
    SearchView.OnQueryTextListener{

    private var timeZoneList = ArrayList<TimeZoneEntity>()
    private var touchPosition = -1
    private var flags = IntArray(26)

    private fun getTimeZoneList() {
        if (CommonUtils.isChineseSystem()) {// 中文
            HttpRequest.instance.getGlobalConfig(CommonField.REGION_LIST_CN, this)
            saveLanguage(CommonField.CHINESE)
        } else {// 外文
            HttpRequest.instance.getGlobalConfig(CommonField.REGION_LIST_EN, this)
            saveLanguage(CommonField.ENGLISH)
        }
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.activity_time_zone
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.setTextColor(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.select_timezone)
        crv_time_zone.setList(timeZoneList)
        crv_time_zone.addRecyclerItemView(this)
        my_side_bar.setTextView(tv_show_key)
        my_side_bar.context = this
        getTimeZoneList()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        my_side_bar.setOnTouchingLetterChangedListener(this)
        search_view.setOnQueryTextListener(this)
    }

    override fun onTouchingLetterChanged(key: String?, position: Int) {
        this.touchPosition = position
        tv_show_key.text = key
        tv_show_key.visibility = View.VISIBLE
        run outSide@{
            timeZoneList.forEachIndexed { index, entity ->
                if (entity.TZ.startsWith(key.toString())) {
                    crv_time_zone.scrollPosition(index)
                    return@outSide
                }
            }
        }
    }

    override fun getViewType(position: Int): Int {
        return if (!TextUtils.isEmpty(timeZoneList[position].TZ)) {
            0
        } else {
            1
        }
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        when (viewType) {
            0 -> {
                return TimeZoneViewHolder(
                    LayoutInflater.from(this)
                        .inflate(R.layout.item_time_zone, parent, false)
                )
            }
            else -> {
                return TimeZoneKeyViewHolder(
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
        val entity = timeZoneList[position]
        HttpRequest.instance.setRegion(entity.TZ, this)
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
                    crv_time_zone.notifyDataChanged()
                }
            }
            RequestCode.set_region -> {// 设置时区
                if (response.isSuccess()) { }
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        val tempList = ArrayList<TimeZoneEntity>()
        tempList.addAll(timeZoneList)
        if (!TextUtils.isEmpty(query)){
            timeZoneList.clear()
            tempList.forEach {
                if (it.TZ.toLowerCase(Locale.ROOT).contains(query.toString().toLowerCase(Locale.ROOT)))
                    timeZoneList.add(it)
            }
        } else {
            crv_time_zone.clearFocus()
            timeZoneList.addAll(tempList)
        }
        crv_time_zone.notifyDataChanged()
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

    private fun parseJson(jsonStr: String) {
        var json = JSONObject(jsonStr)
        val configs = json.getJSONArray("Configs")
        json = JSONObject(configs[0].toString())
        val timezoneArray = json.getString("Value")
        val list = JsonManager.parseJsonArray(timezoneArray, TimeZoneEntity::class.java)
        val tempList = ArrayList<TimeZoneEntity>()
        tempList.addAll(list)
        tempList.sort()
        tempList.forEach {
            val index = it.TZ[0] - 'A'
            if (flags[index] == 0) {
                val entity = TimeZoneEntity()
                entity.Title = it.TZ[0].toString()
                timeZoneList.add(entity)
                flags[index] = 1
            }
            timeZoneList.add(it)
        }
    }
}