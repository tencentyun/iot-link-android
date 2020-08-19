package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.MySideBarView
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.TimeZoneEntity
import com.tencent.iot.explorer.link.kitlink.holder.TimeZoneViewHolder
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.JsonManager
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.util.T
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
    MySideBarView.OnTouchingLetterChangedListener, CRecyclerView.RecyclerItemView, MyCallback {

    private var timeZoneList = ArrayList<TimeZoneEntity>()
    private var touchPosition = -1

    private fun getTimeZoneList() {
        if (this.resources.configuration.locale == Locale.SIMPLIFIED_CHINESE) {// 中文
            HttpRequest.instance.getGlobalConfig(CommonField.REGION_LIST_CN, this)
        } else {// 外文
            HttpRequest.instance.getGlobalConfig(CommonField.REGION_LIST_EN, this)
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
    }

    override fun onTouchingLetterChanged(key: String?, position: Int) {
        this.touchPosition = position
        tv_show_key.text = key
        tv_show_key.visibility = View.VISIBLE
        run outSide@{
            timeZoneList.forEachIndexed { index, entity ->
                crv_time_zone.scrollPosition(index)
                return@outSide
            }
        }
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return TimeZoneViewHolder(
            LayoutInflater.from(this)
                .inflate(R.layout.item_country_code_value, parent, false))
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        val entity = timeZoneList[position]
        val intent = Intent()
        intent.putExtra(CommonField.TIME_ZONE, "${entity.TZ}${entity.Title}")
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.get_global_config -> {
                if (response.isSuccess()) {
                    var json = JSONObject(response.data.toString())
                    val configs = json.getJSONArray("Configs")
                    json = JSONObject(configs[0].toString())
                    val timezoneArray = json.getString("Value")
                    val list = JsonManager.parseJsonArray(timezoneArray, TimeZoneEntity::class.java)
                    timeZoneList.addAll(list)
                    crv_time_zone.notifyDataChanged()
                }
            }
        }
    }
}