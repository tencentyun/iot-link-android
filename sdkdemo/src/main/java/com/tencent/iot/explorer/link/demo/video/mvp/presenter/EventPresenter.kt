package com.tencent.iot.explorer.link.demo.video.mvp.presenter

import com.tencent.iot.explorer.link.demo.common.mvp.ParentPresenter
import com.tencent.iot.explorer.link.demo.video.mvp.model.EventModel
import com.tencent.iot.explorer.link.demo.video.mvp.view.EventView
import java.util.*


class EventPresenter : ParentPresenter<EventModel, EventView> {
    constructor(view: EventView) : super(view)

    override fun getIModel(view: EventView): EventModel {
        return EventModel(view)
    }

    fun setAccessId(accessId: String) {
        model?.setAccessId(accessId)
    }

    fun setAccessToken(accessToken : String) {
        model?.setAccessToken(accessToken)
    }

    fun setProductId(productId : String) {
        model?.setProductId(productId)
    }

    fun setDeviceName(deviceName : String) {
        model?.setDeviceName(deviceName)
    }

    fun getDeviceName() : String {
        model?.let {
            return it.getDeviceName()
        }
        return ""
    }

    fun getCurrentDayEventsData() {
        model?.getCurrentDayEventsData()
    }

    fun setChannel(channel: Int) {
        model?.setChannel(channel)
    }

    fun getChannel() : Int {
        model?.let {
            return it.getChannel()
        }
        return 0
    }

    fun getEventsData(date: Date) {
        var startDate = Date(date.time)
        startDate.hours = 0
        startDate.minutes = 0
        startDate.seconds = 0
        var endDate = Date(date.time)
        endDate.hours = 23
        endDate.minutes = 59
        endDate.seconds = 59
        model?.getEventsData(startDate, endDate)
    }
}