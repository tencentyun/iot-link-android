package com.tencent.iot.explorer.link.demo.core.activity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.consts.RequestCode
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.ShareUserResponse
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.adapter.OnItemListener
import com.tencent.iot.explorer.link.demo.core.adapter.ShareUserAdapter
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.core.link.entity.ShareUserEntity
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityShareUserListBinding

/**
 * 设备分享：用户列表
 */
class ShareUserListActivity : BaseActivity<ActivityShareUserListBinding>(), MyCallback {

    private lateinit var adapter: ShareUserAdapter
    private var device: DeviceEntity? = null

    private val userList = arrayListOf<ShareUserEntity>()

    override fun getViewBinding(): ActivityShareUserListBinding = ActivityShareUserListBinding.inflate(layoutInflater)

    override fun initView() {
        with(binding) {
            menuShareUserList.tvTitle.text = getString(R.string.device_share)
            device = get("device")
            adapter = ShareUserAdapter(this@ShareUserListActivity, userList)
            rvShareUserList.layoutManager = LinearLayoutManager(this@ShareUserListActivity)
            rvShareUserList.adapter = adapter
            getShareUserList()
        }
    }

    override fun setListener() {
        binding.menuShareUserList.ivBack.setOnClickListener { finish() }
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*, *>, clickView: View, position: Int) {
                deleteShareUser(position)
            }
        })
        binding.tvAddDeviceShare.setOnClickListener {
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
        L.e { msg ?: "" }
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
            with(binding) {
                if (userList.size > 0) {
                    rvShareUserList.visibility = View.VISIBLE
                    tvNoDeviceShare.visibility = View.GONE
                } else {
                    rvShareUserList.visibility = View.GONE
                    tvNoDeviceShare.visibility = View.VISIBLE
                }
            }
            adapter.notifyDataSetChanged()
        }
    }
}
