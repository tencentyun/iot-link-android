package com.tencent.iot.explorer.link.kitlink.activity

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.auth.response.CreateRoomResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import kotlinx.android.synthetic.main.activity_add_room.*
import kotlinx.android.synthetic.main.activity_add_task_name.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.android.synthetic.main.menu_cancel_layout.*
import kotlinx.android.synthetic.main.menu_cancel_layout.tv_title

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
        btn_add_room.setText("+   " + getString(R.string.add_room))
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        btn_add_room.setOnClickListener { addRoom() }
        et_room_name.addTextChangedListener(textWatcher)
        et_room_name.setText("")
    }

    private var textWatcher = object: TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (TextUtils.isEmpty(et_room_name.text.trim())) {
                btn_add_room.isClickable = false
                btn_add_room.setTextColor(this@AddRoomActivity.resources.getColor(R.color.gray_c2c5cc))
            } else {
                btn_add_room.isClickable = true
                btn_add_room.setTextColor(this@AddRoomActivity.resources.getColor(R.color.blue_0066FF))
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

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
