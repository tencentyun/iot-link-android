package com.tencent.iot.explorer.link.kitlink.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.DateUtils
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.LogoutPresenter
import com.tencent.iot.explorer.link.mvp.view.LogoutView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.utils.Utils
import kotlinx.android.synthetic.main.activity_logout.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.util.*

class LogoutActivity  : PActivity(), LogoutView, View.OnClickListener{

    private lateinit var presenter: LogoutPresenter

    private val ANDROID_ID = Utils.getAndroidID(T.getContext())

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_logout
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.logout_account)
        btn_logout.setBackgroundResource(R.drawable.background_grey_dark_cell)
        tv_logout_time.text =
            DateUtils.getFormatDateWithoutTime(DateUtils.getDateAfter(Date(), 7+1)) + " 00:00:00" // 7天后的24点
        presenter = LogoutPresenter(this)
        formatTip()
    }

    private fun formatTip() {
        val str = resources.getString(R.string.already_known)
        val partStr1 = resources.getString(R.string.tencentll_account_logout_agreement)
        var showStr = str + " " + partStr1
        val spannable = SpannableStringBuilder(showStr)

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LogoutActivity, WebActivity::class.java)
                intent.putExtra(CommonField.EXTRA_TITLE, getString(R.string.tencentll_account_logout_agreement2))
                var url = CommonField.POLICY_PREFIX
                url += "?uin=$ANDROID_ID"
                url += CommonField.CANCEL_POLICY_SUFFIX
                intent.putExtra(CommonField.EXTRA_TEXT, url)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = resources.getColor(R.color.blue_0066FF)
                ds.setUnderlineText(false);
            }

        },
            showStr.length - partStr1.length, showStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        clear_account_tv.setMovementMethod(LinkMovementMethod.getInstance())
        clear_account_tv.setText(spannable)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        btn_logout.setOnClickListener(this)
        iv_logout_agreement.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            btn_logout -> {// 注销
                if (presenter.isAgreement()) {
                    showConfirmCancelDialog()
                }
            }
            iv_logout_agreement -> {
                presenter.agreement()
            }
        }
    }

    override fun agreement(isAgree: Boolean) {
        iv_logout_agreement.setImageResource(
            if (isAgree) {
                R.mipmap.icon_selected
            } else {
                R.mipmap.dev_mode_unsel
            }
        )
        if (isAgree) {
            btn_logout.setBackgroundResource(R.drawable.background_circle_red_gradient)
        } else {
            btn_logout.setBackgroundResource(R.drawable.background_grey_dark_cell)
        }
    }

    override fun unselectedAgreement() {
        T.show(getString(R.string.tencentll_account_logout_agreement3))
    }

    override fun cancelAccountSuccess() {
        showCancelReqSuccessDialog()
    }

    override fun cancelAccountFail(msg: String) {
        T.show(msg)
    }

    private fun showConfirmCancelDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.confirm_to_cancel_account_title)
            .setMessage(R.string.confirm_to_cancel_account_content)
            .setCancelable(false)
            .setPositiveButton(R.string.label_ok,
                DialogInterface.OnClickListener { dialog, id ->
                    presenter.cancelAccount()
                })
            .setNegativeButton(R.string.label_cancel,
                DialogInterface.OnClickListener { dialog, id ->
                    finish()
                })
        builder.create()
        builder.show()
    }

    private fun showCancelReqSuccessDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.cancel_account_request_success_title)
            .setMessage(R.string.cancel_account_request_success_content)
            .setCancelable(false)
            .setPositiveButton(R.string.have_known,
                DialogInterface.OnClickListener { dialog, id ->
                    App.toLogin()
                    App.data.activityList.forEach {
                        if (it !is GuideActivity) {
                            it.finish()
                        }
                    }
                    App.data.activityList.clear()
                })
        builder.create()
        builder.show()
    }
}