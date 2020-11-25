package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
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
import com.tencent.iot.explorer.link.core.utils.FileUtils
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.utils.Utils
import kotlinx.android.synthetic.main.activity_about_us.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 关于我们
 */
class AboutUsActivity : BaseActivity() {

    private val ANDROID_ID = Utils.getAndroidID(T.getContext())

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
                    url += "?uin=$ANDROID_ID"
                    url += CommonField.SERVICE_POLICY_SUFFIX
                    intent.putExtra(CommonField.EXTRA_TEXT, url)
                    startActivity(intent)
                }

                tv_title_privacy_policy -> {
                    val intent = Intent(this@AboutUsActivity, WebActivity::class.java)
                    intent.putExtra(CommonField.EXTRA_TITLE, getString(R.string.register_agree_4))
                    var url = CommonField.POLICY_PREFIX
                    url += "?uin=$ANDROID_ID"
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

        HttpRequest.instance.getLastVersion(object: MyCallback{
            override fun fail(msg: String?, reqCode: Int) {
                T.show(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response == null) {     // 无响应数据
                    T.show(resources.getString(R.string.unknown_error))

                } else if (response != null && response.isSuccess() && response.data != null) { // 返回响应数据实体
                    var json = response.data as JSONObject
                    var info = UpgradeInfo.convertJson2UpgradeInfo(json)
                    if (App.needUpgrade(info!!.version)) {
                        var dialog = UpgradeDialog(this@AboutUsActivity, info)
                        dialog.setOnDismisListener(upgradeDialogListener)
                        dialog.show()
                    } else {
                        T.show(resources.getString(R.string.no_need_upgrade))
                    }

                } else if (response.isSuccess() && response.data == null) { // 返回的数据实体无实际内容，不需要更新
                    T.show(resources.getString(R.string.no_need_upgrade))

                } else if (!response.isSuccess()) {  // 请求失败，提示失败原因
                    T.show(response.msg)
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
