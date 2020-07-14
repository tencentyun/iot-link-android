package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.entity.MessageEntity
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.response.MessageListResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.MessageView
import java.util.*

class MessageModel : ParentModel<MessageView>, MyCallback {

    val deviceList = LinkedList<MessageEntity>()
    val familyList = LinkedList<MessageEntity>()
    val notifyList = LinkedList<MessageEntity>()

    //列表到底标签
    private var isEnd = false
    //1设备 2家庭 3通知
    private var msgCategory = 1


    constructor(view: MessageView) : super(view)

    fun refreshMessageList(category: Int) {
        isEnd = false
        msgCategory = category
        when (category) {
            1 -> {
                deviceMessageList()
            }
            2 -> {
                familyMessageList()
            }
            3 -> {
                notifyMessageList()
            }
        }
    }

    /**
     * 设备消息
     */
    private fun deviceMessageList() {
        HttpRequest.instance.messageList(1, "", 0, 0, 20, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.e(msg ?: "")
                view?.refreshFail()
                view?.hideLoad()
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                view?.hideLoad()
                if (response.isSuccess()) {
                    response.parse(MessageListResponse::class.java)?.Data?.run {
                        isEnd = Listover
                        deviceList.clear()
                        deviceList.addAll(Msgs)
                        view?.refreshResponse(deviceList.size, "", true, isEnd)
                    }
                } else {
                    view?.refreshResponse(deviceList.size, response.msg, false, isEnd)
                }
            }
        })
    }

    /**
     * 家庭消息
     */
    private fun familyMessageList() {
        HttpRequest.instance.messageList(2, "", 0, 0, 20, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.e(msg ?: "")
                view?.refreshFail()
                view?.hideLoad()
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                view?.hideLoad()
                if (response.isSuccess()) {
                    response.parse(MessageListResponse::class.java)?.Data?.run {
                        isEnd = Listover
                        familyList.clear()
                        familyList.addAll(Msgs)
                        view?.refreshResponse(familyList.size, "", true, isEnd)
                    }

                } else {
                    view?.refreshResponse(familyList.size, response.msg, false, isEnd)
                }
            }
        })
    }

    /**
     * 通知消息
     */
    private fun notifyMessageList() {
        HttpRequest.instance.messageList(3, "", 0, 0, 20, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.e(msg ?: "")
                view?.refreshFail()
                view?.hideLoad()
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                view?.hideLoad()
                if (response.isSuccess()) {
                    response.parse(MessageListResponse::class.java)?.Data?.run {
                        isEnd = Listover
                        notifyList.clear()
                        notifyList.addAll(Msgs)
                        view?.refreshResponse(notifyList.size, "", true, isEnd)
                    }
                } else {
                    view?.refreshResponse(notifyList.size, response.msg, false, isEnd)
                }
            }
        })
    }

    /**
     * 加载更多
     */
    fun loadMessageList() {
        if (isEnd) {
            return
        }
        val messageList = when (msgCategory) {
            1 -> deviceList
            2 -> familyList
            else -> notifyList
        }
        HttpRequest.instance.messageList(
            msgCategory,
            messageList.last.MsgID,
            messageList.last.MsgTimestamp,
            messageList.size,
            20,
            this
        )
    }

    /**
     * 删除消息
     */
    fun deleteMessage(position: Int) {
        val messageList = when (msgCategory) {
            1 -> deviceList
            2 -> familyList
            else -> notifyList
        }
        HttpRequest.instance.deleteMessage(messageList[position].MsgID, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.e(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    messageList.removeAt(position)
                }
                view?.deleteResponse(response.msg, response.isSuccess())
                view?.refreshResponse(messageList.size, "", isSuccess = true, isEnd = false)
            }
        })
    }

    /**
     * 接受邀请或分享
     */
    fun acceptInvite(position: Int) {
        val messageList = when (msgCategory) {
            1 -> deviceList
            2 -> familyList
            else -> notifyList
        }
        messageList[position].run {
            Attachments?.ShareToken?.let {
                when (msgCategory) {
                    1 -> {//设备分享

                    }
                    2 -> {//家庭成员邀请
                        HttpRequest.instance.joinFamily(it, this@MessageModel)
                    }
                    3 -> {//通知
                        HttpRequest.instance.bindShareDevice(
                            ProductId, DeviceName, it, this@MessageModel
                        )
                    }
                }
            }
        }
    }

    /**
     * 拒绝邀请或分享
     */
    fun refuseInvite(position: Int) {
        when (msgCategory) {
            1 -> {//设备分享

            }
            2 -> {//家庭成员邀请

            }
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.message_list -> {
                if (response.isSuccess()) {
                    response.parse(MessageListResponse::class.java)?.Data?.run {
                        isEnd = Listover
                        when (msgCategory) {
                            1 -> deviceList.addAll(Msgs)
                            2 -> familyList.addAll(Msgs)
                            3 -> notifyList.addAll(Msgs)
                        }
                        view?.loadMoreResponse("", msgCategory, true, isEnd)
                    }
                } else {
                    view?.loadMoreResponse(response.msg, msgCategory, false, isEnd)
                }
            }
            RequestCode.join_family -> {//加入家庭
                view?.joinFamilyResponse(response.msg, response.isSuccess())
            }
            RequestCode.bind_share_device -> {//绑定设备
                view?.bindDeviceShareResponse(response.msg, response.isSuccess())
            }
        }
    }

}