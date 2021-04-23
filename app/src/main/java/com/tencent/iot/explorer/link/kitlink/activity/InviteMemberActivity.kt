package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.kitlink.response.UserInfoResponse
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.kitlink.entity.RegionEntity
import com.tencent.iot.explorer.link.kitlink.util.MyCustomCallBack
import kotlinx.android.synthetic.main.activity_invite_member.*
import kotlinx.android.synthetic.main.activity_region.*
import kotlinx.android.synthetic.main.layout_email_register.view.*
import kotlinx.android.synthetic.main.layout_phone_register.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 邀请家庭成员
 */
class InviteMemberActivity : BaseActivity(), View.OnClickListener, MyCallback {

    //true是手机邀请，false是邮箱邀请
    private var inviteType = true
    private var account = ""

    private lateinit var phoneView: View
    private lateinit var emailView: View

    private var familyEntity: FamilyEntity? = null

    override fun getContentView(): Int {
        return R.layout.activity_invite_member
    }

    override fun initView() {
        familyEntity = get("family")
        iv_back.setColorFilter(resources.getColor(R.color.black_15161A))
        tv_title.text = getString(R.string.invite_member)
        initViewPager()
        showPhoneInvite()
        phoneView.tv_register_to_country.text = ""
        HttpRequest.instance.getRegionList(CommonField.REGION_LIST_URL, regionCallback, RequestCode.get_region_list)
    }

    var regionCallback = object: MyCustomCallBack {
        override fun fail(msg: String?, reqCode: Int) {
            T.show(msg?:"")
        }

        override fun success(str: String, reqCode: Int) {
            when (reqCode) {
                RequestCode.get_region_list -> {// 拉取时区列表
                    val start = str.indexOf('[')
                    val end = str.indexOf(']')
                    val regionArray = str.substring(start, end + 1)
                    val list = JsonManager.parseJsonArray(regionArray, RegionEntity::class.java)
                    if (list != null && list.size > 0) {
                        for (i in 0 until list.size) {
                            if (list.get(i).CountryCode == App.data.conutryCode) {
                                if (Utils.getLang().contains("zh-CN")) {
                                    phoneView.tv_register_to_country.text = list.get(i).Title + getString(R.string.conutry_code_num, App.data.conutryCode)
                                    emailView.tv_register_to_country_email.text = list.get(i).Title + getString(R.string.conutry_code_num, App.data.conutryCode)
                                } else {
                                    phoneView.tv_register_to_country.text = list.get(i).TitleEN + getString(R.string.conutry_code_num, App.data.conutryCode)
                                    emailView.tv_register_to_country_email.text = list.get(i).TitleEN + getString(R.string.conutry_code_num, App.data.conutryCode)
                                }
                                break
                            }
                        }
                    }
                }
            }
        }

    }

    private fun initViewPager() {
        phoneView = LayoutInflater.from(this).inflate(R.layout.layout_phone_register, null)
        emailView = LayoutInflater.from(this).inflate(R.layout.layout_email_register, null)
        phoneView.et_register_phone.addClearImage(phoneView.iv_register_phone_clear)
        phoneView.iv_register_to_country.visibility = View.INVISIBLE
        phoneView.tv_register_to_email.text = getString(R.string.email_forgot)
        emailView.et_register_email.addClearImage(emailView.iv_register_email_clear)
        emailView.tv_register_to_phone.text = getString(R.string.phone_forgot)
        emailView.iv_register_to_country_email.visibility = View.INVISIBLE
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
    }

    /**
     * 邮箱邀请
     */
    private fun showEmailInvite() {
        vp_invite.setCurrentItem(1, true)
        btn_invite.removeEditText(phoneView.et_register_phone)
    }

    /**
     * 查找用户ID
     */
    private fun findPhoneUser() {
        HttpRequest.instance.findPhoneUser(account, App.data.conutryCode, this)
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

}
