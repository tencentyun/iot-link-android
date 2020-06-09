package com.tenext.demo.activity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.consts.RequestCode
import com.tenext.auth.response.BaseResponse
import com.tenext.demo.R
import com.tenext.demo.adapter.MessageAdapter
import com.tenext.demo.adapter.OnItemListener
import com.tenext.demo.entity.IotMessage
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.log.L
import com.tenext.demo.response.MessageListResponse
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 消息通知
 */
class MessageActivity : BaseActivity(), MyCallback {

    val messageList = arrayListOf<IotMessage>()
    //1设备 2家庭 3通知
    private var msgCategory = 1
    private lateinit var adapter: MessageAdapter


    override fun getContentView(): Int {
        return R.layout.activity_message
    }

    override fun initView() {
        tv_title.text = getString(R.string.message_notify)

        rv_message_list.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter(this, messageList)
        rv_message_list.adapter = adapter

        requestMessage()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tab_message.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                msgCategory = tab.position + 1
                requestMessage()
            }
        })
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                when (clickView.tag) {
                    0 -> {
                        refuseInvite(position)
                    }
                    1 -> {
                        acceptInvite(position)
                    }
                    2 -> deleteMessage(position)
                }
            }
        })
    }

    /**
     * 请求消息列表
     */
    private fun requestMessage() {
        IoTAuth.messageImpl.messageList(msgCategory, "", 0, this@MessageActivity)
    }

    /**
     * 删除消息
     */
    private fun deleteMessage(position: Int) {
        IoTAuth.messageImpl.deleteMessage(messageList[position].MsgID, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.e(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                messageList.removeAt(position)
                runOnUiThread {
                    show("删除成功")
                    showMessage()
                }
            }
        })
    }

    /**
     * 接受邀请或分享
     */
    fun acceptInvite(position: Int) {
        messageList[position].run {
            Attachments?.ShareToken?.let {
                when (msgCategory) {
                    3 -> {//设备分享
                        IoTAuth.shareImpl.bindShareDevice(
                            ProductId,
                            DeviceName,
                            it,
                            this@MessageActivity
                        )
                    }
                    2 -> {//家庭成员邀请
                        IoTAuth.memberImpl.joinFamily(it, this@MessageActivity)
                    }
                }
            }
        }
    }

    /**
     * 拒绝邀请或分享
     */
    fun refuseInvite(position: Int) {
        finish()
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
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.message_list -> {
                    response.parse(MessageListResponse::class.java)?.Data?.run {
                        messageList.clear()
                        messageList.addAll(Msgs)
                        showMessage()
                    }
                }
                RequestCode.bind_share_device->{
                    show("绑定成功")
                }
                RequestCode.join_family->{
                    show("成功加入家庭")
                }
            }
        }else{
            show(response.msg)
        }
    }

    private fun showMessage() {
        runOnUiThread {
            if (messageList.isNotEmpty()) {
                rv_message_list.visibility = View.VISIBLE
                tv_empty_message.visibility = View.GONE
            } else {
                rv_message_list.visibility = View.GONE
                tv_empty_message.visibility = View.VISIBLE
            }
            adapter.notifyDataSetChanged()
        }
    }
}
