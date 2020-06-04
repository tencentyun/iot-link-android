package com.tenext.demo.activity

import android.text.TextUtils
import android.widget.Toast
import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.response.BaseResponse
import com.tenext.demo.R
import com.tenext.demo.log.L
import kotlinx.android.synthetic.main.activity_add_family.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 添加家庭
 */
class AddFamilyActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_add_family
    }

    override fun initView() {
        tv_title.text = getString(R.string.add_family)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }

        btn_add_family.setOnClickListener { addFamily() }
    }

    private fun addFamily() {
        val familyName = et_family_name.text.toString().trim()
        val familyAddress = et_family_address.text.toString().trim()
        if (TextUtils.isEmpty(familyName)) {
            Toast.makeText(this,getString(R.string.empty_family),Toast.LENGTH_LONG).show()
            return
        }
        IoTAuth.familyImpl.createFamily(familyName, familyAddress, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.e(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    finish()
                }
            }
        })
    }
}
