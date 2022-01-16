package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Handler
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.util.Weak
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.GetCodePresenter
import com.tencent.iot.explorer.link.mvp.view.GetCodeView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.utils.KeyBoardUtils
import com.tencent.iot.explorer.link.customview.dialog.PermissionDialog
import kotlinx.android.synthetic.main.activity_get_code.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 验证码验证界面
 */
class GetCodeActivity : PActivity(), GetCodeView {

    private lateinit var presenter: GetCodePresenter

    companion object {
        const val TYPE = "type"
        const val PHONE = "phone"
        const val EMAIL = "email"
        const val COUNTRY_CODE = "country_code"
    }

    private var permissionDialog: PermissionDialog? = null
    private val permissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )

    private var handler by Weak<Handler>()

    override fun getContentView(): Int {
        return R.layout.activity_get_code
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun initView() {
        if (!checkPermissions(permissions)) {
            // 查看请求sms权限的时间是否大于48小时
            var smsJsonString = Utils.getStringValueFromXml(T.getContext(), CommonField.PERMISSION_SMS, CommonField.PERMISSION_SMS)
            var smsJson: JSONObject? = JSONObject.parse(smsJsonString) as JSONObject?
            val lasttime = smsJson?.getLong(CommonField.PERMISSION_SMS)
            if (lasttime != null && lasttime > 0 && System.currentTimeMillis() / 1000 - lasttime < 48*60*60) {
                T.show(getString(R.string.permission_of_sms_refuse))
                return
            }
            permissionDialog = PermissionDialog(this@GetCodeActivity, R.mipmap.permission_sms, getString(R.string.permission_sms_lips), getString(R.string.permission_sms))
            permissionDialog!!.show()
            requestPermission(permissions)

            // 记录请求sms权限的时间
            var json = JSONObject()
            json.put(CommonField.PERMISSION_SMS, System.currentTimeMillis() / 1000)
            Utils.setXmlStringValue(T.getContext(), CommonField.PERMISSION_SMS, CommonField.PERMISSION_SMS, json.toJSONString())
        } else {
            permissionAllGranted()
        }
        presenter = GetCodePresenter(this)
        tv_title.text = getString(R.string.verification_code)
        presenter.lockResend()
        getInitData()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        //重新获取验证码
        tv_get_code_resend.setOnClickListener {
            presenter.resendCode()
        }
        //隐藏输入框
        get_code.setOnClickListener { KeyBoardUtils.hideKeyBoard(this, tv_get_code_resend) }
        //监听输入验证码
        vcv_get_code.setOnTextLengthListener { text: String ->
            if (text.length == 6) {
                presenter.setVerificationCode(text)
                presenter.next()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 102) {
            if (permissions.contains(Manifest.permission.READ_SMS)) {
                permissionDialog?.dismiss()
                permissionDialog = null
            }
        }
    }

    /**
     * 获得intent中的数据
     */
    private fun getInitData() {
        intent?.let {
            val action = it.getIntExtra(SetPasswordActivity.ACTION, -1)
            presenter.setCommonData(
                it.getStringExtra(TYPE),
                action
            )
            when (action) {
                SetPasswordActivity.REGISTER_PHONE, SetPasswordActivity.RESET_PWD_PHONE
                    , SetPasswordActivity.BIND_PHONE -> {
                    val countryCode = it.getStringExtra(COUNTRY_CODE)!!
                    val phone = it.getStringExtra(PHONE)!!
                    presenter.setCountryCode(countryCode)
                    presenter.setPhone(phone)
                    tv_get_code_show_account.text =
                        "${getString(R.string.get_mobile_code_sent)}$countryCode-$phone"
                }
                SetPasswordActivity.REGISTER_EMAIL, SetPasswordActivity.RESET_PWD_EMAIL -> {
                    val email = it.getStringExtra(EMAIL)!!
                    presenter.setEmail(email)
                    tv_get_code_show_account.text =
                        "${getString(R.string.get_email_code_sent)}$email"
                }
            }
        }
    }

    override fun checkVerificationCodeFail(message: String) {
        T.show(message)
    }

    override fun getCodeFail(message: String) {
        T.show(message)
    }

    override fun lockResendShow(time: Int) {
        tv_get_code_resend.setTextColor(getMyColor(R.color.black_888888))
        tv_get_code_resend.text = "${getString(R.string.resend)}($time)"
    }

    override fun unlock() {
        tv_get_code_resend.setTextColor(getMyColor(R.color.blue_006EFF))
        tv_get_code_resend.text = getString(R.string.resend)
    }

    override fun bindPhoneSuccess() {
        T.show(getString(R.string.bind_phone_success))
        finish()
    }

    override fun bindPhoneFail(msg: String) {
        T.show(msg)
    }

    override fun emailAction(email: String, verificationCode: String) {
        val intent = Intent(this, SetPasswordActivity::class.java)
        intent.putExtra(SetPasswordActivity.ACTION, presenter.getAction())
        intent.putExtra(EMAIL, email)
        intent.putExtra(SetPasswordActivity.VERIFICATION_CODE, verificationCode)
        startActivity(intent)
        T.show(getString(R.string.registe_success))
        Utils.clearXmlStringValue(T.getContext(), CommonField.REG_COUNTRY_INFO, CommonField.REG_COUNTRY_INFO)
        finish()
    }

    override fun phoneAction(countryCode: String, phoneNumber: String, verificationCode: String) {
        val intent = Intent(this, SetPasswordActivity::class.java)
        intent.putExtra(SetPasswordActivity.ACTION, presenter.getAction())
        intent.putExtra(COUNTRY_CODE, countryCode)
        intent.putExtra(PHONE, phoneNumber)
        intent.putExtra(SetPasswordActivity.VERIFICATION_CODE, verificationCode)
        startActivity(intent)
        T.show(getString(R.string.registe_success))
        Utils.clearXmlStringValue(T.getContext(), CommonField.REG_COUNTRY_INFO, CommonField.REG_COUNTRY_INFO)
        finish()
    }

    override fun permissionAllGranted() {

    }

    override fun onDestroy() {
        handler?.removeCallbacks(null)
        handler = null
        super.onDestroy()
    }

}
