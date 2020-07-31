package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.kitlink.entity.User
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.mvp.ParentView

interface LoginView : ParentView {

    fun loginSuccess(user: User)

    fun loginFail(msg: String)

    fun loginFail(response: BaseResponse)

    fun showCountryCode(countryName: String, countryCode: String)

}