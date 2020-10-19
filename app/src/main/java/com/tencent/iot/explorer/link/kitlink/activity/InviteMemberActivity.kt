package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.FamilyEntity
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.kitlink.response.UserInfoResponse
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_invite_member.*
import kotlinx.android.synthetic.main.layout_email_register.view.*
import kotlinx.android.synthetic.main.layout_phone_register.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 邀请家庭成员
 */
class InviteMemberActivity : BaseActivity(), View.OnClickListener, MyCallback {

    //true是手机邀请，false是邮箱邀请
    private var inviteType = true

    private var countryCode = "86"
    private var countryName = T.getContext().getString(R.string.china_main_land)//"中国大陆"
    private var account = ""

    private lateinit var phoneView: View
    private lateinit var emailView: View

    private var familyEntity: FamilyEntity? = null

    override fun getContentView(): Int {
        return R.layout.activity_invite_member
    }

    override fun initView() {
        familyEntity = get("family")
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.invite_member)
        initViewPager()
        showPhoneInvite()
    }

    private fun initViewPager() {
        phoneView = LayoutInflater.from(this).inflate(R.layout.layout_phone_register, null)
        emailView = LayoutInflater.from(this).inflate(R.layout.layout_email_register, null)
        phoneView.et_register_phone.addClearImage(phoneView.iv_register_phone_clear)
        phoneView.tv_register_to_email.text = getString(R.string.email_forgot)
        emailView.et_register_email.addClearImage(emailView.iv_register_email_clear)
        emailView.tv_register_to_phone.text = getString(R.string.phone_forgot)
        vp_invite.addViewToList(phoneView)
        vp_invite.addViewToList(emailView)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        phoneView.tv_register_to_email.setOnClickListener(this)
        emailView.tv_register_to_phone.setOnClickListener(this)
        btn_invite.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v) {
            btn_invite -> {//邀请
                when (inviteType) {
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
                inviteType = false
                showEmailInvite()
            }
            emailView.tv_register_to_phone -> {//显示手机号注册界面
                inviteType = true
                showPhoneInvite()
            }
        }
    }

    /**
     * 手机号邀请
     */
    private fun showPhoneInvite() {
        vp_invite.setCurrentItem(0, true)
        btn_invite.removeEditText(emailView.et_register_email)
        btn_invite.addEditText(
            phoneView.et_register_phone,
            phoneView.tv_register_phone_hint,
            countryCode
        )
    }

    /**
     * 邮箱邀请
     */
    private fun showEmailInvite() {
        vp_invite.setCurrentItem(1, true)
        btn_invite.removeEditText(phoneView.et_register_phone)
        btn_invite.addEditText(
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
     * 发送邀请
     */
    private fun sendFamilyInvite(userId: String) {
        familyEntity?.let {
            HttpRequest.instance.sendFamilyInvite(it.FamilyId, userId, this)
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.find_email_user, RequestCode.find_phone_user -> {
                    response.parse(UserInfoResponse::class.java)?.Data?.run {
                        sendFamilyInvite(UserID)
                    }
                }
                RequestCode.send_family_invite -> {
                    show(getString(R.string.invite_send_success))
                }
            }
        } else {
            show(response.msg)
        }
    }

    private fun showCountryCode() {
        phoneView.tv_register_to_country.text = countryName
        btn_invite.changeType(phoneView.et_register_phone, countryCode)
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
