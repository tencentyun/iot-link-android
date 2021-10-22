package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface ChooseCountryView : ParentView {

    fun showCountryCode(countryCode: String, countryName: String)

}