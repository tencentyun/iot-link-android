package com.tencent.iot.explorer.link.kitlink.activity

import android.text.TextUtils
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.auth.response.CreateRoomResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import kotlinx.android.synthetic.main.activity_add_room.*
import kotlinx.android.synthetic.main.menu_cancel_layout.*

/**
 * 新增房间
 */
class AddRoomActivity : BaseActivity(), MyCallback {

    private var familyEntity: FamilyEntity? = null

    override fun getContentView(): Int {
        return R.layout.activity_add_room
    }

    override fun initView() {
        tv_title.text = getString(R.string.add_room)
        familyEntity = get("family")
    }

    override fun setListener() {
        tv_back.setOnClickListener { finish() }
        btn_add_room.setOnClickListener { addRoom() }
    }

    private fun addRoom() {
        familyEntity?.run {
            val familyName = et_room_name.text.toString().trim()
            if (TextUtils.isEmpty(familyName)) {
                T.show(getString(R.string.empty_room))
                return
            }
            HttpRequest.instance.createRoom(FamilyId, familyName, this@AddRoomActivity)
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.create_room -> {
                if (response.isSuccess()) {
                    response.parse(CreateRoomResponse::class.java)?.Data?.run {
                        App.data.setRefreshLevel(1)
                        T.show(getString(R.string.add_sucess)) //添加成功
                        finish()
                    }
                }
            }
        }
    }
}
