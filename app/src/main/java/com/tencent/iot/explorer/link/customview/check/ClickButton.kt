package com.tencent.iot.explorer.link.customview.check

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.isDigitsOnly
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.mvp.presenter.ForgotPasswordPresenter
import com.tencent.iot.explorer.link.mvp.presenter.RegisterPresenter

class ClickButton : AppCompatTextView {

    private val list = arrayListOf<EditTextHolder>()

    private var registerPresenter: RegisterPresenter? = null
    private var forgotPasswordPresenter: ForgotPasswordPresenter? = null
    var enableBackgroundColor = this.resources.getColor(R.color.bule_0066ff)
    var disableBackgroundColor = this.resources.getColor(R.color.gray_A1A7B2)
    var btn2Click: MutableList<TextView> = ArrayList()

    constructor(context: Context?) : super(context!!) {
        this.isEnabled = false
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        this.isEnabled = false
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!,
        attrs,
        defStyleAttr
    ) {
        this.isEnabled = false
    }

    fun setRegisterPresenter(registerPresenter: RegisterPresenter) {
        this.registerPresenter = registerPresenter
    }

    fun setForgotPasswordPresenter(forgotPasswordPresenter: ForgotPasswordPresenter) {
        this.forgotPasswordPresenter = forgotPasswordPresenter
    }

    fun addEditText(editText: EditText, type: String) {
        if (!isContain(editText)) {
            list.add(EditTextHolder(editText, null, type))
            checkStatus()
        } else changeType(editText, type)
    }

    fun addEditText(editText: EditText, tv: TextView, type: String) {
        if (!isContain(editText)) {
            list.add(EditTextHolder(editText, tv, type))
            checkStatus()
        } else changeType(editText, type)
    }

    fun addEditText(editText: EditText, tv: TextView?) {
        if (!isContain(editText)) {
            list.add(EditTextHolder(editText, tv, ""))
            checkStatus()
        } else changeType(editText, "")
    }

    fun removeEditText(editText: EditText) {
        run out@{
            list.forEach {
                if (it.editText == editText) {
                    list.remove(it)
                    return@out
                }
            }
        }
        checkStatus()
    }

    fun changeType(editText: EditText, type: String) {
        list.forEach {
            if (it.editText == editText) {
                it.type = type
                if (TextUtils.isEmpty(it.type)) {
                    it.type = "pwd"
                }
            }
        }
        checkStatus()
    }

    fun checkStatus() {
        var able = true
        list.forEach {
            if (able) {
                if (registerPresenter != null) {
                    if (registerPresenter!!.model == null) {
                        able = false
                    } else {
                        able = it.check() && registerPresenter!!.model!!.getAgreementStatus()
                    }
                } else if (forgotPasswordPresenter != null) {
                    if (forgotPasswordPresenter!!.model == null) {
                        able = false
                    } else {
                        able = it.check() && forgotPasswordPresenter!!.model!!.getAgreementStatus()
                    }
                } else {
                    able = it.check()
                }
            } else {
                it.check()
            }
        }
        this.isEnabled = able
        if (this.isEnabled) {
            this.setBackgroundResource(R.drawable.background_circle_bule_gradient)
        } else {
            this.setBackgroundResource(R.drawable.background_grey_dark_cell)
        }
        refreshBtnState(this.isEnabled)
    }

    private fun refreshBtnState(state: Boolean) {
        if (btn2Click != null) {
            for (btn in btn2Click) {
                if (state) {
                    btn.setTextColor(enableBackgroundColor)
                } else {
                    btn.setTextColor(disableBackgroundColor)
                }
                btn.isClickable = state
            }
        }
    }

    private fun isContain(editText: EditText): Boolean {
        list.forEach {
            if (it.editText == editText) {
                return true
            }
        }
        return false
    }

    inner class EditTextHolder(et: EditText, tv: TextView?, type: String) : TextWatcher {

        var editText: EditText = et
        private var textView: TextView? = tv

        var type = "pwd"

        init {
            if (!TextUtils.isEmpty(type)) {
                this.type = type
            }
            editText.addTextChangedListener(this)
        }

        fun check(): Boolean {
            editText.text.trim().toString().let {
                when (type) {
                    "pwd" -> {
                        return if (it.matches(Regex("^(?![0-9]+\$)(?![a-z]+\$)(?![A-Z]+\$).{8,}\$"))) {
                            textView?.visibility = View.INVISIBLE
                            textView?.text = ""
                            true
                        } else {
                            textView?.visibility = View.VISIBLE
                            if (it.isEmpty())
                                textView?.text = ""
                            else
                                textView?.text = context.getString(R.string.password_style)
                            false
                        }
                    }
                    "1" -> {
                        return if (it.length == 10 && it.isDigitsOnly()) {
                            textView?.visibility = View.INVISIBLE
                            textView?.text = ""
                            true
                        } else {
                            textView?.visibility = View.VISIBLE
                            if (it.isEmpty())
                                textView?.text = ""
                            else textView?.text =
                                context.getString(R.string.mobile_phone_number_invalid)
                            false
                        }
                    }
                    "86" -> {
                        // 长度为 11 位且仅包含数字的字符串认为是电话号码
                        return if (it.length == 11 && it.isDigitsOnly()) {
                            textView?.visibility = View.INVISIBLE
                            textView?.text = ""
                            true
                        } else {
                            textView?.visibility = View.VISIBLE
                            if (it.isEmpty())
                                textView?.text = ""
                            else textView?.text =
                                context.getString(R.string.mobile_phone_number_invalid)
                            false
                        }
                    }
                    "email" -> {
                        return if (it.matches(Regex("^\\w+@(\\w+\\.)+\\w+$"))) {
                            textView?.visibility = View.INVISIBLE
                            textView?.text = ""
                            true
                        } else {
                            textView?.visibility = View.VISIBLE
                            if (it.isEmpty())
                                textView?.text = ""
                            else
                                textView?.text = context.getString(R.string.email_invalid)
                            false
                        }
                    }
                    else -> {
                        textView?.visibility = View.INVISIBLE
                        return it.isNotEmpty()
                    }
                }
            }
        }

        private fun checkPwdChar(pwd: String) {
            var hasLetter = false
            var hasNum = false
            pwd.forEach {}
        }

        override fun afterTextChanged(s: Editable?) {
            checkStatus()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
}