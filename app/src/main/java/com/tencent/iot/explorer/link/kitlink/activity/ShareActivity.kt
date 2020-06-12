package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.tencent.iot.explorer.link.kitlink.App
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.DeviceEntity
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.util.L
import com.tencent.iot.explorer.link.kitlink.activity.BaseActivity
import com.tencent.iot.explorer.link.kitlink.response.UserInfoResponse
import kotlinx.android.synthetic.main.activity_share.*
import kotlinx.android.synthetic.main.layout_email_register.view.*
import kotlinx.android.synthetic.main.layout_phone_register.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 分享用户
 */
class ShareActivity : BaseActivity(), MyCallback, View.OnClickListener {

    //true是手机分享，false是邮箱分享
    private var shareType = true

    private var countryCode = "86"
    private var countryName = "中国大陆"
    private var account = ""

    private lateinit var phoneView: View
    private lateinit var emailView: View

    private var deviceEntity: DeviceEntity? = null

    override fun getContentView(): Int {
        return R.layout.activity_share
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        deviceEntity = get("device")
        tv_title.text = getString(R.string.device_share_to_user)
        initViewPager()
        showPhoneShare()
    }

    private fun initViewPager() {
        phoneView = LayoutInflater.from(this).inflate(R.layout.layout_phone_register, null)
        emailView = LayoutInflater.from(this).inflate(R.layout.layout_email_register, null)
        phoneView.et_register_phone.addClearImage(phoneView.iv_register_phone_clear)
        phoneView.tv_register_to_email.text = getString(R.string.email_forgot)
        emailView.et_register_email.addClearImage(emailView.iv_register_email_clear)
        emailView.tv_register_to_phone.text = getString(R.string.phone_forgot)
        vp_share.addViewToList(phoneView)
        vp_share.addViewToList(emailView)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        phoneView.tv_register_to_email.setOnClickListener(this)
        emailView.tv_register_to_phone.setOnClickListener(this)
        btn_share.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v) {
            btn_share -> {//分享
                when (shareType) {
                    true -> {//手机号
                        account = phoneView.et_register_phone.text.trim().toString()
                        findPhoneUser()
                    }
                    false -> {
                        account = emailView.et_register_email.text.trim().toString()
                        findEmailUser()
                    }
                }
            }
            phoneView.tv_register_to_email -> {//显示邮箱注册界面
                shareType = false
                showEmailShare()
            }
            emailView.tv_register_to_phone -> {//显示手机号注册界面
                shareType = true
                showPhoneShare()
            }
        }
    }

    private fun showPhoneShare() {
        vp_share.setCurrentItem(0, true)
        btn_share.removeEditText(emailView.et_register_email)
        btn_share.addEditText(
            phoneView.et_register_phone,
            phoneView.tv_register_phone_hint,
            countryCode
        )
    }

    private fun showEmailShare() {
        vp_share.setCurrentItem(1, true)
        btn_share.removeEditText(phoneView.et_register_phone)
        btn_share.addEditText(
            emailView.et_register_email,
            emailView.tv_register_email_hint,
            "email"
        )
    }

    /**
     * 查找用户ID
     */
    private fun findPhoneUser() {
        HttpRequest.instance.findPhoneUser(account, countryCode, this)
    }

    /**
     * 查找用户ID
     */
    private fun findEmailUser() {
        HttpRequest.instance.findEmailUser(account, this)
    }

    /**
     * 发送设备分享
     */
    private fun sendShareInvite(userId: String) {
        deviceEntity?.let {
            HttpRequest.instance.sendShareInvite(it.ProductId, it.DeviceName, userId, this)
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.find_phone_user, RequestCode.find_email_user -> {
                    response.parse(UserInfoResponse::class.java)?.Data?.run {
                        sendShareInvite(UserID)
                    }
                }
                RequestCode.send_share_invite -> {
                    show(getString(R.string.share_success))
                }
            }
        } else {
            show(response.msg)
        }
    }

    private fun showCountryCode() {
        phoneView.tv_register_to_country.text = countryName
        btn_share.changeType(phoneView.et_register_phone, countryCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            data?.let {
                it.getStringExtra(CommonField.COUNTRY_CODE)?.run {
                    if (!this.contains("+")) return
                    this.split("+").let {
                        countryName = it[0]
                        countryCode = it[1]
                        showCountryCode()
                    }
                }
            }
        }
    }

}
