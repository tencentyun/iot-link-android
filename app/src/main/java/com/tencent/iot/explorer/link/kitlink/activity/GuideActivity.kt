package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.content.Intent
import android.os.SystemClock
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.CommonUtils
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.util.AppInfoUtils
import com.tencent.iot.explorer.link.util.SharePreferenceUtil
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_guide.*


class GuideActivity  : PActivity(), View.OnClickListener{

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    private val permissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )

    private val counts = 5 //点击次数
    private val duration = 3 * 1000.toLong() //规定有效时间
    private val hits = LongArray(counts)

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.activity_guide
    }

    override fun initView() {
        if (!checkPermissions(permissions)) {
            requestPermission(permissions)
        } else {
            permissionAllGranted()
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this@GuideActivity)
        if (!TextUtils.isEmpty(App.data.getToken())) {
            val userId = SharePreferenceUtil.getString(this@GuideActivity, App.CONFIG, CommonField.USER_ID)
            mFirebaseAnalytics!!.setUserId(userId)
            mFirebaseAnalytics!!.setUserProperty(CommonField.FIREBASE_USER_ID, userId)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
    }

    override fun setListener() {
        btn_create_new_account.setOnClickListener(this)
        tv_use_existed_account_to_login.setOnClickListener(this)
        tv_empty_area.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            btn_create_new_account -> {
                Intent(this, RegisterActivity::class.java).run {
                    startActivity(this)
                }
            }
            tv_use_existed_account_to_login -> {
                Intent(this, LoginActivity::class.java).run {
                    startActivity(this)
                }
            }
            tv_empty_area -> {// 连续点击五次复制AndroidID
                System.arraycopy(hits, 1, hits, 0, hits.size - 1)
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于duration，即连续5次点击
                hits[hits.size - 1] = SystemClock.uptimeMillis()
                if (hits[0] >= SystemClock.uptimeMillis() - duration) {
                    if (hits.size == 5) {
                        // 获取AndroidID，并保存至剪切板
                        AppInfoUtils.copy(this, CommonUtils.getAndroidID())
                    }
                }
            }
        }
    }
}