package com.tencent.iot.explorer.link.kitlink.activity

import android.text.TextUtils
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.popup.CommonPopupWindow
import com.tencent.iot.explorer.link.kitlink.popup.EditPopupWindow
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 房间详情（设置）
 */
class RoomActivity : BaseActivity(), MyCallback {

    private var roomEntity: RoomEntity? = null
    private var familyEntity: FamilyEntity? = null

    private var deleteRoomPopup: CommonPopupWindow? = null
    private var modifyRoomPopup: EditPopupWindow? = null
    private var modifyRoomName = ""

    override fun getContentView(): Int {
        return R.layout.activity_room
    }

    override fun initView() {
        roomEntity = get("room")
        familyEntity = get("family")
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.room_setting)
        tv_room_setting_name.text = roomEntity?.RoomName ?: ""
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
        familyEntity?.let {
            roomEntity?.run {
                HttpRequest.instance.deleteRoom(it.FamilyId, RoomId, this@RoomActivity)
            }
        }
    }

    /**
     * 修改房间名称
     */
    private fun modifyRoomName() {
        if (TextUtils.isEmpty(modifyRoomName)) return
        familyEntity?.let {
            roomEntity?.run {
                HttpRequest.instance.modifyRoom(
                    it.FamilyId,
                    RoomId,
                    modifyRoomName,
                    this@RoomActivity
                )
            }
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
            App.data.setRefreshLevel(1)
            when (reqCode) {
                RequestCode.modify_room -> {
                    modifyRoomPopup?.dismiss()
                    roomEntity?.RoomName = modifyRoomName
                    tv_room_setting_name.text = modifyRoomName
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
                roomEntity?.RoomName ?: ""
            )
        }
        modifyRoomPopup?.setBg(room_bg)
        modifyRoomPopup?.show(room)
        modifyRoomPopup?.onVerifyListener = object : EditPopupWindow.OnVerifyListener {
            override fun onVerify(text: String) {
                if (TextUtils.isEmpty(text)) {
                    T.show(getString(R.string.empty_room))
                    return
                }
                modifyRoomName = text
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
        deleteRoomPopup?.show(room)
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

}
