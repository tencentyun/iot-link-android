package com.tenext.auth.service

import com.tenext.auth.callback.MyCallback
import com.tenext.auth.consts.RequestCode
import com.tenext.auth.impl.TimingImpl

internal class TimingService : BaseService(), TimingImpl {

    override fun timeList(
        productId: String, deviceName: String, offset: Int, callback: MyCallback
    ) {
        timeList(productId, deviceName, offset, 20, callback)
    }

    override fun timeList(
        productId: String,
        deviceName: String,
        offset: Int,
        limit: Int,
        callback: MyCallback
    ) {
        val param = tokenParams("AppGetTimerList")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["Offset"] = offset
        param["Limit"] = limit
        tokenPost(param, callback, RequestCode.time_list)
    }

    override fun createTimer(
        productId: String, deviceName: String, timerName: String, days: String, timePoint: String,
        repeat: Int, data: String, callback: MyCallback
    ) {
        val param = tokenParams("AppCreateTimer")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["TimerName"] = timerName
        param["Days"] = days
        param["TimePoint"] = timePoint
        param["Repeat"] = repeat
        param["Data"] = data
        tokenPost(param, callback, RequestCode.create_timer)
    }

    /**
     *  修改定时任务
     *  @param days 定时器开启时间，每一位——0:关闭,1:开启, 从左至右依次表示: 周日 周一 周二 周三 周四 周五 周六 1000000
     *  @param repeat 是否循环，0表示不需要，1表示需要
     */
    override fun modifyTimer(
        productId: String, deviceName: String, timerName: String, timerId: String, days: String,
        timePoint: String, repeat: Int, data: String, callback: MyCallback
    ) {
        val param = tokenParams("AppModifyTimer")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["TimerId"] = timerId
        param["TimerName"] = timerName
        param["Days"] = days
        param["TimePoint"] = timePoint
        param["Repeat"] = repeat
        param["Data"] = data
        tokenPost(param, callback, RequestCode.modify_timer)
    }

    /**
     *  修改定时任务状态，打开或者关闭
     *  @param status 0 关闭，1 开启
     */
    override fun modifyTimerStatus(
        productId: String, deviceName: String, timerId: String, status: Int, callback: MyCallback
    ) {
        val param = tokenParams("AppModifyTimerStatus")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["TimerId"] = timerId
        param["Status"] = status
        tokenPost(param, callback, RequestCode.modify_timer_status)
    }

    /**
     *  删除定时
     */
    override fun deleteTimer(
        productId: String, deviceName: String, timerId: String, callback: MyCallback
    ) {
        val param = tokenParams("AppDeleteTimer")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["TimerId"] = timerId
        tokenPost(param, callback, RequestCode.delete_timer)
    }

}