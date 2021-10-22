package com.tencent.iot.explorer.link.customview.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.tencent.iot.explorer.link.R

class UserAgreeDialog(context: Context) : IosCenterStyleDialog(context, R.layout.popup_user_agree_layout), View.OnClickListener, DialogInterface.OnKeyListener  {
    private var tip_title: TextView? = null
    private var tv_tip_content: TextView? = null
    private var tv_register_tip: TextView? = null
    private var tv_confirm: TextView? = null
    private var tv_cancel: TextView? = null
    private var select_tag_layout: RelativeLayout? = null
    private var iv_agreement: ImageView? = null
    private var iv_agreement_status: ImageView? = null
    @Volatile
    private var readed = false

    private fun freshReadState() {
        if (readed) {
            iv_agreement?.setImageResource(R.mipmap.dev_mode_sel)
            iv_agreement_status?.visibility = View.VISIBLE
        } else {
            iv_agreement?.setImageResource(R.mipmap.dev_mode_unsel)
            iv_agreement_status?.visibility = View.GONE
        }
    }

    override fun initView() {
        tip_title = view.findViewById(R.id.tip_title)
        tv_tip_content = view.findViewById(R.id.tv_tip_content)
        tv_register_tip = view.findViewById(R.id.tv_register_tip)
        tv_confirm = view.findViewById(R.id.tv_confirm)
        tv_cancel = view.findViewById(R.id.tv_cancel)
        select_tag_layout = view.findViewById(R.id.select_tag_layout)
        iv_agreement = view.findViewById(R.id.iv_agreement)
        iv_agreement_status = view.findViewById(R.id.iv_agreement_status)

        val partStr0 = context.getString(R.string.register_agree_1)
        val partStr1 = context.getString(R.string.register_agree_2)
        val partStr2 = context.getString(R.string.register_agree_3)
        val partStr3 = context.getString(R.string.register_agree_4)
        var showStr = partStr1 + partStr2 + partStr3
        tip_title?.text = showStr

        var agreeStr = partStr0 + showStr
        var spannable = SpannableStringBuilder(agreeStr)
        spannable.setSpan(IndexClickableSpan(context, 1),
            partStr0.length, partStr0.length + partStr1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(IndexClickableSpan(context, 2),
            agreeStr.length - partStr1.length, agreeStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tv_register_tip?.movementMethod = LinkMovementMethod.getInstance()
        tv_register_tip?.text = spannable

        val agreeContentStrPrefix = context.getString(R.string.rule_content_prefix)
        val agreeContentStrSuffix = context.getString(R.string.rule_content_suffix)
        var agreeContentStr = agreeContentStrPrefix + showStr + agreeContentStrSuffix
        var agreeContentSpannable = SpannableStringBuilder(agreeContentStr)
        agreeContentSpannable.setSpan(IndexClickableSpan(context, 1),
            agreeContentStrPrefix.length, agreeContentStrPrefix.length + partStr1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val start = agreeContentStrPrefix.length + partStr1.length + partStr2.length
        agreeContentSpannable.setSpan(IndexClickableSpan(context, 2),
            start, start + partStr3.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tv_tip_content?.movementMethod = LinkMovementMethod.getInstance()
        tv_tip_content?.text = agreeContentSpannable

        select_tag_layout?.setOnClickListener(this)
        tv_cancel?.setOnClickListener(this)
        tv_confirm?.setOnClickListener(this)
        this.setOnKeyListener(this)
        freshReadState()
    }

    override fun onClick(v: View?) {
        when(v) {
            select_tag_layout -> {
                readed =! readed
                freshReadState()
            }

            tv_cancel -> {
                onDismisListener?.onDismised()
            }

            tv_confirm -> {
                if (!readed) return
                onDismisListener?.onOkClicked()
                dismiss()
            }
        }
    }

    @Volatile
    private var onDismisListener: OnDismisListener? = null

    interface OnDismisListener {
        fun onDismised()
        fun onOkClicked()
        fun onOkClickedUserAgreement()
        fun onOkClickedPrivacyPolicy()
    }

    fun setOnDismisListener(onDismisListener: OnDismisListener?) {
        this.onDismisListener = onDismisListener
    }

    inner class IndexClickableSpan(context: Context, index: Int): ClickableSpan() {
        private var index = 0
        private var context: Context? = null

        init {
            this.index = index
            this.context = context
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onClick(widget: View) {
            context?.let {
                tv_tip_content?.highlightColor = it.getColor(android.R.color.transparent)
                tv_register_tip?.highlightColor = it.getColor(android.R.color.transparent)
            }
            if (index == 1) {
                onDismisListener?.onOkClickedUserAgreement()
            } else if (index == 2) {
                onDismisListener?.onOkClickedPrivacyPolicy()
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            context?.let {
                ds.color = it.getColor(R.color.blue_0066FF)
            }
            ds.isUnderlineText = false
        }
    }

    override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onDismisListener?.onDismised()
        }
        return true
    }
}