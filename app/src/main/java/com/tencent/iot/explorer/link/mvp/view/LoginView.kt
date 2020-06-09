package com.mvp.view

import com.kitlink.entity.User
import com.mvp.ParentView

interface LoginView : ParentView {

    fun loginSuccess(user: User)

    fun loginFail(msg: String)

    fun showCountryCode(countryName: String, countryCode: String)

}