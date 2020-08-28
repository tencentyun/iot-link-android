package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.BuildConfig
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.dialog.ProgressDialog
import com.tencent.iot.explorer.link.customview.dialog.UpgradeDialog
import com.tencent.iot.explorer.link.customview.dialog.UpgradeInfo
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.FileUtils
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_about_us.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 关于我们
 */
class AboutUsActivity : BaseActivity() {

    val INSTALL_PERMISS_CODE = 1

    override fun getContentView(): Int {
        return R.layout.activity_about_us
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.about_me)
        tv_about_app_version.text = resources.getString(R.string.current_version) + BuildConfig.VERSION_NAME
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_title_privacy_policy.setOnClickListener(listener)
        tv_title_user_agreement.setOnClickListener(listener)
        tv_about_app_version.setOnClickListener(listener)
    }

    private var listener = object : View.OnClickListener {
        @RequiresApi(Build.VERSION_CODES.O)
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

                    // 高于 8.0 版本的，尝试获取权限
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        var haveInstallPermission = packageManager.canRequestPackageInstalls()
                        if (!haveInstallPermission) {
                            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                            startActivityForResult(intent, INSTALL_PERMISS_CODE)
                        } else {
                            startUpdateApp()
                        }
                    } else {  // 低于 8.0 的版本直接下载安装
                        startUpdateApp()
                    }
                }
            }
        }
    }

    private fun startUpdateApp() {

        HttpRequest.instance.getLastVersion(object: MyCallback{
            override fun fail(msg: String?, reqCode: Int) {
                T.show(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response != null && response.isSuccess() && response.data != null) {
                    var json = response.data as JSONObject
                    var info = UpgradeInfo.convertJson2UpgradeInfo(json)
                    if (App.needUpgrade(info!!.version)) {
                        var dialog = UpgradeDialog(this@AboutUsActivity, info)
                        dialog.setOnDismisListener(upgradeDialogListener)
                        dialog.show()
                    } else {
                        T.show(resources.getString(R.string.no_need_upgrade))
                    }
                }
            }
        })
    }

    private var upgradeDialogListener = object: UpgradeDialog.OnDismisListener {
        override fun OnClickUpgrade(url: String?) {
            var dialog = ProgressDialog(this@AboutUsActivity, url)
            dialog.setOnDismisListener(downloadListener)
            dialog.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 获取安装未知源 app 的权限，调用下载接口
        if (requestCode == INSTALL_PERMISS_CODE && resultCode == Activity.RESULT_OK) {
            startUpdateApp()
        }
    }

    private var downloadListener = object: ProgressDialog.OnDismisListener {
        override fun onDownloadSuccess(path: String) {
            FileUtils.installApk(this@AboutUsActivity, path)
        }

        override fun onDownloadFailed() {
            T.show(resources.getString(R.string.download_failed))
        }

        override fun onDownloadProgress(currentProgress: Int, size: Int) {

        }

    }
}
