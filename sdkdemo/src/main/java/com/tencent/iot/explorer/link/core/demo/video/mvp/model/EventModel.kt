package com.tencent.iot.explorer.link.core.demo.video.mvp.model

import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.demo.mvp.ParentModel
import com.tencent.iot.explorer.link.core.demo.response.EventResponse
import com.tencent.iot.explorer.link.core.demo.video.entity.ActionRecord
import com.tencent.iot.explorer.link.core.demo.video.entity.ThumbnailBody
import com.tencent.iot.explorer.link.core.demo.video.mvp.view.EventView
import com.tencent.iot.video.link.callback.VideoCallback
import com.tencent.iot.video.link.consts.VideoRequestCode
import com.tencent.iot.video.link.service.VideoBaseService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class EventModel(view: EventView) : ParentModel<EventView>(view), VideoCallback {
    private var accessId: String = ""
    private var accessToken : String = ""
    private var productId: String = ""
    private var deviceName: String = ""
    private var channel: Int = 0
    private var coundownLatch: CountDownLatch? = null

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: String?, reqCode: Int) {
        when(reqCode) {
            VideoRequestCode.video_describe_record_date -> {
                var json = JSONObject.parseObject(response)
                json?.let {
                    var value = it.getJSONObject("Response")
                    value?.let {
                        var eventResp = JSONObject.parseObject(it.toJSONString(), EventResponse::class.java)
                        eventResp?.let {
                            readyDataAndCallback(it)
                        }
                    }
                }
            }

            VideoRequestCode.video_describe_snapshot -> {

            }
        }
    }

    private fun readyDataAndCallback(eventResponse : EventResponse) {
        val events : MutableList<ActionRecord> = CopyOnWriteArrayList()
        coundownLatch =  CountDownLatch(eventResponse.events.size)
        GlobalScope.launch {
            for (i in 0 until eventResponse.events.size) {
                var item = ActionRecord()
                var startDate = Date(eventResponse.events.get(i).startTime * 1000)
                item.time = String.format("%02d:%02d", startDate.hours, startDate.minutes)
                item.startTime = eventResponse.events.get(i).startTime
                item.endTime = eventResponse.events.get(i).endTime
                item.action = eventResponse.events.get(i).eventId
                VideoBaseService(accessId, accessToken).getSnapshotUrl(productId, deviceName, eventResponse.events.get(i).thumbnail,
                    object : VideoCallback {
                        override fun fail(msg: String?, reqCode: Int) {
                            coundownLatch?.countDown()
                        }

                        override fun success(response: String?, reqCode: Int) {
                            var json = JSONObject.parseObject(response)
                            json?.let {
                                var value = it.getJSONObject("Response")
                                var thumbnailBody = JSON.parseObject(value.toJSONString(), ThumbnailBody::class.java)
                                thumbnailBody?.let {
                                    item.snapshotUrl = URLDecoder.decode(it.thumbnailURL)
                                }
                            }
                            coundownLatch?.countDown()
                        }

                    })
                events.add(item)
                delay(50) // 挂起协程，用于控制一秒钟最大的请求次数（后台有请求频率限制）
            }
        }

        Thread(Runnable{
            val await = coundownLatch?.await(60, TimeUnit.SECONDS)
            await?.let {
                if (it) {  // 成功进行所有请求，才对数据做返回
                    view?.eventReady(events)
                }
            }
        }).start()
    }

    fun setAccessId(accessId: String) {
        this.accessId = accessId
    }

    fun setAccessToken(accessToken : String) {
        this.accessToken = accessToken
    }

    fun setProductId(productId : String) {
        this.productId = productId
    }

    fun setDeviceName(deviceName : String) {
        this.deviceName = deviceName
    }

    fun getDeviceName() : String {
        return this.deviceName
    }

    fun setChannel(channel: Int) {
        this.channel = channel
    }

    fun getChannel(): Int {
        return this.channel
    }

    fun getSnapshotUrl(thumbnail : String) {
        VideoBaseService(accessId, accessToken).getSnapshotUrl(productId, deviceName, thumbnail, this)
    }

    fun getCurrentDayEventsData() {
        VideoBaseService(accessId, accessToken).getIPCCurrentDayRecordData(productId, deviceName, this)
    }

    fun getEventsData(startDate: Date, endDate: Date) {
        VideoBaseService(accessId, accessToken).getIPCRecordData(productId, deviceName, startDate, endDate, this)
    }
}