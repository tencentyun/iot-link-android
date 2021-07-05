package com.tencent.iot.explorer.link.demo.core.activity

import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.log.L
import kotlinx.android.synthetic.main.activity_set_password.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 修改密码
 */
class SetPasswordActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_set_password
    }

    override fun initView() {
        tv_title.text = getString(R.string.modify_password)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_commit_password.setOnClickListener {
            setPwd()
        }
    }

    private fun matches(text:String):Boolean{
        return text.matches(Regex("^(?![0-9]+\$)(?![a-z]+\$)(?![A-Z]+\$).{8,}\$"))
    }

    private fun setPwd() {
        val oldPwd = et_old_pwd.text.toString().trim()
        val newPwd = et_new_pwd.text.toString().trim()
        val verifyPwd = et_verify_pwd.text.toString().trim()
        if (!matches(oldPwd)) {
            show("旧密码为8-20位的字母和数字组成")
            return
        }
        if (!matches(newPwd)) {
            show("新密码为8-20位的字母和数字组成")
            return
        }
        if (!matches(verifyPwd)) {
            show("确认密码为8-20位的字母和数字组成")
            return
        }
        if (newPwd != verifyPwd) {
            show("新密码与确认密码不一致")
            return
        }
        IoTAuth.passwordImpl.resetPassword(oldPwd, newPwd, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.d(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    show("密码修改成功")
                    finish()
                } else {
                    show(response.msg)
                }
            }
        })
    }
}
