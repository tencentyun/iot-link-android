package com.tencent.iot.explorer.link.core.demo.activity

import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.tencent.iot.explorer.link.core.demo.popup.CommonPopupWindow
import com.tencent.iot.explorer.link.core.demo.popup.EditPopupWindow
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.consts.RequestCode
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.adapter.MemberAdapter
import com.tencent.iot.explorer.link.core.demo.adapter.OnItemListener
import com.tencent.iot.explorer.link.core.demo.entity.FamilyInfo
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder
import com.tencent.iot.explorer.link.core.demo.log.L
import com.tencent.iot.explorer.link.core.demo.response.FamilyInfoResponse
import com.tencent.iot.explorer.link.core.demo.response.MemberListResponse
import kotlinx.android.synthetic.main.activity_family.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 家庭详情
 */
class FamilyActivity : BaseActivity(), MyCallback {

    private lateinit var adapter: MemberAdapter

    private var family: FamilyEntity? = null

    private val memberList = arrayListOf<Any>()
    private val familyInfo = FamilyInfo()

    private var editPopupWindow: EditPopupWindow? = null
    private var deleteFamilyPopup: CommonPopupWindow? = null
    private var exitFamilyPopup: CommonPopupWindow? = null

    override fun getContentView(): Int {
        return R.layout.activity_family
    }

    override fun onResume() {
        super.onResume()
        getMemberList()
    }

    override fun initView() {
        tv_title.text = getString(R.string.family_detail)
        val layoutManager = GridLayoutManager(this, 3)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) 3 else if (position > memberList.size) 3 else 1
            }
        }
        adapter = MemberAdapter(this, memberList)
        rv_member_list.layoutManager = layoutManager
        rv_member_list.adapter = adapter

        family = get<FamilyEntity>("family")
        family?.run {
            if (Role == 1) {
                if (IoTAuth.familyList.size <= 1)
                    tv_delete_family.alpha = 0.5f
                getString(R.string.delete_family)
            } else {
                getString(R.string.exit_family)
            }
            memberList.add(this)
            getFamilyInfo()
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_delete_family.setOnClickListener {
            family?.run {
                if (Role == 1) {
                    if (IoTAuth.familyList.size > 1)
                        showDeleteFamilyPopup()
                } else {
                    showExitFamilyPopup()
                }
            }
        }
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                when (position) {
                    -1 -> {
                        showModifyFamilyNamePopup()
                    }
                    -2 -> {
                        jumpActivity(RoomListActivity::class.java)
                    }
                    -3 -> {
                        jumpActivity(InviteMemberActivity::class.java)
                    }
                    else -> {
                        put("member", memberList[position])
                        jumpActivity(MemberActivity::class.java)
                    }
                }
            }
        })
    }

    private fun getFamilyInfo() {
        family?.run {
            IoTAuth.familyImpl.familyInfo(FamilyId, this@FamilyActivity)
        }
    }

    private fun getMemberList() {
        family?.run {
            IoTAuth.familyImpl.memberList(FamilyId, 0, this@FamilyActivity)
        }
    }

    /**
     *  修改家庭名称
     */
    fun modifyFamilyName(familyName: String) {
        family?.let {
            IoTAuth.familyImpl.modifyFamily(it.FamilyId, familyName, "", this)
        }
    }

    /**
     * 删除家庭
     */
    fun deleteFamily() {
        familyInfo.run {
            family?.let {
                if (it.Role == 1) {//管理员
                    IoTAuth.memberImpl.deleteFamily(FamilyId, FamilyName, this@FamilyActivity)
                }
            }
        }
    }

    /**
     * 退出家庭
     */
    fun exitFamily() {
        familyInfo.run {
            family?.let {
                if (it.Role == 0) {//管理员
                    IoTAuth.memberImpl.exitFamily(FamilyId, this@FamilyActivity)
                }
            }
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.d(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.family_info -> {
                if (response.isSuccess()) {
                    response.parse(FamilyInfoResponse::class.java)?.Data?.run {
                        familyInfo.FamilyId = FamilyId
                        familyInfo.FamilyName = FamilyName
                        familyInfo.CreateTime = CreateTime
                        familyInfo.UpdateTime = UpdateTime
                        familyInfo.Address = Address
                        family?.FamilyName = FamilyName
                        refreshList()
                    }
                }
            }
            RequestCode.delete_family, RequestCode.exit_family -> {
                if (response.isSuccess()) {
                    deleteSuccess()
                } else {
                    show(response.msg)
                }
            }
            RequestCode.member_list -> {
                if (response.isSuccess()) {
                    response.parse(MemberListResponse::class.java)?.run {
                        memberList.clear()
                        memberList.add(family!!)
                        memberList.addAll(MemberList)
                        refreshList()
                    }
                }

            }
            RequestCode.modify_family -> if (response.isSuccess()) getFamilyInfo()
        }
    }

    private fun refreshList() {
        adapter.notifyDataSetChanged()
    }

    private fun deleteSuccess() {
        editPopupWindow?.dismiss()
        deleteFamilyPopup?.dismiss()
        exitFamilyPopup?.dismiss()
        IoTAuth.familyList.remove(family)
        finish()
    }

    /**
     * 显示修改弹框
     */
    private fun showModifyFamilyNamePopup() {
        if (editPopupWindow == null) {
            editPopupWindow = EditPopupWindow(this)
            editPopupWindow?.setShowData(
                getString(R.string.family_name),
                family?.FamilyName ?: ""
            )
        }
        editPopupWindow?.setBg(family_bg)
        editPopupWindow?.show(family_contain)
        editPopupWindow?.onVerifyListener = object : EditPopupWindow.OnVerifyListener {
            override fun onVerify(text: String) {
                if (!TextUtils.isEmpty(text)) {
                    modifyFamilyName(text)
                    editPopupWindow?.dismiss()
                }
            }
        }
    }

    /**
     * 显示删除家庭弹框
     */
    private fun showDeleteFamilyPopup() {
        if (deleteFamilyPopup == null) {
            deleteFamilyPopup = CommonPopupWindow(this)
            deleteFamilyPopup?.setCommonParams(
                getString(R.string.toast_delete_family_title),
                getString(R.string.toast_delete_family_content)
            )
        }
        deleteFamilyPopup?.setBg(family_bg)
        deleteFamilyPopup?.show(family_contain)
        deleteFamilyPopup?.onKeyListener = object : CommonPopupWindow.OnKeyListener {
            override fun cancel(popupWindow: CommonPopupWindow) {
                popupWindow.dismiss()
            }

            override fun confirm(popupWindow: CommonPopupWindow) {
                deleteFamily()
            }
        }
    }

    /**
     * 显示退出家庭弹框
     */
    private fun showExitFamilyPopup() {
        if (exitFamilyPopup == null) {
            exitFamilyPopup = CommonPopupWindow(this)
            exitFamilyPopup?.setCommonParams(
                getString(R.string.toast_exit_family_title),
                getString(R.string.toast_exit_family_content)
            )
        }
        exitFamilyPopup?.setBg(family_bg)
        exitFamilyPopup?.show(family_contain)
        exitFamilyPopup?.onKeyListener = object : CommonPopupWindow.OnKeyListener {
            override fun cancel(popupWindow: CommonPopupWindow) {
                popupWindow.dismiss()
            }

            override fun confirm(popupWindow: CommonPopupWindow) {
                exitFamily()
            }
        }
    }

    /**
     * 返回按下时
     */
    override fun onBackPressed() {
        editPopupWindow?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        deleteFamilyPopup?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        super.onBackPressed()
    }
}
