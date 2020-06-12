package com.tencent.iot.explorer.link.mvp.model

import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.kitlink.entity.DeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.TimerListEntity
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.response.TimerListResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.TimerListView
import com.tencent.iot.explorer.link.util.L
import com.tencent.iot.explorer.link.util.T

/**
 * 云端定时列表业务
 */
class TimerListModel(view: TimerListView) : ParentModel<TimerListView>(view), MyCallback {

    val timerList = arrayListOf<TimerListEntity>()
    var deviceEntity: DeviceEntity? = null

    private var modifyPosition = -1
    private var deletePosition = -1
    private var total = 0

    /**
     *  刷新定时列表
     */
    fun refreshTimerList() {
        timerList.clear()
        total = 0
        loadTimerList()
    }

    /**
     * 加载定时列表
     */
    fun loadTimerList() {
        if (total > 0 && timerList.size > total) {
            return
        }
        deviceEntity?.run {
            HttpRequest.instance.timeList(
                ProductId,
                DeviceName,
                timerList.size,
                this@TimerListModel
            )
        }
    }

    /**
     * 改变定时任务状态，打开或者关闭
     */
    fun switchTimer(position: Int) {
        modifyPosition = position
        timerList[position].run {
            val status = if (Status == 0) 1 else 0
            HttpRequest.instance.modifyTimerStatus(
                ProductId,
                DeviceName,
                TimerId,
                status,
                this@TimerListModel
            )
        }
    }

    /**
     * 删除定时
     */
    fun deleteCloudTiming(position: Int) {
        timerList[position].run {
            deletePosition = position
            HttpRequest.instance.deleteTimer(ProductId, DeviceName, TimerId, this@TimerListModel)
        }
    }


    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.time_list -> {
                if (response.isSuccess()) {
                    response.parse(TimerListResponse::class.java)?.run {
                        total = Total
                        timerList.addAll(TimerList)
                        L.e("timerList=${JSON.toJSONString(timerList)}")
                        view?.showTimerList(timerList.size)
                    }
                }
            }
            RequestCode.modify_timer_status -> {
                if (response.isSuccess()) {
                    timerList[modifyPosition].run {
                        Status = if (Status == 0) 1 else 0
                        view?.showTimerList(timerList.size)
                    }
                }
            }
            RequestCode.delete_timer -> {
                if (response.isSuccess()) {
                    if (deletePosition >= 0)
                        timerList.removeAt(deletePosition)
                    deletePosition = -1
                    T.show("删除成功")
                    view?.showTimerList(timerList.size)
                }
            }
        }
    }
}