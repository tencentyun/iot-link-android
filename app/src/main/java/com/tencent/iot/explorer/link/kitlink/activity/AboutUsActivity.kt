package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.TextUtils
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.activity.BaseActivity
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.util.AppInfoUtils
import kotlinx.android.synthetic.main.activity_about_us.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 关于我们
 */
class AboutUsActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_about_us
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.about_me)
        AppInfoUtils.getVersionName(this).let {
            tv_about_app_version.text = if (TextUtils.isEmpty(it)) {
                "获取失败"
            } else {
                "V$it"
            }
        }

    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_title_privacy_policy.setOnClickListener {
            val intent = Intent(this, WebActivity::class.java)
            intent.putExtra(CommonField.EXTRA_TITLE, getString(R.string.register_agree_4))
            var url = CommonField.POLICY_PREFIX
            if (App.DEBUG_VERSION) url += "?uin=testReleaseID"
            url += CommonField.PRIVACY_POLICY_SUFFIX
            intent.putExtra(CommonField.EXTRA_TEXT, url)
            startActivity(intent)
        }

        tv_title_user_agreement.setOnClickListener {
            val intent = Intent(this, WebActivity::class.java)
            intent.putExtra(CommonField.EXTRA_TITLE, getString(R.string.register_agree_2))
            var url = CommonField.POLICY_PREFIX
            if (App.DEBUG_VERSION) url += "?uin=testReleaseID"
            url += CommonField.SERVICE_POLICY_SUFFIX
            intent.putExtra(CommonField.EXTRA_TEXT, url)
            startActivity(intent)
        }
    }
}
