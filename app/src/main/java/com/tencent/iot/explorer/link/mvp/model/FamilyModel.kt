package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.kitlink.entity.FamilyEntity
import com.tencent.iot.explorer.link.kitlink.entity.FamilyInfoEntity
import com.tencent.iot.explorer.link.kitlink.entity.MemberEntity
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.response.FamilyInfoResponse
import com.tencent.iot.explorer.link.kitlink.response.MemberListResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.FamilyView
import com.util.L
import com.util.T

/**
 * 家庭详情业务
 */
class FamilyModel(view: FamilyView) : ParentModel<FamilyView>(view), MyCallback {

    val memberList = arrayListOf<MemberEntity>()
    private var total = 0
    val familyInfoEntity = FamilyInfoEntity()
    var familyEntity: FamilyEntity? = null

    /**
     * 家庭详情信息
     */
    fun getFamilyInfo() {
        familyEntity?.let {
            HttpRequest.instance.familyInfo(it.FamilyId, this)
        }
    }

    /**
     *  获取家庭成员列表
     */
    fun refreshMemberList() {
        memberList.clear()
        loadMoreMemberList()
    }

    /**
     *  获取家庭成员列表
     */
    fun loadMoreMemberList() {
        familyEntity?.let {
            if (memberList.size > 0 && memberList.size >= total) return
            HttpRequest.instance.memberList(it.FamilyId, memberList.size, this)
        }
    }

    /**
     *  修改家庭名称
     */
    fun modifyFamilyName(familyName: String) {
        familyEntity?.let {
            HttpRequest.instance.modifyFamily(it.FamilyId, familyName, "", this)
        }
    }

    /**
     * 删除家庭
     */
    fun deleteFamily() {
        familyInfoEntity.run {
            familyEntity?.let {
                if (it.Role == 1) {//管理员
                    HttpRequest.instance.deleteFamily(FamilyId, FamilyName, this@FamilyModel)
                }
            }
        }
    }

    /**
     * 退出家庭
     */
    fun exitFamily() {
        familyInfoEntity.run {
            familyEntity?.let {
                if (it.Role == 0) {//管理员
                    HttpRequest.instance.exitFamily(FamilyId, this@FamilyModel)
                }
            }
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.family_info -> {
                if (response.isSuccess()) {
                    response.parse(FamilyInfoResponse::class.java)?.Data?.run {
                        familyInfoEntity.FamilyId = FamilyId
                        familyInfoEntity.FamilyName = FamilyName
                        familyInfoEntity.CreateTime = CreateTime
                        familyInfoEntity.UpdateTime = UpdateTime
                        familyInfoEntity.Address = Address
                        familyEntity?.FamilyName = FamilyName
                        view?.showFamilyInfo()
                    }
                }
            }
            RequestCode.delete_family, RequestCode.exit_family -> {
                if (response.isSuccess()) {
                    view?.deleteFamilySuccess()
                } else {
                    T.show(response.msg)
                }
            }
            RequestCode.member_list -> {
                if (response.isSuccess()) {
                    response.parse(MemberListResponse::class.java)?.run {
                        total = Total
                        memberList.addAll(MemberList)
                        view?.showMemberList()
                    }
                }

            }
            RequestCode.modify_family -> if (response.isSuccess()) getFamilyInfo()
        }
    }
}