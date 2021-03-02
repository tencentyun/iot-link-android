package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.EditNameValue
import kotlinx.android.synthetic.main.activity_add_family.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.android.synthetic.main.menu_cancel_layout.*
import kotlinx.android.synthetic.main.menu_cancel_layout.tv_title

class AddFamilyActivity : BaseActivity(), MyCallback {

    override fun getContentView(): Int {
        return R.layout.activity_add_family
    }

    override fun initView() {
        tv_title.text = getString(R.string.add_family)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        iv_set_location.setOnClickListener {
            val intent = Intent(this, FamilyAddressActivity::class.java)
            startActivityForResult(intent, CommonField.MAP_LOCATION_REQ_CODE)
        }
        et_family_address.setOnClickListener {
            val intent = Intent(this, FamilyAddressActivity::class.java)
            startActivityForResult(intent, CommonField.MAP_LOCATION_REQ_CODE)
        }
        btn_add_family.setOnClickListener { addFamily() }
        iv_set_family_name.setOnClickListener {
            startLoadContentActivity()
        }
        et_family_name.setOnClickListener {
            startLoadContentActivity()
        }
    }

    private fun startLoadContentActivity() {
        var intent = Intent(this, EditNameActivity::class.java)
        var editNameValue = EditNameValue()
        editNameValue.name = ""
        editNameValue.title = getString(R.string.family_setting)
        editNameValue.tipName = getString(R.string.family_name)
        editNameValue.btn = getString(R.string.save)
        editNameValue.hintText = getString(R.string.fill_family_name)
        editNameValue.errorTip = ""
        intent.putExtra(CommonField.EXTRA_INFO, JSON.toJSONString(editNameValue))
        startActivityForResult(intent, CommonField.EDIT_NAME_REQ_CODE)
    }

    private fun addFamily() {
        val familyName = et_family_name.text.toString().trim()
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
        if (requestCode == CommonField.MAP_LOCATION_REQ_CODE && resultCode == RESULT_OK) {
            et_family_address.text = data?.getStringExtra("address") ?: ""
        } else if (requestCode == CommonField.EDIT_NAME_REQ_CODE && resultCode == RESULT_OK) {
            et_family_name.text = data?.getStringExtra(CommonField.EXTRA_TEXT) ?: ""
        }
    }
}
