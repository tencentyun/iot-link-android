package com.tencent.iot.explorer.link.demo.core.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.activity.*
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.core.response.UserInfoResponse
import com.tencent.iot.explorer.link.demo.databinding.FragmentMeBinding

class MeFragment : BaseFragment<FragmentMeBinding>(), MyCallback {
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMeBinding = FragmentMeBinding.inflate(inflater, container, false)

    override fun startHere(view: View) {

        IoTAuth.userImpl.userInfo(this)

        with(binding) {
            tvUserInfo.setOnClickListener {
                jumpActivity(PersonalInfoActivity::class.java)
            }
            tvFamilyManager.setOnClickListener {
                jumpActivity(FamilyListActivity::class.java)
            }
            tvShareDevice.setOnClickListener {
                jumpActivity(ShareDeviceListActivity::class.java)
            }
            tvMessageNotify.setOnClickListener {
                jumpActivity(MessageActivity::class.java)
            }
            tvFeedback.setOnClickListener {
                jumpActivity(FeedbackActivity::class.java)
            }
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e { msg ?: "" }
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            response.parse(UserInfoResponse::class.java)?.Data?.let {
                App.data.userInfo.update(it)
            }
        } else {
            L.e { response.msg }
        }
    }

}