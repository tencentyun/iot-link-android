package com.tencent.iot.explorer.link.core.demo.activity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.consts.RequestCode
import com.tencent.iot.explorer.link.core.auth.entity.Device
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.adapter.OnItemListener
import com.tencent.iot.explorer.link.core.demo.adapter.ShareUserAdapter
import com.tencent.iot.explorer.link.core.demo.entity.ShareUser
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder
import com.tencent.iot.explorer.link.core.demo.log.L
import com.tencent.iot.explorer.link.core.demo.response.ShareUserResponse
import kotlinx.android.synthetic.main.activity_share_user_list.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 设备分享：用户列表
 */
class ShareUserListActivity : BaseActivity(), MyCallback {

    private lateinit var adapter: ShareUserAdapter
    private var device: Device? = null

    private val userList = arrayListOf<ShareUser>()

    override fun getContentView(): Int {
        return R.layout.activity_share_user_list
    }

    override fun initView() {
        tv_title.text = getString(R.string.device_share)
        device = get("device")

        adapter = ShareUserAdapter(this, userList)
        rv_share_user_list.layoutManager = LinearLayoutManager(this)
        rv_share_user_list.adapter = adapter

        getShareUserList()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                deleteShareUser(position)
            }
        })
        tv_add_device_share.setOnClickListener {
            jumpActivity(ShareDeviceActivity::class.java)
        }
    }

    /**
     * 获取分享设备的用户列表
     */
    private fun getShareUserList() {
        userList.clear()
        device?.run {
            IoTAuth.shareImpl.shareUserList(
                ProductId,
                getAlias(),
                userList.size,
                this@ShareUserListActivity
            )
        }
    }

    /**
     * 删除分享用户
     */
    private fun deleteShareUser(position: Int) {
        device?.run {
            userList[position].let {
                IoTAuth.shareImpl.deleteShareUser(
                    ProductId,
                    getAlias(),
                    it.UserID,
                    this@ShareUserListActivity
                )
            }
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.share_user_list -> {
                    response.parse(ShareUserResponse::class.java)?.run {
                        Users?.run {
                            userList.addAll(this)
                            showList()
                        }
                    }
                }
                RequestCode.delete_share_user -> {
                    if (response.isSuccess()) {
                        getShareUserList()
                    } else {
                        show(response.msg)
                    }
                }
            }
        }
    }

    /**
     * 显示
     */
    private fun showList() {
        runOnUiThread {
            if (userList.size > 0) {
                rv_share_user_list.visibility = View.VISIBLE
                tv_no_device_share.visibility = View.GONE
            } else {
                rv_share_user_list.visibility = View.GONE
                tv_no_device_share.visibility = View.VISIBLE
            }
            adapter.notifyDataSetChanged()
        }
    }
}
