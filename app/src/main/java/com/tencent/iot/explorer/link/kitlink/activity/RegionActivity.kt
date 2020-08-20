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
import com.tencent.iot.explorer.link.kitlink.util.*
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
    MySideBarView.OnTouchingLetterChangedListener, CRecyclerView.RecyclerItemView, MyCustomCallBack {

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
                    crv_region.scrollPosition(index)
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
        return when (viewType) {
            0 -> {
                RegionViewHolder(
                    LayoutInflater.from(this)
                        .inflate(R.layout.item_time_zone, parent, false)
                )
            }
            else -> {
                RegionKeyViewHolder(
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
        intent.putExtra(CommonField.REGION_ID, "${entity.Title}+${entity.RegionID}+${entity.CountryCode}+${entity.Region}")
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(str: String, reqCode: Int) {
        when (reqCode) {
            RequestCode.get_region_list -> {// 拉取时区列表
                parseJson(str)
                crv_region.notifyDataChanged()
            }
        }
    }

    private fun getRegionList() {
        HttpRequest.instance.getRegionList(CommonField.REGION_LIST_URL, this, RequestCode.get_region_list)
    }

    private fun parseJson(str: String) {
        val start = str.indexOf('[')
        val end = str.indexOf(']')
        val regionArray = str.substring(start, end + 1)
        val list = JsonManager.parseJsonArray(regionArray, RegionEntity::class.java)
        val tempList = ArrayList<RegionEntity>()
        tempList.addAll(list)
        tempList.sort()
        tempList.forEach {
            val index: Int
            val firstLetter: String
            if (!CommonUtils.isChineseSystem()) {// 英文
                index = it.TitleEN[0] - 'A'
                firstLetter = it.TitleEN[0].toString()
                it.Title = it.TitleEN
            } else {// 中文
                firstLetter = PinyinUtil.getFirstLetter(it.Title[0])?.toUpperCase(Locale.ROOT)!!
                index = firstLetter[0] - 'A'
            }
            if (flags[index] == 0) {
                val entity = RegionEntity()
                entity.Title = firstLetter
                regionList.add(entity)
                flags[index] = 1
            }
            regionList.add(it)
        }
    }
}