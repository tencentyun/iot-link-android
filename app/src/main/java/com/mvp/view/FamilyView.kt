package com.mvp.view

import com.mvp.ParentView

interface FamilyView:ParentView {

    fun showFamilyInfo()

    fun showMemberList()

    /**
     * 删除家庭或离开家庭成功
     */
    fun deleteFamilySuccess()

}