package com.tencent.iot.explorer.link.core.demo.activity

import android.text.TextUtils
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.log.L
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
        get<FamilyEntity>("family")?.run {
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
