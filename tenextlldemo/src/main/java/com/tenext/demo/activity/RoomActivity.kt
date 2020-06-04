package com.tenext.demo.activity

import android.text.TextUtils
import com.tenext.demo.popup.CommonPopupWindow
import com.tenext.demo.popup.EditPopupWindow
import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.consts.RequestCode
import com.tenext.auth.entity.Family
import com.tenext.auth.entity.Room
import com.tenext.auth.response.BaseResponse
import com.tenext.demo.R
import com.tenext.demo.log.L
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 房间设置
 */
class RoomActivity : BaseActivity(), MyCallback {

    private var room: Room? = null
    private var familyId = ""

    private var deleteRoomPopup: CommonPopupWindow? = null
    private var modifyRoomPopup: EditPopupWindow? = null
    private var modifyRoomName = ""

    override fun getContentView(): Int {
        return R.layout.activity_room
    }

    override fun initView() {
        tv_title.text = getString(R.string.room_setting)
        room = get<Room>("room")
        get<Family>("family")?.FamilyId?.let {
            familyId = it
        }
        tv_room_setting_name.text = room?.RoomName ?: ""
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_room_setting_title.setOnClickListener {
            showModifyPopup()
        }
        tv_delete_room.setOnClickListener {
            showDeletePopup()
        }
    }

    /**
     *  删除房间
     */
    private fun deleteRoom() {
        room?.run {
            IoTAuth.roomImpl.delete(familyId, RoomId, this@RoomActivity)
        }
    }

    /**
     * 修改房间名称
     */
    private fun modifyRoomName() {
        if (TextUtils.isEmpty(modifyRoomName)) return
        room?.run {
            IoTAuth.roomImpl.modify(familyId, RoomId, modifyRoomName, this@RoomActivity)
        }
    }

    /**
     * 请求失败
     */
    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    /**
     * 请求成功
     */
    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.modify_room -> {
                    room?.RoomName = modifyRoomName
                    runOnUiThread { tv_room_setting_name.text = modifyRoomName }
                }
                RequestCode.delete_room -> {
                    finish()
                }
            }
        }
    }

    /**
     * 显示修改房间弹框
     */
    private fun showModifyPopup() {
        if (modifyRoomPopup == null) {
            modifyRoomPopup = EditPopupWindow(this)
            modifyRoomPopup?.setShowData(
                getString(R.string.room_name),
                room!!.RoomName
            )
        }
        modifyRoomPopup?.setBg(room_bg)
        modifyRoomPopup?.show(room_contain)
        modifyRoomPopup?.onVerifyListener = object : EditPopupWindow.OnVerifyListener {
            override fun onVerify(text: String) {
                if (TextUtils.isEmpty(text)) {
                    show(getString(R.string.empty_room))
                    return
                }
                modifyRoomName = text
                modifyRoomPopup?.dismiss()
                modifyRoomName()
            }
        }
    }

    /**
     * 显示删除房间弹框
     */
    private fun showDeletePopup() {
        if (deleteRoomPopup == null) {
            deleteRoomPopup = CommonPopupWindow(this)
            deleteRoomPopup?.setCommonParams(
                getString(R.string.toast_delete_room_title),
                getString(R.string.toast_delete_room_content)
            )
        }
        deleteRoomPopup?.setBg(room_bg)
        deleteRoomPopup?.show(room_contain)
        deleteRoomPopup?.onKeyListener = object : CommonPopupWindow.OnKeyListener {
            override fun cancel(popupWindow: CommonPopupWindow) {
                deleteRoomPopup?.dismiss()
            }

            override fun confirm(popupWindow: CommonPopupWindow) {
                popupWindow.dismiss()
                deleteRoom()
            }
        }
    }

    /**
     * 返回键按下处理
     */
    override fun onBackPressed() {
        modifyRoomPopup?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        deleteRoomPopup?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        modifyRoomPopup?.dismiss()
        deleteRoomPopup?.dismiss()
        super.onDestroy()
    }

}
