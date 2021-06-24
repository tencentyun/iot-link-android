package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.EditNameValue
import com.tencent.iot.explorer.link.kitlink.entity.Postion
import com.tencent.iot.explorer.link.kitlink.holder.FamilyFootHolder
import com.tencent.iot.explorer.link.kitlink.holder.FamilyInfoHeaderHolder
import com.tencent.iot.explorer.link.kitlink.holder.MemberListViewHolder
import com.tencent.iot.explorer.link.kitlink.popup.CommonPopupWindow
import com.tencent.iot.explorer.link.kitlink.popup.EditPopupWindow
import com.tencent.iot.explorer.link.mvp.IModel
import com.tencent.iot.explorer.link.mvp.model.FamilyModel
import com.tencent.iot.explorer.link.mvp.view.FamilyView
import kotlinx.android.synthetic.main.activity_add_family.*
import kotlinx.android.synthetic.main.activity_family.*
import kotlinx.android.synthetic.main.foot_family.view.*
import kotlinx.android.synthetic.main.head_family.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 家庭详情
 */
class FamilyActivity : MActivity(), FamilyView, CRecyclerView.RecyclerItemView {

    private var familyEntity: FamilyEntity? = null
    private var canDelete = true

    private lateinit var model: FamilyModel

    private lateinit var headerHolder: FamilyInfoHeaderHolder
    private lateinit var footHolder: FamilyFootHolder

    private var editPopupWindow: EditPopupWindow? = null
    private var deleteFamilyPopup: CommonPopupWindow? = null
    private var exitFamilyPopup: CommonPopupWindow? = null

    override fun getModel(): IModel? {
        return model
    }

    override fun getContentView(): Int {
        return R.layout.activity_family
    }

    override fun onResume() {
        model.getFamilyInfo()
        model.refreshMemberList()
        model.getFamilyRooms()
        super.onResume()
    }

    override fun initView() {
        model = FamilyModel(this)
        familyEntity = get("family")
        canDelete = App.data.familyList.size > 0
        tv_title.text = getString(R.string.family_detail)
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        val layoutManager = LinearLayoutManager(this)
        crv_member_list.layoutManager = layoutManager
        crv_member_list.setList(model.memberList)
        crv_member_list.addRecyclerItemView(this)
        model.familyEntity = familyEntity
        addHeader()
        addFooter()
    }

    /**
     * 添加头部
     */
    private fun addHeader() {
        headerHolder = FamilyInfoHeaderHolder(this, crv_member_list, R.layout.head_family)
        headerHolder.data = familyEntity
        crv_member_list.addHeader(headerHolder)
        headerHolder.headListener = object : CRecyclerView.HeadListener {
            override fun doAction(
                holder: CRecyclerView.HeadViewHolder<*>,
                clickView: View,
                position: Int
            ) {
                when (position) {
                    0 ->  {
                        if (familyEntity?.Role == 1) {
                            modifyFamilyName()
                        }
                    }
                    1 -> {
                        jumpActivity(RoomListActivity::class.java)
                    }
                    2 -> {
                        val intent = Intent(this@FamilyActivity, MarkerPaddingActivity::class.java)
                        var bundle = Bundle()
                        bundle.putString(CommonField.ADDRESS, headerHolder.data?.Address)
                        bundle.putString(CommonField.FAMILY_ID, familyEntity?.FamilyId)
                        bundle.putString(CommonField.FAMILY_NAME, familyEntity?.FamilyName)
                        intent.putExtra(CommonField.ADDRESS, bundle)
                        startActivityForResult(intent, CommonField.MAP_LOCATION_REQ_CODE)
                    }
                    3 -> {
                        jumpActivity(InviteMemberActivity::class.java)
                    }
                }
            }
        }
        if (familyEntity?.Role == 1) {
            headerHolder.itemView.iv_family_name_set.visibility = View.VISIBLE
        } else {
            headerHolder.itemView.iv_family_name_set.visibility = View.GONE
        }
    }

