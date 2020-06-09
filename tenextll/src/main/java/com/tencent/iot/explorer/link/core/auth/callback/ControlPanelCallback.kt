package com.tencent.iot.explorer.link.core.auth.callback

import com.tencent.iot.explorer.link.core.auth.entity.ControlPanel

interface ControlPanelCallback {

    /**
     * 请求成功
     */
    fun success(panelList: List<ControlPanel>)

    /**
     * 数据刷新
     */
    fun refresh()

    /**
     * 请求失败
     */
    fun fail(msg: String)

}