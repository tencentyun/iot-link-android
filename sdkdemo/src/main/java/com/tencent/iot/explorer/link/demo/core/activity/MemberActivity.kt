package com.tencent.iot.explorer.link.demo.core.activity

import android.text.TextUtils
import android.view.View
import com.tencent.iot.explorer.link.demo.core.popup.CommonPopupWindow
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.core.link.entity.MemberEntity
import kotlinx.android.synthetic.main.activity_member.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 成员详情
 */
class MemberActivity : BaseActivity(), MyCallback {

    private var deleteMemberPopup: CommonPopupWindow? = null

    private var member: MemberEntity? = null
    private var role = 0

    override fun getContentView(): Int {
        return R.layout.activity_member
    }

    override fun initView() {
        tv_title.text = getString(R.string.member_setting)
        member = get("member")
        role = get<FamilyEntity>("family")?.Role ?: 0
        showMemberInfo()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_delete_member.setOnClickListener { showDeletePopup() }

    }

    /**
     * 显示成员信息
     */
    private fun showMemberInfo() {
        member?.run {
            tv_member_name.text = NickName
            tv_member_account.text = UserID
            tv_member_role.text = if (Role == 1) {
                getString(R.string.role_owner)
            } else {
                getString(R.string.role_member)
            }
            //不是所有者不能展示、是本人也不展示
            tv_delete_member.visibility = if (role != 1 || UserID == App.data.userInfo.UserID) {
                View.GONE
            } else {
                View.VISIBLE
            }
            if (!TextUtils.isEmpty(Avatar))
                Picasso.get().load(Avatar).into(iv_member_portrait)
        }
    }

    private fun showDeletePopup() {
        if (deleteMemberPopup == null) {
            deleteMemberPopup = CommonPopupWindow(this)
            deleteMemberPopup!!.setCommonParams(
                getString(R.string.toast_delete_member_title),
                getString(R.string.toast_delete_member_content)
            )
        }
        deleteMemberPopup?.setBg(member_bg)
        deleteMemberPopup?.show(member_contain)
        deleteMemberPopup?.onKeyListener = object : CommonPopupWindow.OnKeyListener {
            override fun cancel(popupWindow: CommonPopupWindow) {
                popupWindow.dismiss()
            }

            override fun confirm(popupWindow: CommonPopupWindow) {
                deleteMember()
            }
        }
    }

    /**
     * 移除成员
     */
    private fun deleteMember() {
        member?.run {
            get<FamilyEntity>("family")?.let {
                IoTAuth.memberImpl.deleteFamilyMember(it.FamilyId, UserID, this@MemberActivity)
            }
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            deleteMemberPopup?.dismiss()
            finish()
        }
    }

    override fun onBackPressed() {
        deleteMemberPopup?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        super.onBackPressed()
    }
}