    /**
     * 添加底部
     */
    private fun addFooter() {
        footHolder = FamilyFootHolder(this, crv_member_list, R.layout.foot_family)
        footHolder.itemView.tv_delete_family.text = if (familyEntity?.Role ?: 0 == 1) {
            if (!canDelete)
                footHolder.itemView.tv_delete_family.alpha = 0.5f
            getString(R.string.delete_family)
        } else {
            getString(R.string.exit_family)
        }
        footHolder.footListener = object : CRecyclerView.FootListener {
            override fun doAction(
                holder: CRecyclerView.FootViewHolder<*>,
                clickView: View,
                position: Int
            ) {
                if (familyEntity?.Role ?: 0 == 1) {
                    if (canDelete)
                        showDeleteFamilyPopup()
                } else {
                    showExitFamilyPopup()
                }
            }
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    /**
     * 显示家庭详情
     */
    override fun showFamilyInfo() {
        editPopupWindow?.dismiss()
        headerHolder.data?.Address = model.familyInfoEntity.Address
        crv_member_list.notifyDataChanged()
    }

    /**
     * 显示成员列表
     */
    override fun showMemberList() {
        crv_member_list.notifyDataChanged()
        crv_member_list.addFooter(footHolder)
    }

    /**
     *  删除家庭成功
     */
    override fun deleteFamilySuccess() {
        editPopupWindow?.dismiss()
        deleteFamilyPopup?.dismiss()
        exitFamilyPopup?.dismiss()
        App.data.familyList.remove(familyEntity)
        App.data.setRefreshLevel(0)
        finish()
    }

    override fun showFamilyRoomsInfo() {
        var num = model.roomList.size
        headerHolder.data?.RoomsNum = getString(R.string.num_rooms, num.toString())
        crv_member_list.notifyDataChanged()
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        if (model.memberList.size > position) {
            put("member", model.memberList[position])
            jumpActivity(MemberActivity::class.java)
        }
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return MemberListViewHolder(this, parent, R.layout.item_member)
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

    private fun modifyFamilyName() {
        var intent = Intent(this@FamilyActivity, EditNameActivity::class.java)
        var editNameValue = EditNameValue()
        editNameValue.name = familyEntity?.FamilyName ?: ""
        editNameValue.title = getString(R.string.family_setting)
        editNameValue.tipName = getString(R.string.family_name)
        editNameValue.btn = getString(R.string.save)
        editNameValue.errorTip = getString(R.string.toast_name_length)
        intent.putExtra(CommonField.EXTRA_INFO, JSON.toJSONString(editNameValue))
        startActivityForResult(intent, CommonField.EDIT_NAME_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CommonField.EDIT_NAME_REQ_CODE &&
            resultCode == Activity.RESULT_OK && data != null) {
            var extraInfo = data?.getStringExtra(CommonField.EXTRA_TEXT)
            model.modifyFamilyName(extraInfo)
        } else if (requestCode == CommonField.MAP_LOCATION_REQ_CODE && resultCode == RESULT_OK) {
            var ret = data?.getStringExtra(CommonField.ADDRESS) ?: ""
            var familyPostion = JSON.parseObject(ret, Postion::class.java)
            if (familyPostion == null)  return

            headerHolder.data?.Address = familyPostion.address
            crv_member_list.notifyDataChanged()
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
        deleteFamilyPopup?.show(family)
        deleteFamilyPopup?.onKeyListener = object : CommonPopupWindow.OnKeyListener {
            override fun cancel(popupWindow: CommonPopupWindow) {
                popupWindow.dismiss()
            }

            override fun confirm(popupWindow: CommonPopupWindow) {
                model.deleteFamily()
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
            exitFamilyPopup?.setMenuText(getString(R.string.cancel), getString(R.string.exit))
        }
        exitFamilyPopup?.setBg(family_bg)
        exitFamilyPopup?.show(family)
        exitFamilyPopup?.onKeyListener = object : CommonPopupWindow.OnKeyListener {
            override fun cancel(popupWindow: CommonPopupWindow) {
                popupWindow.dismiss()
            }

            override fun confirm(popupWindow: CommonPopupWindow) {
                model.exitFamily()
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

    override fun onDestroy() {
        familyEntity = null
        super.onDestroy()
    }

}
