package com.tencent.iot.explorer.link.mvp.model

import android.os.Handler
import android.text.TextUtils
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.activity.SetPasswordActivity
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.GetCodeView

class GetCodeModel(view: GetCodeView) : ParentModel<GetCodeView>(view), MyCallback {

    private var isCheck = false
    private var lockTime = true
    private var isSend = false
    var phone = ""
    var email = ""
    private var action = -1
    private var type = ""
    private var verificationCode = ""
    private var countryCode = "86"

    private var handler: Handler? = null
    private var runnable: TimerRunnable? = null
    private var time = 60

    fun setCommonData(type: String, action: Int) {
        this.type = type
        this.action = action
    }

    fun setCountryCode(countryCode: String) {
        this.countryCode = countryCode
    }

    fun setVerificationCode(verificationCode: String) {
        this.verificationCode = verificationCode
    }

    fun getAction(): Int {
        return action
    }

    fun isLockTime(): Boolean {
        return lockTime
    }

    fun lockResend() {
        if (handler == null) {
            handler = Handler()
            runnable = TimerRunnable()
        }
        handler?.postDelayed(runnable!!, 1000)
    }

    inner class TimerRunnable : Runnable {
        override fun run() {
            if (time > 0) {
                time--
                handler?.postDelayed(this, 1000)
                lockTime = true
                view?.lockResendShow(time)
            } else {
                time = 60
                view?.unlock()
                lockTime = false
            }
        }
    }

    /**
     * 验证邮箱验证码
     */
    fun checkEmailCode() {
        if (isCheck) return
        HttpRequest.instance.checkEmailCode(type, email, verificationCode, this)
        isCheck = true
    }

    /**
     * 验证手机验证码
     */
    fun checkMobileCode() {
        if (isCheck) return
        HttpRequest.instance.checkMobileCode(type, countryCode, phone, verificationCode, this)
        isCheck = true
    }

    /**
     * 重新发送邮箱验证码
     */
    fun resendEmailCode() {
        HttpRequest.instance.sendEmailCode(type, email, this)
    }

    /**
     * 重新发送手机验证码
     */
    fun resendMobileCode() {
        HttpRequest.instance.sendMobileCode(type, countryCode, phone, this)
    }

    /**
     * 判断是手机号还是邮箱地址
     */
    fun hasMobileNumber(): Boolean {
        if (TextUtils.isEmpty(phone))
            return false
        return true
    }

    /**
     * 判断手机号
     */
    private fun bindPhone() {
        if (TextUtils.isEmpty(phone)) return
        HttpRequest.instance.bindPhone(countryCode, phone, verificationCode, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.send_mobile_code, RequestCode.send_email_code -> {
                isSend = false
                if (response.isSuccess())//没有Error就是成功的
                    lockResend()
                else
                    view?.getCodeFail(response.msg)
            }
            RequestCode.check_mobile_code -> {
                isCheck = false
                if (response.isSuccess()) {
                    if (action == SetPasswordActivity.BIND_PHONE)
                        bindPhone()
                    else view?.phoneAction(countryCode, phone, verificationCode)
                } else view?.checkVerificationCodeFail(response.msg)
            }
            RequestCode.check_email_code -> {
                isCheck = false
                if (response.isSuccess())//没有Error就是成功的
                    view?.emailAction(email, verificationCode)
                else view?.checkVerificationCodeFail(response.msg)
            }
            RequestCode.update_user_info -> {
                if (response.isSuccess()) {
                    App.data.userInfo.PhoneNumber = phone
                    view?.bindPhoneSuccess()
                } else {
                    view?.bindPhoneFail(response.msg)
                }
            }
        }
    }

    override fun onDestroy() {
        handler?.removeCallbacks(runnable)
        handler = null
        runnable = null
        super.onDestroy()
    }
}