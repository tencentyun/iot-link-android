package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.text.TextUtils
import android.view.View
import com.alibaba.fastjson.JSON
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.dialog.SuccessToastDialog
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import kotlinx.android.synthetic.main.activity_complete_task_info.*
import kotlinx.android.synthetic.main.menu_back_layout.*


class CompleteTaskInfoActivity : BaseActivity() {

    @Volatile
    private var smartPicUrl = ""
    private var smartName = ""
    private var handler = Handler()

    override fun getContentView(): Int {
        return R.layout.activity_complete_task_info
    }

    override fun initView() {
        tv_title.setText(R.string.complete_task_info)
        loadView()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        layout_smart_pic.setOnClickListener {
            var intent = Intent(this, SelectTaskPicActivity::class.java)
            startActivityForResult(intent, CommonField.REQUEST_PIC_REQ_CODE)
        }
        layout_smart_name.setOnClickListener {
            var intent = Intent(this, AddTaskNameActivity::class.java)
            startActivityForResult(intent, CommonField.REQUEST_TASK_NAME_REQ_CODE)
        }
        tv_ok.setOnClickListener {
            var dialog = SuccessToastDialog(this)
            dialog.show()
            Thread(
                {
                    Thread.sleep(1000)
                    handler.post {
                        dialog.dismiss()
                        finish()
                        jumpActivity(DeviceModeInfoActivity::class.java)
                    }
                }
            ).start()
        }
    }

    fun loadView() {
        if (TextUtils.isEmpty(smartName.trim())) {
            tv_unset_tip_2.setText(R.string.unset)
        } else {
            tv_unset_tip_2.setText(smartName)
        }

        if (TextUtils.isEmpty(smartPicUrl.trim())) {
            iv_smart_background.visibility = View.GONE
            tv_unset_tip_1.visibility = View.VISIBLE
        } else {
            iv_smart_background.visibility = View.VISIBLE
            tv_unset_tip_1.visibility = View.GONE
            Picasso.get().load(smartPicUrl).into(iv_smart_background)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CommonField.REQUEST_PIC_REQ_CODE &&  // 添加任务
            resultCode == Activity.RESULT_OK && data != null) {
            var picUrl = data?.getStringExtra(CommonField.EXTRA_PIC_URL)
            smartPicUrl = picUrl
        } else if (requestCode == CommonField.REQUEST_TASK_NAME_REQ_CODE &&  // 添加任务
            resultCode == Activity.RESULT_OK && data != null) {
            var name = data?.getStringExtra(CommonField.EXYRA_TASK_NAME)
            smartName = name.trim()
        }

        loadView()
    }
}