package com.tencent.iot.explorer.link.kitlink.activity

import android.opengl.Visibility
import android.text.TextUtils
import android.view.View
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.kitlink.util.WeChatLogin
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.AccountAndSafetyPresenter
import com.tencent.iot.explorer.link.mvp.presenter.BindEmailPresenter
import com.tencent.iot.explorer.link.mvp.view.AccountAndSafetyView
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_account_and_safety.*
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class AccountAndSafetyActivity : PActivity(), AccountAndSafetyView, View.OnClickListener,
    WeChatLogin.OnLoginListener, MyCallback {

    private lateinit var presenter: AccountAndSafetyPresenter

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_account_and_safety
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.account_and_safety)
        presenter = AccountAndSafetyPresenter(this)
    }

    override fun onResume() {
        super.onResume()
        presenter.getUserInfo()
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
                T.show("手机号")
                if (!TextUtils.isEmpty(App.data.userInfo.PhoneNumber)) {// 若已绑定则跳到修改手机号
                    jumpActivity(ModifyPhoneActivity::class.java)
                } else {// 若未绑定则跳到绑定
                    jumpActivity(BindPhoneActivity::class.java)
                }
            }
            tv_email -> {// 邮箱
                T.show("邮箱")
                if (!TextUtils.isEmpty(App.data.userInfo.Email)) {// 若已绑定则跳到修改邮箱号
                    jumpActivity(ModifyEmailActivity::class.java)
                } else {// 若未绑定则跳到绑定
                    jumpActivity(BindEmailActivity::class.java)
                }
            }
            tv_wechat -> {// 微信
                if (App.data.userInfo.HasWxOpenID == "0") {
                    WeChatLogin.getInstance().login(this, this)
                } else {
                    T.show("微信已经绑定过了, 请勿重复绑定")
                }
            }
            tv_modify_passwd -> {// 修改密码
                T.show("修改密码")
                jumpActivity(ModifyPasswordActivity::class.java)
            }
            tv_account_logout -> {// 注销账号
                T.show("注销账号")
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
        if (App.data.userInfo.HasWxOpenID == "0") {
            tv_wechat_state.text = getString(R.string.unbind)
        } else {
            tv_wechat_state.text = getString(R.string.have_bind)
        }
    }

    private fun hideModifyPasswd() {
        line_modify_passwd.visibility = View.GONE
        iv_modify_passwd_arrow.visibility = View.GONE
        tv_modify_passwd.visibility = View.GONE
    }

    // Wechat begin
    override fun onSuccess(reqCode: String) {
        T.show(reqCode)
        HttpRequest.instance.bindWX(reqCode, this)
    }

    override fun cancel() {
        T.show("取消")
    }

    override fun onFail(msg: String) {
        T.show(msg)
    }
    // Wechat end

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
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
}