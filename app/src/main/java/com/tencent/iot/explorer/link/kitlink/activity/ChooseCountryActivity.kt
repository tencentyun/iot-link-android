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
//        loadLastCountryInfo()
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

                if (tv_register_to_country.text == resources.getString(R.string.please_choose)) {
                    T.show(resources.getString(R.string.please_choose) + resources.getString(R.string.country_or_place))
                    return
                }
                val countryCode = presenter.getCountryCode()
                var lastTimeJson: String?
                if (countryCode == "1") {
                    lastTimeJson = Utils.getStringValueFromXml(this@ChooseCountryActivity, CommonField.USA_USER_REG_TIME_INFO, CommonField.USA_USER_REG_TIME_INFO)
                } else if (countryCode == "86") {
                    lastTimeJson = Utils.getStringValueFromXml(this@ChooseCountryActivity, CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO, CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO)
                } else {
                    return
                }

                // 不存在上一次的注册信息
                if (TextUtils.isEmpty(lastTimeJson) || lastTimeJson == "{}")  {
                    T.show(resources.getString(R.string.please_choose) + resources.getString(R.string.country_or_place))
                    return
                }
                var json = JSONObject.parseObject(lastTimeJson)

                var tagYear = 0
                var tagMonth = 0
                var tagDay = 0
                if (countryCode == "1") {
                    tagYear = json.getIntValue(CommonField.USA_USER_REG_TIME_INFO_YEAR)
                    tagMonth = json.getIntValue(CommonField.USA_USER_REG_TIME_INFO_MONTH)
                    tagDay = json.getIntValue(CommonField.USA_USER_REG_TIME_INFO_DAY)
                } else if (countryCode == "86") {
                    tagYear = json.getIntValue(CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO_YEAR)
                    tagMonth = json.getIntValue(CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO_MONTH)
                    tagDay = json.getIntValue(CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO_DAY)
                }

                // 是否满age周岁
                if (!ifOverAge(countryCode, tagYear, tagMonth, tagDay)) {
                    if (countryCode == "1") {
                        T.show(resources.getString(R.string.usa_too_young_to_use))
                    } else if (countryCode == "86") {
                        T.show(resources.getString(R.string.mainland_too_young_to_use))
                    }
                    return
                }

                Intent(this, RegisterActivity::class.java).run {
                    startActivity(this)
                }
            }
        }
    }

    override fun showCountryCode(countryCode: String, countryName: String) {
        tv_register_to_country.text = countryName + getString(R.string.conutry_code_num, countryCode)
    }

    private fun shouldShowBirthdayDlg(countryCode: String): Boolean {
        var lastTimeJson: String?
        if (countryCode == "1") {
            lastTimeJson = Utils.getStringValueFromXml(this@ChooseCountryActivity, CommonField.USA_USER_REG_TIME_INFO, CommonField.USA_USER_REG_TIME_INFO)
        } else if (countryCode == "86") {
            lastTimeJson = Utils.getStringValueFromXml(this@ChooseCountryActivity, CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO, CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO)
        } else {
            return false
        }

        // 不存在上一次的注册信息
        if (TextUtils.isEmpty(lastTimeJson) || lastTimeJson == "{}") return true

        var json = JSONObject.parseObject(lastTimeJson)
        var tagYear = 0
        var tagMonth = 0
        var tagDay = 0
        if (countryCode == "1") {
            tagYear = json.getIntValue(CommonField.USA_USER_REG_TIME_INFO_YEAR)
            tagMonth = json.getIntValue(CommonField.USA_USER_REG_TIME_INFO_MONTH)
            tagDay = json.getIntValue(CommonField.USA_USER_REG_TIME_INFO_DAY)
        } else if (countryCode == "86") {
            tagYear = json.getIntValue(CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO_YEAR)
            tagMonth = json.getIntValue(CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO_MONTH)
            tagDay = json.getIntValue(CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO_DAY)
        }

        if (!ifOverAge(countryCode, tagYear, tagMonth, tagDay)) {
            if (countryCode == "1") {
                T.show(resources.getString(R.string.usa_too_young_to_use))
            } else if (countryCode == "86") {
                T.show(resources.getString(R.string.mainland_too_young_to_use))
            }
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
        val countryCode = presenter.getCountryCode()
        if (shouldShowBirthdayDlg(countryCode)) {
            var dlg = InputBirthdayDialog(this@ChooseCountryActivity, countryCode)
            dlg.show()
            dlg.setOnDismissListener(object: InputBirthdayDialog.OnDismisListener {
                override fun onOkClicked(year: Int, month: Int, day: Int) {

                    // 是否满age周岁
                    if (!ifOverAge(countryCode, year, month, day)) {
                        if (countryCode == "1") {
                            T.show(resources.getString(R.string.usa_too_young_to_use))
                        } else if (countryCode == "86") {
                            T.show(resources.getString(R.string.mainland_too_young_to_use))
                        }
                    }

                    var timeJson = JSONObject()

                    if (countryCode == "1") {
                        // 记录本次使用的日期
                        timeJson.put(CommonField.USA_USER_REG_TIME_INFO_YEAR, year)
                        timeJson.put(CommonField.USA_USER_REG_TIME_INFO_MONTH, month)
                        timeJson.put(CommonField.USA_USER_REG_TIME_INFO_DAY, day)
                        Utils.setXmlStringValue(T.getContext(), CommonField.USA_USER_REG_TIME_INFO,
                            CommonField.USA_USER_REG_TIME_INFO, timeJson.toJSONString())
                    } else if (countryCode == "86") {
                        // 记录本次使用的日期
                        timeJson.put(CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO_YEAR, year)
                        timeJson.put(CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO_MONTH, month)
                        timeJson.put(CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO_DAY, day)
                        Utils.setXmlStringValue(T.getContext(), CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO,
                            CommonField.CHINA_MAINLAND_USER_REG_TIME_INFO, timeJson.toJSONString())
                    }
                }

                override fun onCancelClicked() { finish() }
            })
        }
    }

    private fun ifOverAge(countryCode: String, year: Int, month: Int, day: Int): Boolean {

        var age = 0
        if (countryCode == "1") {
            age = 13
        } else if (countryCode == "86") {
            age = 18
        }

        var currentDate = Date()
        var currentYear = currentDate.year + 1900
        var currentMonth = currentDate.month + 1
        var currentDay = currentDate.day
        if (currentYear - year < age || (currentYear - year == age && currentMonth - month < 0) ||
            (currentYear - year == age && currentMonth - month == 0 && currentDay - day < 0)) {
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