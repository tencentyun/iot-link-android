package com.tencent.iot.explorer.link.kitlink.activity

import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.dialog.ListOptionsDialog
import kotlinx.android.synthetic.main.activity_add_manual_task.*
import kotlinx.android.synthetic.main.menu_back_layout.*


class AddManualTaskActivity : BaseActivity() {

    private var options = ArrayList<String>()
    private var optionsDialog: ListOptionsDialog? = null

    override fun getContentView(): Int {
        return R.layout.activity_add_manual_task
    }

    override fun initView() {
        tv_title.setText(R.string.add_manual_smart)
        options.add(getString(R.string.control_dev))
        options.add(getString(R.string.delay_time))
        optionsDialog = ListOptionsDialog(this, options)
        optionsDialog?.setOnDismisListener(onItemClickedListener)
    }

    private var onItemClickedListener = ListOptionsDialog.OnDismisListener {
        if (it == 0) {

        } else if (it == 1) {

        }
    }


    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_add_now_btn.setOnClickListener{ optionsDialog!!.show() }
    }
}