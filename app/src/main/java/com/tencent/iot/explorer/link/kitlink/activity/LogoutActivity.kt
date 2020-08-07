package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.view.View
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.AccountAndSafetyPresenter
import com.tencent.iot.explorer.link.mvp.presenter.LogoutPresenter
import com.tencent.iot.explorer.link.mvp.view.LogoutView
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_logout.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class LogoutActivity  : PActivity(), LogoutView, View.OnClickListener{

    private lateinit var presenter: LogoutPresenter

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_logout
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.logout_account)
        presenter = LogoutPresenter(this)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        btn_logout.setOnClickListener(this)
        tv_logout_account_agreement.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            btn_logout -> {// 注销
                T.show("注销")
                presenter.cancelAccount()
            }
            tv_logout_account_agreement -> {// 注销协议
                T.show("注销协议")
                val intent = Intent(this, WebActivity::class.java)
                intent.putExtra(CommonField.EXTRA_TITLE, getString(R.string.tencentll_account_logout_agreement2))
                var url = CommonField.POLICY_PREFIX
                if (App.DEBUG_VERSION) url += "?uin=archurtest"
                url += CommonField.CANCEL_POLICY_SUFFIX
                intent.putExtra(CommonField.EXTRA_TEXT, url)
                startActivity(intent)
            }
        }
    }

}