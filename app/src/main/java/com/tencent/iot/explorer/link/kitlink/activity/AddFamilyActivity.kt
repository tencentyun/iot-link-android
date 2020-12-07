package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import kotlinx.android.synthetic.main.activity_add_family.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.android.synthetic.main.menu_cancel_layout.*
import kotlinx.android.synthetic.main.menu_cancel_layout.tv_title

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
        iv_back.setOnClickListener { finish() }
        /*tv_family_address.setOnClickListener {
            val intent = Intent(this, FamilyAddressActivity::class.java)
            startActivityForResult(intent, 105)
        }*/
        btn_add_family.setOnClickListener { addFamily() }
        et_family_name.addTextChangedListener(textWatcher)
        et_family_address.addTextChangedListener(textWatcher)
        et_family_name.setText("")
    }

    private var textWatcher = object : TextWatcher{
        override fun afterTextChanged(s: Editable?) {
            if (!TextUtils.isEmpty(et_family_name.text.toString().trim()) &&
                !TextUtils.isEmpty(et_family_address.text.toString().trim())) {
                btn_add_family.isClickable = true
                btn_add_family.setBackgroundResource(R.drawable.background_circle_bule_gradient)
            } else {
                btn_add_family.isClickable = false
                btn_add_family.setBackgroundResource(R.drawable.background_grey_dark_cell)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

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
