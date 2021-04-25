package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.utils.KeyBoardUtils
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.customview.dialog.InputBirthdayDialog
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.consts.SocketConstants
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.RegisterPresenter
import com.tencent.iot.explorer.link.mvp.view.RegisterView
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.layout_email_register.view.*
import kotlinx.android.synthetic.main.layout_phone_register.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.util.*

/**
 * 手机号注册界面
 */
class RegisterActivity : PActivity(), RegisterView, View.OnClickListener {

    companion object {
        const val ACCOUNT_TYPE = "account_type"
        const val ACCOUNT_NUMBER = "account_number"
    }

    private lateinit var presenter: RegisterPresenter
    //true是手机注册，false是邮箱
    private var registerType = true

    private lateinit var phoneView: View
    private lateinit var emailView: View

    private val ANDROID_ID = Utils.getAndroidID(T.getContext())

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_register
    }

    override fun initView() {
        App.data.regionId = "1"
        App.data.region = "ap-guangzhou"
        presenter = RegisterPresenter(this)
        btn_register_get_code.setRegisterPresenter(presenter)
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.mobile_phone_register)
        initViewPager()
        intent.getBooleanExtra(ACCOUNT_TYPE, true).let {
            registerType = it
            when (registerType) {
                true -> showPhoneRegister()
                false -> showEmailRegister()
            }
        }
        intent?.let {
            val account = intent?.getStringExtra(ACCOUNT_NUMBER)?:""
            if (!TextUtils.isEmpty(account)) {
                if (account.contains("@"))
                    emailView.et_register_email.setText(account)
                else
                    phoneView.et_register_phone.setText(account)
            }
        }
        if (presenter.isAgreement()) {
            iv_register_agreement.setImageResource(R.mipmap.readed)
            iv_register_agreement_status.visibility = View.VISIBLE
        } else {
            iv_register_agreement.setImageResource(R.mipmap.icon_unselected)
            iv_register_agreement_status.visibility = View.GONE
        }

        phoneView.tv_register_to_country.text = getString(R.string.country_china) + getString(R.string.conutry_code_num, presenter.getCountryCode())
        emailView.tv_register_to_country_email.text = getString(R.string.country_china) + getString(R.string.conutry_code_num, presenter.getCountryCode())

        loadLastCountryInfo()
        showBirthDayDlg()
        formatTipText()
    }

    private fun formatTipText() {
        val str = resources.getString(R.string.register_agree_1)
        val partStr1 = resources.getString(R.string.register_agree_2)
        val partStr2 = resources.getString(R.string.register_agree_3)
        val partStr3 = resources.getString(R.string.register_agree_4)
        var showStr = str + partStr1 + partStr2 + partStr3
        val spannable = SpannableStringBuilder(showStr)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@RegisterActivity, WebActivity::class.java)
                intent.putExtra(CommonField.EXTRA_TITLE, getString(R.string.register_agree_2))
                var url = CommonField.POLICY_PREFIX
                url += "?uin=$ANDROID_ID"
                url += CommonField.SERVICE_POLICY_SUFFIX
                intent.putExtra(CommonField.EXTRA_TEXT, url)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = resources.getColor(R.color.blue_0066FF)
                ds.setUnderlineText(false);
            }
        },
            str.length, str.length + partStr1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@RegisterActivity, WebActivity::class.java)
                intent.putExtra(CommonField.EXTRA_TITLE, getString(R.string.register_agree_4))
                var url = CommonField.POLICY_PREFIX
                url += "?uin=$ANDROID_ID"
                url += CommonField.PRIVACY_POLICY_SUFFIX
                intent.putExtra(CommonField.EXTRA_TEXT, url)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = resources.getColor(R.color.blue_0066FF)
                ds.setUnderlineText(false);
            }

        },
            showStr.length - partStr1.length, showStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        tv_register_tip.setMovementMethod(LinkMovementMethod.getInstance())
        tv_register_tip.setText(spannable)
    }

    private fun initViewPager() {
        phoneView = LayoutInflater.from(this).inflate(R.layout.layout_phone_register, null)
        emailView = LayoutInflater.from(this).inflate(R.layout.layout_email_register, null)
        phoneView.et_register_phone.addClearImage(phoneView.iv_register_phone_clear)
        emailView.et_register_email.addClearImage(emailView.iv_register_email_clear)
        vp_register.addViewToList(phoneView)
        vp_register.addViewToList(emailView)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        register.setOnClickListener {
            KeyBoardUtils.hideKeyBoard(
                this,
                phoneView.et_register_phone
            )
        }

        phoneView.tv_register_to_email.setOnClickListener(this)
        emailView.tv_register_to_phone.setOnClickListener(this)

        phoneView.tv_register_to_country.setOnClickListener(this)
        phoneView.iv_register_to_country.setOnClickListener(this)

        emailView.tv_register_to_country_email.setOnClickListener(this)
        emailView.iv_register_to_country_email.setOnClickListener(this)

        iv_register_agreement.setOnClickListener(this)
        btn_register_get_code.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            btn_register_get_code -> {//获取手机验证码
                when (registerType) {
                    true -> {
                        presenter.setMobilePhone(phoneView.et_register_phone.text.trim().toString())
                        presenter.requestPhoneCode()
                    }
                    false -> {
                        presenter.setEmailAddress(emailView.et_register_email.text.trim().toString())
                        presenter.requestEmailCode()
                    }
                }
            }
            phoneView.tv_register_to_email -> {//显示邮箱注册界面
                registerType = false
                showEmailRegister()
            }
            emailView.tv_register_to_phone -> {//显示手机号注册界面
                registerType = true
                showPhoneRegister()
            }
            iv_register_agreement -> {//同意或不同意协议
                presenter.agreement()
            }
            phoneView.tv_register_to_country, phoneView.iv_register_to_country -> {
                startActivityForResult(Intent(this, RegionActivity::class.java), 100)
            }

            emailView.tv_register_to_country_email, emailView.iv_register_to_country_email -> {
                startActivityForResult(Intent(this, RegionActivity::class.java), 100)
            }
        }
    }

    private fun showPhoneRegister() {
        tv_title.text = getString(R.string.mobile_phone_register)
        vp_register.setCurrentItem(0, true)
        btn_register_get_code.removeEditText(emailView.et_register_email)
        btn_register_get_code.addEditText(
            phoneView.et_register_phone,
            phoneView.tv_register_phone_hint,
            presenter.getCountryCode()
        )
    }

    private fun showEmailRegister() {
        tv_title.text = getString(R.string.email_register)
        vp_register.setCurrentItem(1, true)
        btn_register_get_code.removeEditText(phoneView.et_register_phone)
        btn_register_get_code.addEditText(
            emailView.et_register_email,
            emailView.tv_register_email_hint,
            "email"
        )
    }

    /**
     * 手机验证码发送成功后跳转到验证界面
     */
    override fun sendSmsCodeSuccess() {
        val intent = Intent(this, GetCodeActivity::class.java)
        intent.putExtra(GetCodeActivity.TYPE, SocketConstants.register)
        intent.putExtra(GetCodeActivity.COUNTRY_CODE, presenter.getCountryCode())
        intent.putExtra(GetCodeActivity.PHONE, presenter.model?.phone)
        intent.putExtra(SetPasswordActivity.ACTION, SetPasswordActivity.REGISTER_PHONE)
        startActivity(intent)
    }

    override fun sendEmailCodeSuccess() {
        val intent = Intent(this, GetCodeActivity::class.java)
        intent.putExtra(GetCodeActivity.TYPE, SocketConstants.register)
        intent.putExtra(GetCodeActivity.EMAIL, presenter.model?.email)
        intent.putExtra(SetPasswordActivity.ACTION, SetPasswordActivity.REGISTER_EMAIL)
        startActivity(intent)
    }

    override fun sendCodeFail(msg: String) {
        T.show(msg)
    }

    override fun agreement(isAgree: Boolean) {
        iv_register_agreement.setImageResource(
            if (isAgree) {
                R.mipmap.readed
            } else {
                R.mipmap.icon_unselected
            }
        )
        if (isAgree) {
            iv_register_agreement_status.visibility = View.VISIBLE
        } else {
            iv_register_agreement_status.visibility = View.GONE
        }
        btn_register_get_code.checkStatus()
    }

    override fun unselectedAgreement() {
        T.show(getString(R.string.toast_register_agreement))
    }

    override fun showCountryCode(countryCode: String, countryName: String) {
        phoneView.tv_register_to_country.text = countryName + getString(R.string.conutry_code_num, countryCode)
        emailView.tv_register_to_country_email.text = countryName + getString(R.string.conutry_code_num, countryCode)
        btn_register_get_code.changeType(phoneView.et_register_phone, presenter.getCountryCode())
    }

    private fun shouldShowBirthdayDlg(): Boolean {
        var lastTimeJson = Utils.getStringValueFromXml(this@RegisterActivity, CommonField.USA_USER_REG_TIME_INFO, CommonField.USA_USER_REG_TIME_INFO)
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
            var dlg = InputBirthdayDialog(this@RegisterActivity)
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
