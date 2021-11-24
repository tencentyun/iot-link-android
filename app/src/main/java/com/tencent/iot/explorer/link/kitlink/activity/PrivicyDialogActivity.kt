package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.TextUtils
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.customview.dialog.UserAgreeDialog
import com.tencent.iot.explorer.link.kitlink.consts.CommonField


class PrivicyDialogActivity : BaseActivity() {

    private var dialog: UserAgreeDialog? = null
    private val ANDROID_ID = App.uuid

    override fun getContentView(): Int {
        return R.layout.activity_privicy_dialog
    }

    override fun initView() {
        if (!TextUtils.isEmpty(App.data.getToken())) {
            startActivity(Intent(this, MainActivity::class.java))
            return
        }
        // 21.11.20 产品沟通这里展示默认的中国对应的协议。
        dialog = UserAgreeDialog(this@PrivicyDialogActivity)
        dialog!!.show()
        dialog!!.setOnDismisListener(object : UserAgreeDialog.OnDismisListener {
            override fun onDismised() {
                onBackPressed()
            }
            override fun onOkClicked() {
                Utils.setXmlStringValue(this@PrivicyDialogActivity, CommonField.AGREED_RULE_FLAG, CommonField.AGREED_RULE_FLAG, "1")
                finish()
                App.toLogin()
            }

            override fun onOkClickedUserAgreement() {
                if (Utils.getLang().contains(CommonField.ZH_TAG)) {
                    val intent = Intent(this@PrivicyDialogActivity, WebActivity::class.java)
                    intent.putExtra(CommonField.EXTRA_TITLE, getString(R.string.register_agree_2))
                    var url = CommonField.POLICY_PREFIX
                    url += "?uin=$ANDROID_ID"
                    url += CommonField.SERVICE_POLICY_SUFFIX
                    intent.putExtra(CommonField.EXTRA_TEXT, url)
                    startActivity(intent)
                } else {
                    OpensourceLicenseActivity.startWebWithExtra(this@PrivicyDialogActivity, getString(R.string.register_agree_2), CommonField.SERVICE_AGREEMENT_URL_CN_EN)
                }
            }

            override fun onOkClickedPrivacyPolicy() {
                if (Utils.getLang().contains(CommonField.ZH_TAG)) {
                    val intent = Intent(this@PrivicyDialogActivity, WebActivity::class.java)
                    intent.putExtra(CommonField.EXTRA_TITLE, getString(R.string.register_agree_4))
                    var url = CommonField.POLICY_PREFIX
                    url += "?uin=$ANDROID_ID"
                    url += CommonField.PRIVACY_POLICY_SUFFIX
                    intent.putExtra(CommonField.EXTRA_TEXT, url)
                    startActivity(intent)
                } else {
                    OpensourceLicenseActivity.startWebWithExtra(this@PrivicyDialogActivity, getString(R.string.register_agree_4), CommonField.PRIVACY_POLICY_URL_CN_EN)
                }
            }

            override fun onOkClickedThirdSDKList() {
                var url = ""
                if (Utils.getLang().contains(CommonField.ZH_TAG)) {
                    url = CommonField.THIRD_SDK_URL_US_ZH
                } else {
                    url = CommonField.THIRD_SDK_URL_US_EN
                }
                OpensourceLicenseActivity.startWebWithExtra(this@PrivicyDialogActivity, getString(R.string.rule_content_list), url)
            }
        })
    }

    override fun setListener() {

    }

    override fun onDestroy() {
        dialog?.dismiss()
        dialog = null
        super.onDestroy()
    }
}