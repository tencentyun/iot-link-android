package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.EditNameValue
import kotlinx.android.synthetic.main.activity_add_task_name.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class EditNameActivity : BaseActivity() {
    var editNameValue = EditNameValue()

    override fun getContentView(): Int {
        return R.layout.activity_add_task_name
    }

    override fun initView() {
        var extraInfo = intent.getStringExtra(CommonField.EXTRA_INFO)
        if (TextUtils.isEmpty(extraInfo)) return
        editNameValue = JSON.parseObject(extraInfo, EditNameValue::class.java)
        if (editNameValue == null) return

        tv_title.setText(editNameValue.title)
        tv_tip_2.setText(editNameValue.tipName)
        ev_task_name.setText(editNameValue.name);
    }

    private var textWatcher = object: TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (ev_task_name.text.trim().length !in 1..20) {
                tv_ok.isClickable = false
                tv_ok.setBackgroundResource(R.drawable.background_grey_dark_cell)
            } else {
                tv_ok.isClickable = true
                tv_ok.setBackgroundResource(R.drawable.background_circle_bule_gradient)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    }

    override fun setListener() {
        ev_task_name.addTextChangedListener(textWatcher)
        iv_back.setOnClickListener { finish() }
        tv_ok.setOnClickListener {

            if (ev_task_name.text.trim().length !in 1..20) {
                if (!TextUtils.isEmpty(editNameValue.errorTip)) {
                    T.show(editNameValue.errorTip)
                } else {
                    T.show(getString(R.string.length_error))
                }
                return@setOnClickListener
            }

            val intent = Intent()
            intent.putExtra(CommonField.EXTRA_TEXT, ev_task_name.text.toString())
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}