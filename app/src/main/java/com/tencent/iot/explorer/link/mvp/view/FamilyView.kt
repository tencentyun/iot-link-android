package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface FamilyView:ParentView {

    fun showFamilyInfo()

    fun showMemberList()

    /**
     * 删除家庭或离开家庭成功
     */
    fun deleteFamilySuccess()

}