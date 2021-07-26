package com.tencent.iot.explorer.link.kitlink.fragment

import android.os.SystemClock
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

    private val counts = 5 //点击次数
    private val duration = 3 * 1000.toLong() //规定有效时间
    private val hits = LongArray(counts)

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
        tv_for_open_log.setOnClickListener(this)
        tv_for_close_log.setOnClickListener(this)
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
                jumpActivity(HelpWebViewActivity::class.java)
            }
            tv_me_feedback -> {
                jumpActivity(FeedbackActivity::class.java)
            }
            tv_me_about -> {
                jumpActivity(AboutUsActivity::class.java)
            }
            tv_for_open_log, tv_for_close_log -> {// 连续点击五次控制日志打印
                System.arraycopy(hits, 1, hits, 0, hits.size - 1)
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于duration，即连续5次点击
                hits[hits.size - 1] = SystemClock.uptimeMillis()
                if (hits[0] >= SystemClock.uptimeMillis() - duration) {
                    if (hits.size == 5) {
                        if (v != null) {
                            if (v.id == tv_for_open_log.id) {
                                L.isLog = true
                                T.show("日志打印已开启")
                            } else {
                                L.isLog = false
                                T.show("日志打印已关闭")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showUserInfo() {
        tv_me_name.text = App.data.userInfo.NickName
        tv_me_phone.text = App.data.userInfo.PhoneNumber
        ImageManager.setImagePath(this.context, me_portrait, App.data.userInfo.Avatar, 0)
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