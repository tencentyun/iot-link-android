package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.content.Intent
import android.os.SystemClock
import android.text.TextUtils
import android.view.View
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.utils.FileUtils
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.customview.dialog.ProgressDialog
import com.tencent.iot.explorer.link.customview.dialog.UpgradeDialog
import com.tencent.iot.explorer.link.customview.dialog.UpgradeInfo
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.activity_guide.*


class GuideActivity  : PActivity(), View.OnClickListener{

    private val permissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )

    private val counts = 5 //点击次数
    private val duration = 3 * 1000.toLong() //规定有效时间
    private val hits = LongArray(counts)
    private var isForceUpgrade = true

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
        if (!TextUtils.isEmpty(App.data.getToken())) {
            startActivity(Intent(this, MainActivity::class.java))
            return
        }

        var appName = getString(R.string.app_name)
        textView.text = getString(R.string.welcome_to_use_tencent_ll, appName)
    }

    override fun onResume() {
        super.onResume()
        if (isForceUpgrade) {
            startUpdateApp()
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
                        Utils.copy(this, Utils.getAndroidID(T.getContext()))
                    }
                }
            }
        }
    }

    private fun startUpdateApp() {
        HttpRequest.instance.getLastVersion(object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                T.show(msg)
            }
            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    val json = response.data as JSONObject
                    val info = UpgradeInfo.convertJson2UpgradeInfo(json)
                    if (App.needUpgrade(info!!.version) && info.upgradeType != 2 && !UpgradeDialog.dialogShowing()) {
                        isForceUpgrade = info.upgradeType == 1 // 2:静默更新不提示 1:强制升级 0:用户确认
                        if (isForceUpgrade || (!isForceUpgrade && !UpgradeDialog.dialogShowed())) {
                            val dialog = UpgradeDialog(this@GuideActivity, info)
                            dialog.setOnDismisListener(upgradeDialogListener)
                            dialog.show()
                        }
                    }
                }
            }
        })
    }

    private var upgradeDialogListener =
        UpgradeDialog.OnDismisListener { url ->
            val dialog = ProgressDialog(this@GuideActivity, url)
            dialog.setOnDismisListener(downloadListener)
            dialog.show()
        }

    private var downloadListener = object: ProgressDialog.OnDismisListener {
        override fun onDownloadSuccess(path: String) {
            FileUtils.installApk(this@GuideActivity, path)
        }
        override fun onDownloadFailed() {
            T.show(resources.getString(R.string.download_failed))
        }
        override fun onDownloadProgress(currentProgress: Int, size: Int) { }
    }
}