package com.tencent.iot.explorer.link.core.demo.video.mvp.model

import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.demo.mvp.ParentModel
import com.tencent.iot.explorer.link.core.demo.response.EventResponse
import com.tencent.iot.explorer.link.core.demo.video.entity.ActionRecord
import com.tencent.iot.explorer.link.core.demo.video.mvp.view.EventView
import com.tencent.iot.video.link.callback.VideoCallback
import com.tencent.iot.video.link.service.VideoBaseService
import java.util.*
import kotlin.collections.ArrayList
import com.alibaba.fastjson.JSONObject.parseObject as parseObject1

class EventModel(view: EventView) : ParentModel<EventView>(view), VideoCallback {

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: String?, reqCode: Int) {
        Log.e("XXX", "response " + response)
        var json = JSONObject.parseObject(response)
        json?.let {
            var value = it.getJSONObject("Response")
            value?.let {
                var eventResp = JSONObject.parseObject(it.toJSONString(), EventResponse::class.java)
                eventResp?.let {
                    var events : MutableList<ActionRecord> = ArrayList()
                    for (i in 0 until it.events.size) {
                        var item = ActionRecord()
                        var startDate = Date(it.events.get(i).startTime)
                        item.time = String.format("%02d:%02d", startDate.hours, startDate.minutes)
                        events.add(item)
                    }
                    view?.eventReady(events)
                }
            }
        }
    }

    fun getCurrentDayEventsData(accessId: String, accessToken: String, productId: String, deviceName: String) {
        VideoBaseService(accessId, accessToken).getIPCCurrentDayRecordData(productId, deviceName, this)
    }

    fun getEventsData(accessId: String, accessToken: String, productId: String, startDate: Date, endDate: Date, deviceName: String) {
        VideoBaseService(accessId, accessToken).getIPCRecordData(productId, deviceName, startDate, endDate, this)
    }
}