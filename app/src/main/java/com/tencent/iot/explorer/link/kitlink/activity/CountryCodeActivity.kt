package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.PropertyListParser
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.CountryCodeEntity
import com.tencent.iot.explorer.link.kitlink.holder.CountryCodeKeyViewHolder
import com.tencent.iot.explorer.link.kitlink.holder.CountryCodeViewHolder
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.customview.MySideBarView
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.activity_country_code.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 选择国家地区
 */
class CountryCodeActivity : PActivity(),
    MySideBarView.OnTouchingLetterChangedListener, CRecyclerView.RecyclerItemView {

    private val countryCodeList = ArrayList<Any>()
    private var touchPosition = -1

    override fun getContentView(): Int {
        return R.layout.activity_country_code
    }

    override fun initView() {
        sbhv_country_code.setBackgroundColor(Color.WHITE)
        tv_title.text = getString(R.string.country_code)
        crv_country_code.setList(countryCodeList)
        crv_country_code.addRecyclerItemView(this)
        my_side_bar.setTextView(tv_show_key)
        my_side_bar.context = this
        parseList()
    }

    private fun parseList() {
        val nsDictionary =
            PropertyListParser.parse(assets.open("sortedNameCH.plist")) as? NSDictionary
        nsDictionary?.let {
            val dict = nsDictionary.allKeys()
            dict.sortedArray().forEachIndexed { _, key ->
                if (TextUtils.isEmpty(key))
                    countryCodeList.add(getString(R.string.hot_country))
                else
                    countryCodeList.add(key)
                val nsArray = nsDictionary[key] as NSArray
                nsArray.array.forEachIndexed { _, nsObject ->
                    val a = nsObject.toJavaObject().toString().split(" ")
                    countryCodeList.add(CountryCodeEntity(a[0], a[1]))
                }
            }
        }
    }

    override fun getViewType(position: Int): Int {
        return if (countryCodeList[position] is CountryCodeEntity) {
            0
        } else {
            1
        }
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        when (viewType) {
            0 -> {
                return CountryCodeViewHolder(
                    LayoutInflater.from(this)
                        .inflate(R.layout.item_country_code_value, parent, false)
                )
            }
            else -> {
                return CountryCodeKeyViewHolder(
                    LayoutInflater.from(this)
                        .inflate(R.layout.item_country_code_key, parent, false)
                )
            }
        }
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        val entity = countryCodeList[position]
        if (entity is CountryCodeEntity) {
            val intent = Intent()
            intent.putExtra(CommonField.COUNTRY_CODE, "${entity.countryName}${entity.countryCode}")
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        my_side_bar.setOnTouchingLetterChangedListener(this)
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun onTouchingLetterChanged(key: String, position: Int) {
        this.touchPosition = position
        tv_show_key.text = key
        tv_show_key.visibility = View.VISIBLE
        run outSide@{
            countryCodeList.forEachIndexed { index, entity ->
                if (entity is String && entity.toString() == key) {
                    crv_country_code.scrollPosition(index)
                    return@outSide
                }
            }
        }

    }

}
