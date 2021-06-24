package com.tencent.iot.explorer.link.kitlink.activity

import android.text.TextUtils
import android.view.View
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.RegionEntity
import com.tencent.iot.explorer.link.kitlink.util.*
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.AccountAndSafetyPresenter
import com.tencent.iot.explorer.link.mvp.view.AccountAndSafetyView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.utils.Utils
import kotlinx.android.synthetic.main.activity_account_and_safety.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class AccountAndSafetyActivity : PActivity(), AccountAndSafetyView, View.OnClickListener,
    WeChatLogin.OnLoginListener, MyCallback, MyCustomCallBack{

    private lateinit var presenter: AccountAndSafetyPresenter

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_account_and_safety
    }

    override fun initView() {
        tv_title.text = getString(R.string.account_and_safety)
        presenter = AccountAndSafetyPresenter(this)
    }

    override fun onResume() {
        super.onResume()
        presenter.getUserInfo()
        getRegionList()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_phone_number.setOnClickListener(this)
        tv_email.setOnClickListener(this)
        tv_wechat.setOnClickListener(this)
        tv_modify_passwd.setOnClickListener(this)
        tv_account_logout.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_phone_number -> {// 手机号
                if (!TextUtils.isEmpty(App.data.userInfo.PhoneNumber)) {// 若已绑定则跳到修改手机号
                    jumpActivity(ModifyPhoneActivity::class.java)
                } else {// 若未绑定则跳到绑定
                    jumpActivity(BindPhoneActivity::class.java)
                }
            }
            tv_email -> {// 邮箱
                if (!TextUtils.isEmpty(App.data.userInfo.Email)) {// 若已绑定则跳到修改邮箱号
                    jumpActivity(ModifyEmailActivity::class.java)
                } else {// 若未绑定则跳到绑定
                    jumpActivity(BindEmailActivity::class.java)
                }
            }
            tv_wechat -> {// 微信
                if (App.data.userInfo.HasWxOpenID == "1") {
                    T.show(getString(R.string.wechat_bind_already)) //微信已经绑定过了, 请勿重复绑定
                } else {
                    WeChatLogin.getInstance().login(this, this)
                }
            }
            tv_modify_passwd -> {// 修改密码
                jumpActivity(ModifyPasswordActivity::class.java)
            }
            tv_account_logout -> {// 注销账号
                jumpActivity(LogoutActivity::class.java)
            }
        }
    }

    override fun showUserInfo() {
        if (App.data.userInfo.HasPassword == "0") {
            hideModifyPasswd()
        }
        if (!TextUtils.isEmpty(App.data.userInfo.PhoneNumber)) {
            tv_phone_number_state.text = App.data.userInfo.PhoneNumber
        } else {
            tv_phone_number_state.text = getString(R.string.unbind)
        }
        if (!TextUtils.isEmpty(App.data.userInfo.Email)) {
            tv_email_state.text = App.data.userInfo.Email
        } else {
            tv_email_state.text = getString(R.string.unbind)
        }
        if (App.data.userInfo.HasWxOpenID == "1") {
            tv_wechat_state.text = getString(R.string.have_bind)
        } else {
            tv_wechat_state.text = getString(R.string.unbind)
        }
    }

    private fun hideModifyPasswd() {
        line_modify_passwd.visibility = View.GONE
        iv_modify_passwd_arrow.visibility = View.GONE
        tv_modify_passwd.visibility = View.GONE
    }

    // Wechat begin
    override fun onSuccess(reqCode: String) {
        HttpRequest.instance.bindWX(reqCode, this)
    }

    override fun cancel() {
        T.show(getString(R.string.cancel))
    }

    override fun onFail(msg: String) {
        T.show(msg)
    }
    // Wechat end

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(str: String, reqCode: Int) {
        when (reqCode) {
            RequestCode.get_region_list -> {
                tv_country_of_account.text = getCountryOfAccount(str)
            }
        }
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.bind_wx -> {
                if (response.isSuccess()) {
                    tv_wechat_state.text = getString(R.string.have_bind)
                } else {
                    T.show(response.msg)
                }
            }
        }
    }

    private fun getRegionList() {
        HttpRequest.instance.getRegionList(CommonField.REGION_LIST_URL, this, RequestCode.get_region_list)
    }

    private fun getCountryOfAccount(str: String) : String {
        val start = str.indexOf('[')
        val end = str.indexOf(']')
        val regionArray = str.substring(start, end + 1)
        val list = JsonManager.parseJsonArray(regionArray, RegionEntity::class.java)
        for (item: RegionEntity in list) {
            if (item.Region == App.data.region) {
                return if (Utils.isChineseSystem(this)) item.Title
                else item.TitleEN
            }
        }
        return getString(R.string.country_china)
    }
}