package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.dialog.ProgressDialog
import com.tencent.iot.explorer.link.customview.dialog.UpgradeDialog
import com.tencent.iot.explorer.link.customview.dialog.UpgradeInfo
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
                resources.getString(R.string.current_version) + "\n获取失败"
            } else {
                resources.getString(R.string.current_version) + "\nV$it"
            }
        }

    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_title_privacy_policy.setOnClickListener(listener)
        tv_title_user_agreement.setOnClickListener(listener)
        tv_about_app_version.setOnClickListener(listener)
    }

    private var listener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                tv_title_user_agreement -> {
                    val intent = Intent(this@AboutUsActivity, WebActivity::class.java)
                    intent.putExtra(CommonField.EXTRA_TITLE, getString(R.string.register_agree_2))
                    var url = CommonField.POLICY_PREFIX
                    if (App.DEBUG_VERSION) url += "?uin=testReleaseID"
                    url += CommonField.SERVICE_POLICY_SUFFIX
                    intent.putExtra(CommonField.EXTRA_TEXT, url)
                    startActivity(intent)
                }

                tv_title_privacy_policy -> {
                    val intent = Intent(this@AboutUsActivity, WebActivity::class.java)
                    intent.putExtra(CommonField.EXTRA_TITLE, getString(R.string.register_agree_4))
                    var url = CommonField.POLICY_PREFIX
                    if (App.DEBUG_VERSION) url += "?uin=testReleaseID"
                    url += CommonField.PRIVACY_POLICY_SUFFIX
                    intent.putExtra(CommonField.EXTRA_TEXT, url)
                    startActivity(intent)
                }

                tv_about_app_version -> {
                    startUpdateApp()
                }
            }
        }
    }

    private fun startUpdateApp() {
        var json = "{\n" +
                "\t\"VersionInfo\": {\n" +
                "\t\t\"Platform\": \"android\",\n" +
                "\t\t\"Title\": \"This title\",\n" +
                "\t\t\"AppVersion\": \"2.0.1\",\n" +
                "\t\t\"DownloadURL\": \"\",\n" +
                "\t\t\"PackageSize\": 102499,\n" +
                "\t\t\"Content\": \"1、tsssss 不知道说些什么呢  怎么办啊测试特别长的时候是什么样子的呼啦啦啦  就是这么任性呢\\n2、不知道如何是好呢\\n3、单元可以变得越来越好呢\\n4、人生总是在向前看的，不知道现在往哪里看\",\n" +
                "\t\t\"UpgradeType\": 1,\n" +
                "\t\t\"Channel\": 0,\n" +
                "\t\t\"ReleaseTime\": 1597030920\n" +
                "\t}\n" +
                "}"

        var info = UpgradeInfo.convertJson2UpgradeInfo(json)
        var dialog = UpgradeDialog(this, info)
        dialog.setOnDismisListener(upgradeDialogListener)
        dialog.show()
    }

    private var upgradeDialogListener = object: UpgradeDialog.OnDismisListener {
        override fun OnClickUpgrade(url: String?) {
            var dialog = ProgressDialog(this@AboutUsActivity, "")
            dialog.setOnDismisListener(downloadListener)
            dialog.show()
        }
    }

    private var downloadListener = object: ProgressDialog.OnDismisListener {
        override fun onDownloadSuccess() {
            TODO("Not yet implemented")
        }

        override fun onDownloadFailed() {
            TODO("Not yet implemented")
        }

        override fun onDownloadProgress(currentProgress: Int, size: Int) {
            Log.e("XXX", "currentProgress=" + currentProgress + ", size=" + size)
        }

    }
}
