package com.tenext.demo.activity

import android.text.TextUtils
import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.entity.Family
import com.tenext.auth.response.BaseResponse
import com.tenext.demo.R
import com.tenext.demo.log.L
import kotlinx.android.synthetic.main.activity_add_room.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class AddRoomActivity : BaseActivity(), MyCallback {
    override fun getContentView(): Int {
        return R.layout.activity_add_room
    }

    override fun initView() {
        tv_title.text = getString(R.string.add_room)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        btn_add_room.setOnClickListener { addRoom() }
    }

    private fun addRoom() {
        val familyName = et_room_name.text.toString().trim()
        if (TextUtils.isEmpty(familyName)) {
            show(getString(R.string.empty_room))
            return
        }
        get<Family>("family")?.run {
            if (TextUtils.isEmpty(FamilyId)) return
            IoTAuth.roomImpl.create(FamilyId, familyName, this@AddRoomActivity)
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        runOnUiThread {
            if (response.isSuccess()) {
                show("添加成功")
                finish()
            }
        }
    }
}
