package com.tencent.iot.explorer.link.demo.core.activity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.consts.RequestCode
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.MessageListResponse
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.adapter.MessageAdapter
import com.tencent.iot.explorer.link.demo.core.adapter.OnItemListener
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.core.link.entity.MessageEntity
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityMessageBinding

/**
 * 消息通知
 */
class MessageActivity : BaseActivity<ActivityMessageBinding>(), MyCallback {

    val messageList = arrayListOf<MessageEntity>()
    //1设备 2家庭 3通知
    private var msgCategory = 1
    private lateinit var adapter: MessageAdapter

    override fun getViewBinding(): ActivityMessageBinding = ActivityMessageBinding.inflate(layoutInflater)

    override fun initView() {
        with(binding) {
            menuMessage.tvTitle.text = getString(R.string.message_notify)

            rvMessageList.layoutManager = LinearLayoutManager(this@MessageActivity)
            adapter = MessageAdapter(this@MessageActivity, messageList)
            rvMessageList.adapter = adapter
        }

        requestMessage()
    }

    override fun setListener() {
        binding.menuMessage.ivBack.setOnClickListener { finish() }
        binding.tabMessage.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
            override fun onItemClick(holder: BaseHolder<*, *>, clickView: View, position: Int) {
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
            with(binding) {
                if (messageList.isNotEmpty()) {
                    rvMessageList.visibility = View.VISIBLE
                    tvEmptyMessage.visibility = View.GONE
                } else {
                    rvMessageList.visibility = View.GONE
                    tvEmptyMessage.visibility = View.VISIBLE
                }
            }
            // todo 可优化
            adapter.notifyDataSetChanged()
        }
    }
}
