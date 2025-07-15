package com.tencent.iot.explorer.link.demo.core.activity

import android.text.TextUtils
import android.widget.Toast
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.databinding.ActivityAddFamilyBinding

/**
 * 添加家庭
 */
class AddFamilyActivity : BaseActivity<ActivityAddFamilyBinding>() {

    override fun getViewBinding(): ActivityAddFamilyBinding = ActivityAddFamilyBinding.inflate(layoutInflater)

    override fun initView() {
        binding.addFamilyMenu.tvTitle.text = getString(R.string.add_family)
    }

    override fun setListener() {
        binding.addFamilyMenu.ivBack.setOnClickListener { finish() }

        binding.btnAddFamily.setOnClickListener { addFamily() }
    }

    private fun addFamily() {
        val familyName = binding.etFamilyName.text.toString().trim()
        val familyAddress = binding.etFamilyAddress.text.toString().trim()
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
