package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.TextUtils
import com.tencent.iot.explorer.link.kitlink.App
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.util.L
import com.util.T
import kotlinx.android.synthetic.main.activity_add_family.*
import kotlinx.android.synthetic.main.menu_cancel_layout.*

/**
 * 新增家庭
 */
class AddFamilyActivity : BaseActivity(), MyCallback {

    override fun getContentView(): Int {
        return R.layout.activity_add_family
    }

    override fun initView() {
        tv_title.text = getString(R.string.add_family)
    }

    override fun setListener() {
        tv_back.setOnClickListener { finish() }
        /*tv_family_address.setOnClickListener {
            val intent = Intent(this, FamilyAddressActivity::class.java)
            startActivityForResult(intent, 105)
        }*/
        btn_add_family.setOnClickListener { addFamily() }
    }

    private fun addFamily() {
        val familyName = et_family_name.text.toString().trim()
//        val familyAddress = tv_family_address.text.toString().trim()
        val familyAddress = et_family_address.text.toString().trim()
        if (TextUtils.isEmpty(familyName)) {
            T.show(getString(R.string.empty_family))
            return
        }
        HttpRequest.instance.createFamily(familyName, familyAddress, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.create_family -> {
                if (response.isSuccess()) {
                    App.data.setRefreshLevel(0)
                    finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 105 && resultCode == 200) {
//            tv_family_address.text = data?.getStringExtra("address") ?: ""
        }
    }
}
