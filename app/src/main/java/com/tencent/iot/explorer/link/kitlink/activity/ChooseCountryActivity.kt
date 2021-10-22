package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.customview.dialog.InputBirthdayDialog
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ChooseCountryPresenter
import com.tencent.iot.explorer.link.mvp.view.ChooseCountryView
import kotlinx.android.synthetic.main.activity_choose_country.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.layout_email_register.view.*
import kotlinx.android.synthetic.main.layout_phone_register.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.util.*

class ChooseCountryActivity : PActivity(), ChooseCountryView, View.OnClickListener {

    private lateinit var presenter: ChooseCountryPresenter

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_choose_country
    }

    override fun initView() {
        tv_title.text = getString(R.string.country_or_place)
        presenter = ChooseCountryPresenter(this)
        loadLastCountryInfo()
        showBirthDayDlg()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_register_to_country.setOnClickListener(this)
        btn_bind_get_code.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_register_to_country -> {
                startActivityForResult(Intent(this, RegionActivity::class.java), 100)
            }
            btn_bind_get_code -> {
                Intent(this, RegisterActivity::class.java).run {
                    startActivity(this)
                }
            }
        }
    }

    override fun showCountryCode(countryCode: String, countryName: String) {
        tv_register_to_country.text = countryName + getString(R.string.conutry_code_num, countryCode)
    }

    private fun shouldShowBirthdayDlg(): Boolean {
        var lastTimeJson = Utils.getStringValueFromXml(this@ChooseCountryActivity, CommonField.USA_USER_REG_TIME_INFO, CommonField.USA_USER_REG_TIME_INFO)
        // 不存在上一次的注册信息
        if (TextUtils.isEmpty(lastTimeJson) || lastTimeJson == "{}") return true

        var json = JSONObject.parseObject(lastTimeJson)
        var currentDate = Date()
        var currentYear = currentDate.year + 1900
        var currentMonth = currentDate.month + 1
        var currentDay = currentDate.day
        var tagYear = json.getIntValue(CommonField.USA_USER_REG_TIME_INFO_YEAR)
        var tagMonth = json.getIntValue(CommonField.USA_USER_REG_TIME_INFO_MONTH)
        var tagDay = json.getIntValue(CommonField.USA_USER_REG_TIME_INFO_DAY)
        if (currentYear - tagYear > 0 && currentMonth - tagMonth == 0 && currentDay - tagDay == 0) { // 满周年
            return true
        }

        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            data?.let {
                it.getStringExtra(CommonField.REGION_ID)?.run {
                    presenter.setCountry(this)
                    Utils.setXmlStringValue(T.getContext(), CommonField.REG_COUNTRY_INFO, CommonField.REG_COUNTRY_INFO, this)
                    showBirthDayDlg()
                }
            }
        }
    }

    private fun showBirthDayDlg() {
        if (presenter.getCountryCode() == "1" && shouldShowBirthdayDlg()) {
            var dlg = InputBirthdayDialog(this@ChooseCountryActivity)
            dlg.show()
            dlg.setOnDismissListener(object: InputBirthdayDialog.OnDismisListener {
                override fun onOkClicked(year: Int, month: Int, day: Int) {

                    // 是否满13周岁
                    if (!ifOver13YearsOld(year, month, day)) {
                        T.show(resources.getString(R.string.too_young_to_use))
                        finish()
                        return
                    }

                    var timeJson = JSONObject()
                    var currentDate = Date()
                    var currentYear = currentDate.year + 1900
                    var currentMonth = currentDate.month + 1
                    var currentDay = currentDate.day
                    // 记录本次使用的日期
                    timeJson.put(CommonField.USA_USER_REG_TIME_INFO_YEAR, currentYear)
                    timeJson.put(CommonField.USA_USER_REG_TIME_INFO_MONTH, currentMonth)
                    timeJson.put(CommonField.USA_USER_REG_TIME_INFO_DAY, currentDay)
                    Utils.setXmlStringValue(T.getContext(), CommonField.USA_USER_REG_TIME_INFO,
                        CommonField.USA_USER_REG_TIME_INFO, timeJson.toJSONString())
                }

                override fun onCancelClicked() { finish() }
            })
        }
    }

    private fun ifOver13YearsOld(year: Int, month: Int, day: Int): Boolean {
        var currentDate = Date()
        var currentYear = currentDate.year + 1900
        var currentMonth = currentDate.month + 1
        var currentDay = currentDate.day
        if (currentYear - year < 13 || (currentYear - year == 13 && currentMonth - month < 0) ||
            (currentYear - year == 13 && currentMonth - month == 0 && currentDay - day < 0)) {
            return false
        }
        return true
    }

    private fun loadLastCountryInfo() {
        var countryInfo = Utils.getStringValueFromXml(T.getContext(), CommonField.REG_COUNTRY_INFO, CommonField.REG_COUNTRY_INFO)
        if (TextUtils.isEmpty(countryInfo)) return

        presenter.setCountry(countryInfo!!)
    }

}