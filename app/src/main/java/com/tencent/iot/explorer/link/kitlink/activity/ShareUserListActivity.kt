package com.tencent.iot.explorer.link.kitlink.activity

import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.entity.DeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.ShareUserEntity
import com.tencent.iot.explorer.link.kitlink.holder.ShareUserFootHolder
import com.tencent.iot.explorer.link.kitlink.holder.ShareUserHeadHolder
import com.tencent.iot.explorer.link.kitlink.holder.ShareUserHolder
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.response.ShareUserResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.activity_share_user_list.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 分享设备-用户列表
 */
class ShareUserListActivity : BaseActivity(), MyCallback, CRecyclerView.RecyclerItemView {

    private var deviceEntity: DeviceEntity? = null

    private val userList = arrayListOf<ShareUserEntity>()
    private var total = 0

    private var headHolder: ShareUserHeadHolder? = null
    private lateinit var footHolder: ShareUserFootHolder

    override fun getContentView(): Int {
        return R.layout.activity_share_user_list
    }

    override fun onResume() {
        super.onResume()
        getShareUserList()
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        deviceEntity = get("device")
        tv_title.text = getString(R.string.device_share)
        crv_share_user_list.setList(userList)
        crv_share_user_list.addRecyclerItemView(this)
        headHolder = ShareUserHeadHolder(this, crv_share_user_list, R.layout.head_share_user_list)
        crv_share_user_list.addHeader(headHolder!!)
        footHolder = ShareUserFootHolder(this, crv_share_user_list, R.layout.foot_share_user_list)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_add_device_share.setOnClickListener {
            jumpToShare()
        }
        footHolder.footListener = object : CRecyclerView.FootListener {
            override fun doAction(
                holder: CRecyclerView.FootViewHolder<*>,
                clickView: View,
                position: Int
            ) {
                jumpToShare()
            }
        }
    }

    /**
     * 跳转到分享界面
     */
    private fun jumpToShare() {
        jumpActivity(ShareActivity::class.java)
    }

    /**
     * 获取分享设备的用户列表
     */
    private fun getShareUserList() {
        userList.clear()
        deviceEntity?.run {
            HttpRequest.instance.shareUserList(
                ProductId,
                DeviceName,
                userList.size,
                this@ShareUserListActivity
            )
        }
    }

    /**
     * 删除分享用户
     */
    private fun deleteShareUser(position: Int) {
        deviceEntity?.run {
            userList[position].let {
                HttpRequest.instance.deleteShareUser(
                    ProductId,
                    DeviceName,
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
                        total = Total
                        Users?.run {
                            userList.addAll(this)
                            showList()
                        }
                    }
                }
                RequestCode.delete_share_user -> {
                    getShareUserList()
                }
            }
        }
    }

    /**
     * 显示
     */
    private fun showList() {
        if (userList.size > 0) {
            crv_share_user_list.visibility = View.VISIBLE
            rl_no_device_share.visibility = View.GONE
            crv_share_user_list.addFooter(footHolder)
        } else {
            crv_share_user_list.visibility = View.GONE
            rl_no_device_share.visibility = View.VISIBLE
        }
        crv_share_user_list.notifyDataChanged()
    }


    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        deleteShareUser(position)
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return ShareUserHolder(this, parent, R.layout.item_share_user)
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

}
