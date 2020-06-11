package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface MessageView : ParentView {

    fun deleteResponse(msg: String?, isSuccess: Boolean)
    /**
     * 列表刷新成功
     */
    fun refreshResponse(size: Int, msg: String?, isSuccess: Boolean, isEnd: Boolean)

    /**
     * 列表刷新失败
     */
    fun refreshFail()

    /**
     * 加载更多
     */
    fun loadMoreResponse(msg: String?, category: Int, isSuccess: Boolean, isEnd: Boolean)

    /**
     * 加入家庭
     */
    fun joinFamilyResponse(msg: String?, isSuccess: Boolean)

    /**
     * 绑定设备
     */
    fun bindDeviceShareResponse(msg: String?, isSuccess: Boolean)

    fun showLoad()

    fun hideLoad()

}