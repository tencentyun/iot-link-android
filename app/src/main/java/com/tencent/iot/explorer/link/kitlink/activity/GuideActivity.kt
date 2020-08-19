package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.google.firebase.analytics.FirebaseAnalytics
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.util.SharePreferenceUtil
import kotlinx.android.synthetic.main.activity_guide.*

class GuideActivity  : PActivity(), View.OnClickListener{

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    private val permissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )

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
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
    }

    override fun setListener() {
        btn_create_new_account.setOnClickListener(this)
        tv_use_existed_account_to_login.setOnClickListener(this)
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
        }
    }
}