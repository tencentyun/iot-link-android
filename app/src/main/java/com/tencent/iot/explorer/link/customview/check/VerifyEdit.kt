package com.tencent.iot.explorer.link.customview.check

import android.content.Context
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.ImageView

class VerifyEdit : EditText {

    private var showIv: ImageView? = null
    private var clearIv: ImageView? = null

    private var showRes = 0
    private var hideRes = 0

    private var showPwd = false

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun addShowView(view: View) {
        view.setOnClickListener {
            showPwd = !showPwd
            this.transformationMethod = if (showPwd) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }
            this.setSelection(this.text.length)
        }
    }

    fun addShowImage(view: ImageView, showRes: Int, hideRes: Int) {
        showIv = view
        this.showRes = showRes
        this.hideRes = hideRes
        showIv?.setOnClickListener {
            showPwd = !showPwd
            this.transformationMethod = if (showPwd) {
                showIv?.setImageResource(showRes)
                HideReturnsTransformationMethod.getInstance()
            } else {
                showIv?.setImageResource(hideRes)
                PasswordTransformationMethod.getInstance()
            }
            this.setSelection(this.text.length)
        }
    }

    fun addClearImage(view: ImageView) {
        clearIv = view
        view.visibility = View.GONE
        clearIv?.setOnClickListener {
            this.setText("")
            clearIv?.visibility = View.GONE
        }
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (!TextUtils.isEmpty(this.text)) {
            clearIv?.let {
                if (it.visibility != View.VISIBLE) {
                    it.visibility = View.VISIBLE
                }
            }
        }
    }

}