package com.tencent.iot.explorer.link.core.demo.activity

import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.TimePicker
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.ControlPanel
import com.tencent.iot.explorer.link.core.auth.entity.Device
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.adapter.AddTimingProjectAdapter
import com.tencent.iot.explorer.link.core.demo.adapter.OnItemListener
import com.tencent.iot.explorer.link.core.demo.entity.TimingProject
import com.tencent.iot.explorer.link.core.demo.holder.AddTimingActionHolder
import com.tencent.iot.explorer.link.core.demo.holder.AddTimingFooterHolder
import com.tencent.iot.explorer.link.core.demo.holder.AddTimingHeaderHolder
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder
import com.tencent.iot.explorer.link.core.demo.log.L
import com.tencent.iot.explorer.link.core.demo.popup.EditPopupWindow
import com.tencent.iot.explorer.link.core.demo.popup.EnumPopupWindow
import com.tencent.iot.explorer.link.core.demo.popup.NumberPopupWindow
import kotlinx.android.synthetic.main.activity_add_timing_project.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.lang.StringBuilder
import java.util.*

/**
 * 添加定时
 */
class AddTimingProjectActivity : BaseActivity(), MyCallback {

    private lateinit var mTimingProject: TimingProject
    private lateinit var adapter: AddTimingProjectAdapter
    private val mList = arrayListOf<Any>()
    //定时动作
    private lateinit var deviceAction: JSONObject

    private var editPopupWindow: EditPopupWindow? = null
    private var numberPopupWindow: NumberPopupWindow? = null
    private var enumPopupWindow: EnumPopupWindow? = null
    private var boolPopupWindow: EnumPopupWindow? = null

    override fun getContentView(): Int {
        return R.layout.activity_add_timing_project
    }

