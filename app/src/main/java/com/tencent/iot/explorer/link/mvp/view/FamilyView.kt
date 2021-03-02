package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface FamilyView:ParentView {

    fun showFamilyInfo()

    fun showMemberList()

    fun deleteFamilySuccess()

    fun showFamilyRoomsInfo()
}