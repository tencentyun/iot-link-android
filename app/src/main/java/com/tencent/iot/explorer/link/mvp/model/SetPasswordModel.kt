package com.tencent.iot.explorer.link.mvp.model

import android.text.TextUtils
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.SetPasswordView
import com.tencent.iot.explorer.link.util.T

/**
 * 设置密码
 */
class SetPasswordModel(view: SetPasswordView) : ParentModel<SetPasswordView>(view), MyCallback {

    var action = -1
    private var countryCode = ""
    private var phoneNumber = ""
    private var password = ""
    private var verifyPassword = ""
    private var email = ""
    private var verificationCode = ""
    private var isCommit = false

    fun setPhoneData(countryCode: String, phoneNumber: String, verificationCode: String) {
        this.countryCode = countryCode
        this.phoneNumber = phoneNumber
        this.verificationCode = verificationCode
    }

    fun setEmailData(email: String, verificationCode: String) {
        this.email = email
        this.verificationCode = verificationCode
    }

    fun setPassword(password: String, verifyPassword: String) {
        this.password = password
        this.verifyPassword = verifyPassword
    }

    /**
     * 邮箱注册
     */
    fun registerEmailPassword() {
        if (!ifPwdSame()) return
        if (isCommit) return
        HttpRequest.instance.emailRegister(email, verificationCode, password, this)
        isCommit = true
    }

    /**
     * 手机号注册
     */
    fun registerPhonePassword() {
        if (!ifPwdSame()) return
        if (isCommit) return
        HttpRequest.instance.phoneRegister(
            countryCode,
            phoneNumber,
            verificationCode,
            password,
            this
        )
        isCommit = true
    }

    /**
     * 邮箱重置密码
     */
    fun resetEmailPassword() {
        if (!ifPwdSame()) return
        if (isCommit) return
        HttpRequest.instance.resetEmailPassword(email, verificationCode, password, this)
        isCommit = true
    }

    /**
     * 手机号重置密码
     */
    fun resetPhonePassword() {
        if (!ifPwdSame()) return
        if (isCommit) return
        HttpRequest.instance.resetPhonePassword(
            countryCode,
            phoneNumber,
            verificationCode,
            password,
            this
        )
        isCommit = true
    }

    private fun ifPwdSame() : Boolean{
        if (!TextUtils.isEmpty(verifyPassword) &&
            !TextUtils.isEmpty(password) &&
            verifyPassword.equals(password)) {
            return true
        } else {
            view?.fail(T.getContext().getString(R.string.toast_password_not_verify))
            return false
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
        isCommit = false
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        isCommit = false
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.phone_register -> view?.phoneRegisterSuccess(phoneNumber)
                RequestCode.email_register -> view?.emailRegisterSuccess(email)
                RequestCode.phone_reset_pwd -> view?.phoneResetSuccess(phoneNumber)
                RequestCode.email_reset_pwd -> view?.emailResetSuccess(email)
            }
        } else {
            view?.fail(response.msg)
        }
    }

}