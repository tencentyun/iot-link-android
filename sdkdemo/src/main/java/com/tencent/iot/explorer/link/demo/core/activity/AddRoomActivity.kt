package com.tencent.iot.explorer.link.demo.core.activity

import android.text.TextUtils
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.databinding.ActivityAddRoomBinding

class AddRoomActivity : BaseActivity<ActivityAddRoomBinding>(), MyCallback {
    override fun getViewBinding(): ActivityAddRoomBinding = ActivityAddRoomBinding.inflate(layoutInflater)

    override fun initView() {
        binding.addRoomMenu.tvTitle.text = getString(R.string.add_room)
    }

    override fun setListener() {
        with(binding) {
            addRoomMenu.ivBack.setOnClickListener { finish() }
            btnAddRoom.setOnClickListener { addRoom() }
        }
    }

    private fun addRoom() {
        val familyName = binding.etRoomName.text.toString().trim()
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
        L.e { msg ?: "" }
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
