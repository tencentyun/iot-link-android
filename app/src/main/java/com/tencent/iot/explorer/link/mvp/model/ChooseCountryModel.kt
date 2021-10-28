package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.SocketConstants
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.RegisterView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.mvp.view.ChooseCountryView

class ChooseCountryModel(view: ChooseCountryView) : ParentModel<ChooseCountryView>(view), MyCallback {

    private var countryCode = "86"
    private var countryName = T.getContext().getString(R.string.china_main_land) //"中国大陆"
    private var regionId = "1"
    private val type = SocketConstants.register

    fun setCountry(country: String) {
        country.split("+").let {
            this.countryName = it[0]
            this.regionId = it[1]
            this.countryCode = it[2]
            App.data.regionId = regionId
            App.data.region = it[3]
            view?.showCountryCode(this.countryCode, this.countryName)
        }
    }

    fun getCountryCode(): String {
        return countryCode
    }

    override fun fail(msg: String?, reqCode: Int) {
    }

    override fun success(response: BaseResponse, reqCode: Int) {
    }

}