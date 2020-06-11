package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.view.View
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.kitlink.consts.SocketConstants
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.util.L
import kotlinx.android.synthetic.main.activity_bind_mobile_phone.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 绑定手机号
 */
class BindMobilePhoneActivity : PActivity(), MyCallback {

    private var countryName = "中国大陆"
    private var countryCode = "86"
    private var phone = ""
    private var isSend = false

    override fun getContentView(): Int {
        return R.layout.activity_bind_mobile_phone
    }

    override fun getPresenter(): IPresenter? {
        return null
    }
    override fun initView() {
        tv_title.text = getString(R.string.bind_phone)
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        et_bind_phone.addClearImage(iv_clear_bind_phone)
        btn_bind_get_code.addEditText(et_bind_phone, tv_bind_phone_hint, countryCode)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_bind_to_country.setOnClickListener {
            startActivityForResult(Intent(this, CountryCodeActivity::class.java), 100)
        }
        iv_bind_to_country.setOnClickListener {
            startActivityForResult(Intent(this, CountryCodeActivity::class.java), 100)
        }
        btn_bind_get_code.setOnClickListener {
            phone = et_bind_phone.text.trim().toString()
            commit()
        }
    }

    private fun showCountry(data: String) {
        if (data.contains("+")) {
            data.split("+").let {
                countryName = it[0]
                countryCode = it[1]
                btn_bind_get_code.changeType(et_bind_phone, countryCode)
                tv_bind_to_country.text = countryName
            }
        }
    }

    private fun commit() {
        HttpRequest.instance.sendMobileCode(SocketConstants.register, countryCode, phone, this)
        /*val params = NoLoginParams(SocketAction.GET_MOBILE_CODE, RequestID.get_code)
        params.addActionParam(CommonField.TYPE, SocketConstants.register)
            .addActionParam(CommonField.PHONE_NUMBER, phone)
            .addActionParam(CommonField.COUNTRY_CODE, countryCode)
        WSClientManager.request(params.toString())*/
        isSend = true
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        isSend = false
        if (response.isSuccess()){
            jump()
        }else{
            tv_bind_phone_hint.visibility = View.VISIBLE
            tv_bind_phone_hint.text = response.msg
        }
    }

    private fun jump() {
        val intent = Intent(this, GetCodeActivity::class.java)
        intent.putExtra(GetCodeActivity.TYPE, SocketConstants.register)
        intent.putExtra(GetCodeActivity.COUNTRY_CODE, countryCode)
        intent.putExtra(GetCodeActivity.PHONE, phone)
        intent.putExtra(SetPasswordActivity.ACTION, SetPasswordActivity.BIND_PHONE)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            data?.let {
                it.getStringExtra(CommonField.COUNTRY_CODE)?.run {
                    showCountry(this)
                }
            }
        }
    }
}
