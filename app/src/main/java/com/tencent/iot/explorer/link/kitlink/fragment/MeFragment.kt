package com.tencent.iot.explorer.link.kitlink.fragment

import android.view.View
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.activity.*
import com.tencent.iot.explorer.link.kitlink.response.UserInfoResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.picture.imp.ImageManager
import kotlinx.android.synthetic.main.fragment_me.*

/**
 *  我的界面
 */
class MeFragment : BaseFragment(), View.OnClickListener, MyCallback {


    override fun getContentView(): Int {
        return R.layout.fragment_me
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun onResume() {
        super.onResume()
        showUserInfo()
    }

    override fun startHere(view: View) {
        HttpRequest.instance.userInfo(this)
        setListener()
    }

    private fun setListener() {
        info_bg.setOnClickListener(this)
        tv_me_family.setOnClickListener(this)
        tv_me_message.setOnClickListener(this)
        tv_me_help.setOnClickListener(this)
        tv_me_feedback.setOnClickListener(this)
        tv_me_about.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            info_bg -> {
                jumpActivity(UserInfoActivity::class.java)
            }
            tv_me_family -> {
                jumpActivity(FamilyListActivity::class.java)
            }
            tv_me_message -> {
                jumpActivity(MessageActivity::class.java)
            }
            tv_me_help -> {
//                jumpActivity(HelpCenterActivity::class.java)
                jumpActivity(HelpWebViewActivity::class.java)
            }
            tv_me_feedback -> {
                jumpActivity(FeedbackActivity::class.java)
            }
            tv_me_about -> {
                jumpActivity(AboutUsActivity::class.java)
            }
        }
    }

    private fun showUserInfo() {
        tv_me_name.text = App.data.userInfo.NickName
        tv_me_phone.text = App.data.userInfo.PhoneNumber
        ImageManager.setImagePath(
            this.context,
            me_portrait,
            App.data.userInfo.Avatar,
            0
        )
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            response.parse(UserInfoResponse::class.java)?.Data?.run {
                App.data.userInfo = this
                showUserInfo()
            }
        } else {
            T.show(response.msg)
        }
    }


}