package com.tencent.iot.explorer.link.demo.core.fragment

import android.view.View
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.demo.core.activity.*
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.core.response.UserInfoResponse
import kotlinx.android.synthetic.main.fragment_me.*

class MeFragment : BaseFragment(), MyCallback {
    override fun getContentView(): Int {
        return R.layout.fragment_me
    }

    override fun startHere(view: View) {

        IoTAuth.userImpl.userInfo(this)

        tv_user_info.setOnClickListener {
            jumpActivity(PersonalInfoActivity::class.java)
        }
        tv_family_manager.setOnClickListener {
            jumpActivity(FamilyListActivity::class.java)
        }
        tv_share_device.setOnClickListener {
            jumpActivity(ShareDeviceListActivity::class.java)
        }
        tv_message_notify.setOnClickListener {
            jumpActivity(MessageActivity::class.java)
        }
        tv_feedback.setOnClickListener {
            jumpActivity(FeedbackActivity::class.java)
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            response.parse(UserInfoResponse::class.java)?.Data?.let {
                App.data.userInfo.update(it)
            }
        } else {
            L.e(response.msg)
        }
    }

}