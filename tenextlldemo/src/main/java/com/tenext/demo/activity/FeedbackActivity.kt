package com.tenext.demo.activity

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.response.BaseResponse
import com.tenext.demo.R
import com.tenext.demo.log.L
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 意见反馈
 */
class FeedbackActivity : BaseActivity(), MyCallback {

    private var isCommit = false

    override fun getContentView(): Int {
        return R.layout.activity_feedback
    }

    override fun initView() {
        tv_title.text = getString(R.string.feedback)
        val length = et_feedback_problem.length()
        tv_feedback_count.text = "$length/200"
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_feedback_commit.setOnClickListener {
            commit()
        }
        et_feedback_problem.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length <= 200) {
                        tv_feedback_count.text = "${it.length}/200"
                        return
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
    }

    private fun commit() {
        if (isCommit) return
        val problem = et_feedback_problem.text.trim().toString()
        var phone = et_feedback_phone.text.trim().toString()
        val picture = et_picture.text.toString().trim()
        if (TextUtils.isEmpty(phone))
            phone = "13800138000"
        if (TextUtils.isEmpty(problem)) {
            show("请填写问题描述")
            return
        }
        if (problem.trim().length < 10) {
            show("请填写不少于10个字的问题描述")
            return
        }
        IoTAuth.userImpl.feedback(problem, phone, picture, this)
        isCommit = true
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        runOnUiThread {
            isCommit = false
            if (response.isSuccess()) {
                show("提交成功")
                et_feedback_problem.setText("")
            } else {
                show(response.msg)
            }
        }
    }

}
