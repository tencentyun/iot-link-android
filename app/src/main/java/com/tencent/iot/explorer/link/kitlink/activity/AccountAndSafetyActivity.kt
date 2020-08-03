package com.tencent.iot.explorer.link.kitlink.activity

import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_account_and_safety.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class AccountAndSafetyActivity : PActivity(), View.OnClickListener {

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.activity_account_and_safety
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.account_and_safety)
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
                // 若已绑定则跳到修改手机号

                // 若未绑定则跳到绑定
            }
            tv_email -> {// 邮箱
                T.show("邮箱")
                // 若已绑定则跳到修改手机号

                // 若未绑定则跳到绑定
            }
            tv_wechat -> {// 微信
                T.show("微信")
            }
            tv_modify_passwd -> {// 修改密码
                T.show("修改密码")
            }
            tv_account_logout -> {// 注销账号
                T.show("注销账号")
            }
        }
    }
}