package com.tenext.auth.impl

import com.tenext.auth.callback.MyCallback

interface TimingImpl {

    /**
     * 云端定时列表
     */
    fun timeList(productId: String, deviceName: String, offset: Int, callback: MyCallback)

    /**
     * 云端定时列表
     */
    fun timeList(
        productId: String,
        deviceName: String,
        offset: Int,
        limit: Int,
        callback: MyCallback
    )

    /**
     *  创建定时任务
     */
    fun createTimer(
        productId: String, deviceName: String, timerName: String, days: String, timePoint: String,
        repeat: Int, data: String, callback: MyCallback
    )

    /**
     *  修改定时任务
     *  @param days 定时器开启时间，每一位——0:关闭,1:开启, 从左至右依次表示: 周日 周一 周二 周三 周四 周五 周六 1000000
     *  @param repeat 是否循环，0表示不需要，1表示需要
     */
    fun modifyTimer(
        productId: String, deviceName: String, timerName: String, timerId: String, days: String,
        timePoint: String, repeat: Int, data: String, callback: MyCallback
    )

    /**
     *  修改定时任务状态，打开或者关闭
     *  @param status 0 关闭，1 开启
     */
    fun modifyTimerStatus(
        productId: String, deviceName: String, timerId: String, status: Int, callback: MyCallback
    )

    /**
     *  删除定时
     */
    fun deleteTimer(productId: String, deviceName: String, timerId: String, callback: MyCallback)

}