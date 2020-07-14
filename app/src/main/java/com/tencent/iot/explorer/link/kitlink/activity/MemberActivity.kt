package com.tencent.iot.explorer.link.kitlink.activity

import android.view.View
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.entity.MemberEntity
import com.tencent.iot.explorer.link.kitlink.popup.CommonPopupWindow
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.entity.FamilyEntity
import com.tencent.iot.explorer.link.util.picture.imp.ImageManager
import kotlinx.android.synthetic.main.activity_member.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 成员详情
 */
class MemberActivity : BaseActivity(), MyCallback {

    private var deleteMemberPopup: CommonPopupWindow? = null

    private var memberEntity: MemberEntity? = null

    //我在房间的权限
    private var familyEntity: FamilyEntity? = null

    override fun getContentView(): Int {
        return R.layout.activity_member
    }

    override fun initView() {
        memberEntity = get("member")
        familyEntity = get("family")
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.member_setting)
        showMemberInfo()
    }

    /**
     * 显示成员信息
     */
    private fun showMemberInfo() {
        memberEntity?.run {
            tv_member_name.text = NickName
            tv_member_account.text = UserID
            tv_member_role.text = if (Role == 1) {
                getString(R.string.role_owner)
            } else {
                getString(R.string.role_member)
            }
            //不是所有者不能展示、是本人也不展示
            tv_delete_member.visibility =
                if (familyEntity?.Role ?: "0" != 1 || UserID == App.data.userInfo.UserID) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            ImageManager.setImagePath(
                this@MemberActivity,
                iv_member_portrait,
                Avatar,
                R.mipmap.image_default_portrait
            )
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_delete_member.setOnClickListener { showDeletePopup() }
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
        deleteMemberPopup?.show(member)
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
        familyEntity?.let {
            memberEntity?.run {
                HttpRequest.instance.deleteFamilyMember(it.FamilyId, UserID, this@MemberActivity)
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
