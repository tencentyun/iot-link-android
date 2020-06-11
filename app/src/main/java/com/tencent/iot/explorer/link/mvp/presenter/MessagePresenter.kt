package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.kitlink.entity.MessageEntity
import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.MessageModel
import com.tencent.iot.explorer.link.mvp.view.MessageView

class MessagePresenter : ParentPresenter<MessageModel, MessageView> {
    constructor(view: MessageView) : super(view)

    override fun getIModel(view: MessageView): MessageModel {
        return MessageModel(view)
    }

    fun getList(category: Int): List<MessageEntity> {
        return when (category) {
            1 -> model!!.deviceList
            2 -> model!!.familyList
            else -> model!!.notifyList
        }
    }

    fun getMsgEntity(position: Int, category: Int): MessageEntity {
        return when (category) {
            1 -> model!!.deviceList[position]
            2 -> model!!.familyList[position]
            else -> model!!.notifyList[position]
        }
    }

    fun refreshMessageList(category: Int) {
        model?.refreshMessageList(category)
    }

    fun loadMessageList() {
        model?.loadMessageList()
    }

    fun refuseInvite(position: Int) {
        model?.refuseInvite(position)
    }

    fun acceptInvite(position: Int) {
        model?.acceptInvite(position)
    }

    fun deleteMessage(position: Int) {
        model?.deleteMessage(position)
    }

}