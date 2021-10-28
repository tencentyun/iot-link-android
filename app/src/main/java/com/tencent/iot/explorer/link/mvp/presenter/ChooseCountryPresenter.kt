package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.model.RegisterModel
import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.ChooseCountryModel
import com.tencent.iot.explorer.link.mvp.view.ChooseCountryView
import com.tencent.iot.explorer.link.mvp.view.RegisterView

class ChooseCountryPresenter(view: ChooseCountryView) : ParentPresenter<ChooseCountryModel, ChooseCountryView>(view) {

    override fun getIModel(view: ChooseCountryView): ChooseCountryModel {
        return ChooseCountryModel(view)
    }

    fun setCountry(country: String) {
        model?.setCountry(country)
    }

    fun getCountryCode(): String {
        return model!!.getCountryCode()
    }

}