    override fun initView() {
        mTimingProject = get<TimingProject>("timing") ?: createTimingProject()
        deviceAction = JSON.parseObject(mTimingProject.Data)
        tv_title.text = if (mTimingProject.TimerId == "") getString(R.string.add_timer) else
            getString(R.string.modify_timer)
        rv_add_timing_project.layoutManager = LinearLayoutManager(this)
        adapter = AddTimingProjectAdapter(this, mList)
        adapter.deviceAction = deviceAction
        rv_add_timing_project.adapter = adapter
        getList()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                when (holder) {
                    is AddTimingHeaderHolder -> {
                        when (position) {
                            0 -> showEditPopup()
                            1 -> {
                                selectWeekRepeat()
                            }
                        }
                    }
                    is AddTimingActionHolder -> {
                        (mList[position] as? ControlPanel)?.run {
                            when {
                                isNumberType() -> {
                                    showNumberPopup(this)
                                }
                                isEnumType() -> {
                                    showEnumPopup(this)
                                }
                                isBoolType() -> {
                                    showBoolPopup(this)
                                }
                            }
                        }
                    }
                    is AddTimingFooterHolder -> {
                        commitTimingProject()
                    }
                }
            }
        })
        adapter.onTimeChangedListener = TimePicker.OnTimeChangedListener { _, hourOfDay, minute ->
            mTimingProject.TimePoint = "$hourOfDay:${if (minute < 10) "0" else ""}$minute"
        }
    }

    /**
     * 创建一个定时器对象
     */
    private fun createTimingProject(): TimingProject {
        TimingProject().run {
            this.Data = "{}"
            this.Days = getToday()
            this.TimerName = "我的定时"
            this.TimePoint = "12:00"
            get<Device>("device")?.let {
                this.ProductId = it.ProductId
                this.DeviceName = it.DeviceName
            }
            return this
        }
    }

    /**
     * 当前时间对应的星期
     */
    private fun getToday(): String {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
        val sb = StringBuilder()
        for (i in 0..6) {
            if (i == day) {
                sb.append(1)
            } else {
                sb.append(0)
            }
        }
        return sb.toString()
    }

    /**
     * 整理列表数据
     */
    private fun getList() {
        mList.add(mTimingProject)
        IoTAuth.deviceImpl.panelList().forEachIndexed { _, controlPanel ->
            val panel = ControlPanel()
            panel.id = controlPanel.id
            panel.name = controlPanel.name
            panel.valueType = controlPanel.valueType
            panel.type = controlPanel.type
            panel.define = controlPanel.define
            panel.desc = controlPanel.desc
            panel.value = controlPanel.value
            mList.add(panel)
        }
        mList.add("footer")
    }

    /**
     * 提交定时任务
     */
    private fun commitTimingProject() {
        mTimingProject.let {
            if (it.TimerId != "") {
                IoTAuth.timingImpl.modifyTimer(
                    it.ProductId, it.DeviceName, it.TimerName, it.TimerId, it.Days, it.TimePoint, 1,
                    deviceAction.toJSONString(),
                    this
                )
            } else {
                if (TextUtils.isEmpty(it.TimerName)) {
                    show("请填写定时名称")
                    return
                }
                IoTAuth.timingImpl.createTimer(
                    it.ProductId, it.DeviceName, it.TimerName, it.Days, it.TimePoint, 1,
                    deviceAction.toJSONString(),
                    this
                )
            }
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            finish()
        } else {
            show(response.msg)
        }
    }

    /**
     * 显示列表
     */
    private fun showList() {
        adapter.notifyDataSetChanged()
    }

    /**
     * 显示编辑框
     */
    private fun showEditPopup() {
        if (editPopupWindow == null) {
            editPopupWindow = EditPopupWindow(this)
        }
        editPopupWindow!!.setShowData(
            getString(R.string.timing_name), mTimingProject.TimerName
        )
        editPopupWindow!!.onVerifyListener = object : EditPopupWindow.OnVerifyListener {
            override fun onVerify(text: String) {
                if (text.isNotEmpty()) {
                    mTimingProject.TimerName = text
                }
                editPopupWindow?.dismiss()
                showList()
            }
        }
        editPopupWindow!!.setBg(add_timing_project_bg)
        editPopupWindow!!.show(add_timing_project)
    }

    /**
     * 显示列表框
     */
    private fun showBoolPopup(entity: ControlPanel) {
        if (boolPopupWindow == null) {
            boolPopupWindow = EnumPopupWindow(this)
            boolPopupWindow!!.showDeleteButton(true)
        }
        entity.run {
            boolPopupWindow!!.setList(define)
            boolPopupWindow!!.selectValue = mTimingProject.getValueForData(id)
            boolPopupWindow!!.onUploadListener = object : EnumPopupWindow.OnUploadListener {
                override fun upload(value: String) {
                    if (!TextUtils.isEmpty(value)) {
                        deviceAction[id] = value
                        showList()
                    }
                    boolPopupWindow?.dismiss()
                }
            }
            boolPopupWindow!!.onDeleteListener = object : EnumPopupWindow.OnDeleteListener {
                override fun onDelete() {
                    deviceAction.remove(id)
                    boolPopupWindow?.dismiss()
                    showList()
                }
            }
            boolPopupWindow!!.showTitle(entity.name)
            boolPopupWindow!!.setBg(add_timing_project_bg)
            boolPopupWindow!!.show(add_timing_project)
        }
    }

    /**
     * 显示列表框
     */
    private fun showEnumPopup(entity: ControlPanel) {
        if (enumPopupWindow == null) {
            enumPopupWindow = EnumPopupWindow(this)
            enumPopupWindow!!.showDeleteButton(true)
        }
        entity.run {
            enumPopupWindow!!.setList(define)
            enumPopupWindow!!.selectValue = mTimingProject.getValueForData(id)
            enumPopupWindow!!.onUploadListener = object : EnumPopupWindow.OnUploadListener {
                override fun upload(value: String) {
                    if (!TextUtils.isEmpty(value)) {
                        deviceAction[id] = value
                        showList()
                    }
                    enumPopupWindow?.dismiss()
                }
            }
            enumPopupWindow!!.onDeleteListener = object : EnumPopupWindow.OnDeleteListener {
                override fun onDelete() {
                    deviceAction.remove(id)
                    enumPopupWindow?.dismiss()
                    showList()
                }
            }
            enumPopupWindow!!.showTitle(entity.name)
            enumPopupWindow!!.setBg(add_timing_project_bg)
            enumPopupWindow!!.show(add_timing_project)
        }
    }


    /**
     * 显示进度条框
     */
    private fun showNumberPopup(entity: ControlPanel) {
        if (numberPopupWindow == null) {
            numberPopupWindow = NumberPopupWindow(this)
            numberPopupWindow!!.showDeleteButton(true)
        }
        entity.run {
            numberPopupWindow!!.showTitle(this.name)
            numberPopupWindow!!.setProgress(mTimingProject.getIntForData(id))
            numberPopupWindow!!.setBg(add_timing_project_bg)
            numberPopupWindow!!.show(add_timing_project)
            numberPopupWindow!!.onDeleteListener = object : NumberPopupWindow.OnDeleteListener {
                override fun onDelete() {
                    deviceAction.remove(id)
                    numberPopupWindow?.dismiss()
                    showList()
                }
            }
            numberPopupWindow!!.onUploadListener = object : NumberPopupWindow.OnUploadListener {
                override fun upload(progress: Int) {
                    deviceAction[id] = progress
                    numberPopupWindow?.dismiss()
                    showList()
                }
            }
        }
    }

    /**
     * 选择重复
     */
    private fun selectWeekRepeat() {
        put("repeat", mTimingProject)
        startActivityForResult(Intent(this, WeekRepeatActivity::class.java), 1024)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1024 && resultCode == 200) {
            showList()
        }
    }

    override fun onBackPressed() {
        editPopupWindow?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        numberPopupWindow?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        enumPopupWindow?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        boolPopupWindow?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        super.onBackPressed()
    }
}
