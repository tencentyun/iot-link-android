package com.tenext.demo.fragment

import android.view.View
import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.response.BaseResponse
import com.tenext.demo.App
import com.tenext.demo.R
import com.tenext.demo.activity.*
import com.tenext.demo.log.L
import com.tenext.demo.response.UserInfoResponse
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