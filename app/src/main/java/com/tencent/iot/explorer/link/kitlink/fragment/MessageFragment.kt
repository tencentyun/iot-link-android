package com.tencent.iot.explorer.link.kitlink.fragment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.link.entity.MessageEntity
import com.tencent.iot.explorer.link.kitlink.holder.MsgDeviceViewHolder
import com.tencent.iot.explorer.link.kitlink.holder.MsgFamilyViewHolder
import com.tencent.iot.explorer.link.kitlink.holder.MsgNotifyViewHolder
import com.tencent.iot.explorer.link.core.auth.response.MessageListResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.fragment_message.*
import java.util.*

/**
 * 消息列表 fragment
 */
class MessageFragment(category: Int) : BaseFragment(), CRecyclerView.RecyclerItemView, MyCallback {

    private var isEnd = false
    private var mCategory = category
    private val messageList = LinkedList<MessageEntity>()

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_message
    }

    override fun startHere(view: View) {

        crv_message_list.setList(messageList)
        crv_message_list.layoutManager = LinearLayoutManager(activity)
        crv_message_list.addRecyclerItemView(this)

        srl_message_list.autoRefresh()
        setListener()
    }

    private fun setListener() {
        //刷新加载
        srl_message_list.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                loadMessageList()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                refreshMessageList()
            }
        })
    }

    /**
     * 设备消息
     */
    private fun refreshMessageList() {
        isEnd = false
        HttpRequest.instance.messageList(mCategory, "", 0, 0, 20, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.e(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(MessageListResponse::class.java)?.Data?.run {
                        isEnd = Listover
                        messageList.clear()
                        messageList.addAll(Msgs)
                        refreshFinish(true)
                    }
                } else {
                    refreshFinish(false)
                }
            }
        })
    }

    /**
     * 加载更多
     */
    private fun loadMessageList() {
        if (isEnd) {
            return
        }
        HttpRequest.instance.messageList(
            mCategory,
            messageList.last.MsgID,
            messageList.last.MsgTimestamp,
            messageList.size,
            20,
            this
        )
    }

    /**
     * 接受邀请或分享
     */
    private fun acceptInvite(position: Int) {
        messageList[position].run {
            Attachments?.ShareToken?.let {
                when (mCategory) {
                    1 -> {//设备分享

                    }
                    2 -> {//家庭成员邀请
                        HttpRequest.instance.joinFamily(it, this@MessageFragment)
                    }
                    3 -> {//通知
                        HttpRequest.instance.bindShareDevice(
                            ProductId, DeviceName, it, this@MessageFragment
                        )
                    }
                }
            }
        }
    }

    /**
     * 删除消息
     */
    private fun deleteMessage(position: Int) {
        HttpRequest.instance.deleteMessage(messageList[position].MsgID, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.e(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    messageList.removeAt(position)
                }
                deleteResponse(response.msg, response.isSuccess())
                showList()
            }
        })
    }

    /**
     * 删除消息
     */
    private fun deleteResponse(msg: String?, isSuccess: Boolean) {
        if (isSuccess) {
            show(getString(R.string.delete_success))

        } else {
            show(msg)
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
                        messageList.addAll(Msgs)
                        loadFinish(true)
                    }
                } else {
                    loadFinish(true)
                }
            }
            RequestCode.join_family -> {//加入家庭
                joinFamilyResponse(response.msg, response.isSuccess())
            }
            RequestCode.bind_share_device -> {//绑定设备
                bindDeviceShareResponse(response.msg, response.isSuccess())
            }
        }
    }

    private fun refreshFinish(success: Boolean) {
        srl_message_list?.finishRefresh(0, success, isEnd)
        showList()
    }

    private fun loadFinish(success: Boolean) {
        srl_message_list?.finishLoadMore(0, success, isEnd)
        showList()
    }

    /**
     * 显示列表
     */
    private fun showList() {
        view?.let {
            if (messageList.isEmpty()) {
                iv_empty_message.visibility = View.VISIBLE
                tv_empty_message.visibility = View.VISIBLE
                crv_message_list.visibility = View.GONE
            } else {
                iv_empty_message.visibility = View.GONE
                tv_empty_message.visibility = View.GONE
                crv_message_list.visibility = View.VISIBLE
            }
            crv_message_list.notifyDataChanged()
        }
    }

    private fun joinFamilyResponse(msg: String?, isSuccess: Boolean) {
        activity!!.runOnUiThread {
            if (isSuccess) {
                srl_message_list.autoRefresh()
                App.data.setRefreshLevel(0)
                show(getString(R.string.join_family_success))
            } else {
                show(msg)
            }
        }
    }

    private fun bindDeviceShareResponse(msg: String?, isSuccess: Boolean) {
        activity!!.runOnUiThread {
            if (isSuccess) {
                srl_message_list.autoRefresh()
                App.data.setRefreshLevel(2)
                show(getString(R.string.bind_device_success))
            } else show(msg)
        }
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        when (clickView.tag) {
            0 -> {
                activity?.finish()
            }
            1 -> acceptInvite(position)
            2 -> deleteMessage(position)
        }
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        when (viewType) {
            0 -> {
                return MsgDeviceViewHolder(
                    LayoutInflater.from(activity)
                        .inflate(R.layout.item_message_device, parent, false)
                )
            }
            1 -> {
                return MsgFamilyViewHolder(
                    LayoutInflater.from(activity)
                        .inflate(R.layout.item_message_family, parent, false)
                )
            }
            else -> {
                return MsgNotifyViewHolder(
                    LayoutInflater.from(activity)
                        .inflate(R.layout.item_message_notify, parent, false)
                )
            }
        }
    }

    override fun getViewType(position: Int): Int {
        Log.e("XXX", "messageList " + JSON.toJSONString(messageList))
        return when (messageList[position].Attachments == null) {
            true -> {
                when (messageList[position].Category) {
                    1, 2 -> {
                        0
                    }
                    else -> 2
                }
            }
            else -> {
                when(messageList[position].MsgType) {
                    302,303 -> {
                        2
                    }
                    else -> 1
                }

            }
        }
    }
}