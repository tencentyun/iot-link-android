package com.kitlink.activity

import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.kitlink.R
import com.kitlink.entity.DeviceEntity
import com.kitlink.entity.DevicePropertyEntity
import com.kitlink.entity.TimerListEntity
import com.kitlink.holder.DeviceActionViewHolder
import com.kitlink.holder.HeadAddTimerHolder
import com.kitlink.holder.OnTimeChangedListener
import com.kitlink.popup.EditPopupWindow
import com.kitlink.popup.EnumPopupWindow
import com.kitlink.popup.NumberPopupWindow
import com.kitlink.response.BaseResponse
import com.kitlink.util.HttpRequest
import com.kitlink.util.JsonManager
import com.kitlink.util.MyCallback
import com.util.L
import com.util.T
import com.view.recyclerview.CRecyclerView
import com.kitlink.activity.BaseActivity
import com.kitlink.holder.AddTimingFootHolder
import kotlinx.android.synthetic.main.activity_add_timer.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.lang.StringBuilder
import java.util.*

/**
 * 添加云端定时
 */
class AddTimerActivity : BaseActivity(), CRecyclerView.RecyclerItemView, MyCallback {

    private var deviceEntity: DeviceEntity? = null
    private var timerListEntity: TimerListEntity? = null
    private var devicePropertyList: LinkedList<DevicePropertyEntity>? = null

    private lateinit var headHolder: HeadAddTimerHolder
    private lateinit var addTimingFootHolder: AddTimingFootHolder

    //定时实体
    private lateinit var mTimerListEntity: TimerListEntity

    //定时动作
    private lateinit var deviceAction: JSONObject

    private var editPopupWindow: EditPopupWindow? = null
    private var numberPopupWindow: NumberPopupWindow? = null
    private var enumPopupWindow: EnumPopupWindow? = null
    private var boolPopupWindow: EnumPopupWindow? = null

    override fun getContentView(): Int {
        return R.layout.activity_add_timer
    }

    /**
     * 头部监听器
     */
    private val headListener = object : CRecyclerView.HeadListener {
        override fun doAction(
            holder: CRecyclerView.HeadViewHolder<*>,
            clickView: View,
            position: Int
        ) {
            when (position) {
                0 -> showEditPopup()
                1 -> {
                    selectWeekRepeat()
                }
            }
        }
    }

    /**
     *  时间选择
     */
    private val onTimeChangedListener = object : OnTimeChangedListener {
        override fun onTimeChangedListener(view: TimePicker, hour: Int, minute: Int) {
            mTimerListEntity.TimePoint = "$hour:${if (minute < 10) "0" else ""}$minute"
        }
    }

    override fun initView() {
        deviceEntity = get("device")
        timerListEntity = get("timer")
        devicePropertyList = get("property")
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = if (timerListEntity == null) getString(R.string.add_timer) else
            getString(R.string.modify_timer)
        mTimerListEntity = timerListEntity ?: createNewTimerListEntity()
        deviceAction = JSON.parseObject(mTimerListEntity.Data)
        devicePropertyList?.run {
            crv_add_timer.setList(this)
            crv_add_timer.addRecyclerItemView(this@AddTimerActivity)
            headHolder =
                HeadAddTimerHolder(this@AddTimerActivity, crv_add_timer, R.layout.head_add_timer)
            headHolder.entity = mTimerListEntity
            headHolder.headListener = headListener
            headHolder.onTimeChangedListener = onTimeChangedListener
            crv_add_timer.addHeader(headHolder)
            addFooter()
        }
    }

    private fun addFooter() {
        addTimingFootHolder =
            AddTimingFootHolder(this, crv_add_timer, R.layout.foot_add_timing)
        crv_add_timer.addFooter(addTimingFootHolder)
        addTimingFootHolder.footListener = object : CRecyclerView.FootListener {
            override fun doAction(
                holder: CRecyclerView.FootViewHolder<*>,
                clickView: View,
                position: Int
            ) {
                if (timerListEntity == null)
                    createTimer()
                else
                    modifyTimer()
            }
        }
    }

    private fun createNewTimerListEntity(): TimerListEntity {
        TimerListEntity().run {
            this.Data = "{}"
            this.TimerName = "我的定时"
            this.TimePoint = "12:00"
            return this
        }
    }

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
     * 修改定时任务
     */
    private fun modifyTimer() {
        mTimerListEntity.let {
            HttpRequest.instance.modifyTimer(
                it.ProductId,
                it.DeviceName,
                it.TimerName,
                it.TimerId,
                if (it.Repeat == 1) it.Days else "1111111",
                it.TimePoint,
                it.Repeat,
                deviceAction.toJSONString(),
                this
            )
        }
    }

    /**
     * 保存定时任务
     */
    private fun createTimer() {
        deviceEntity?.run {
            val timerName = mTimerListEntity.TimerName
            val repeat = mTimerListEntity.Repeat
            val timePoint = mTimerListEntity.TimePoint
            val days = if (repeat == 1) mTimerListEntity.Days
            else "1111111"

            if (TextUtils.isEmpty(timerName)) {
                T.show("请填写定时名称")
                return
            }
            HttpRequest.instance.createTimer(
                ProductId,
                DeviceName,
                timerName,
                days,
                timePoint,
                repeat,
                deviceAction.toJSONString(),
                this@AddTimerActivity
            )
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            finish()
        } else {
            T.show(response.msg)
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        devicePropertyList!![position].run {
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

    /**
     * 选择重复
     */
    private fun selectWeekRepeat() {
        WeekRepeatActivity.days =
            if (mTimerListEntity.Repeat == 0) "0000000" else mTimerListEntity.Days
        startActivityForResult(Intent(this, WeekRepeatActivity::class.java), 1024)
    }

    /**
     * 显示编辑框
     */
    private fun showEditPopup() {
        if (editPopupWindow == null) {
            editPopupWindow = EditPopupWindow(this)
        }
        editPopupWindow!!.setShowData(
            getString(R.string.timing_name), mTimerListEntity.TimerName
        )
        editPopupWindow!!.onVerifyListener = object : EditPopupWindow.OnVerifyListener {
            override fun onVerify(text: String) {
                if (text.isNotEmpty()) {
                    mTimerListEntity.TimerName = text
                }
                editPopupWindow?.dismiss()
                crv_add_timer.notifyDataChanged()
            }
        }
        editPopupWindow!!.setBg(add_timer_bg)
        editPopupWindow!!.show(add_timer)
    }

    /**
     * 显示列表框
     */
    private fun showBoolPopup(entity: DevicePropertyEntity) {
        if (boolPopupWindow == null) {
            boolPopupWindow = EnumPopupWindow(this)
            boolPopupWindow!!.showDeleteButton(true)
        }
        entity.run {
            boolPopupWindow!!.setList(boolEntity!!.mapping)
            boolPopupWindow!!.selectKey = mTimerListEntity.getValueForData(id)
            boolPopupWindow!!.onUploadListener = object : EnumPopupWindow.OnUploadListener {
                override fun upload(value: String) {
                    if (!TextUtils.isEmpty(value)) {
                        deviceAction[id] = value
                        crv_add_timer.notifyDataChanged()
                    }
                    boolPopupWindow?.dismiss()
                }
            }
            boolPopupWindow!!.onDeleteListener = object : EnumPopupWindow.OnDeleteListener {
                override fun onDelete() {
                    deviceAction.remove(id)
                    boolPopupWindow?.dismiss()
                    crv_add_timer.notifyDataChanged()
                }
            }
            boolPopupWindow!!.showTitle(entity.name)
            boolPopupWindow!!.setBg(add_timer_bg)
            boolPopupWindow!!.show(add_timer)
        }
    }

    /**
     * 显示列表框
     */
    private fun showEnumPopup(entity: DevicePropertyEntity) {
        if (enumPopupWindow == null) {
            enumPopupWindow = EnumPopupWindow(this)
            enumPopupWindow!!.showDeleteButton(true)
        }
        entity.run {
            enumPopupWindow!!.setList(enumEntity!!.mapping)
            enumPopupWindow!!.selectKey = mTimerListEntity.getValueForData(id)
            enumPopupWindow!!.onUploadListener = object : EnumPopupWindow.OnUploadListener {
                override fun upload(value: String) {
                    if (!TextUtils.isEmpty(value)) {
                        deviceAction[id] = value
                        crv_add_timer.notifyDataChanged()
                    }
                    enumPopupWindow?.dismiss()
                }
            }
            enumPopupWindow!!.onDeleteListener = object : EnumPopupWindow.OnDeleteListener {
                override fun onDelete() {
                    deviceAction.remove(id)
                    enumPopupWindow?.dismiss()
                    crv_add_timer.notifyDataChanged()
                }
            }
            enumPopupWindow!!.showTitle(entity.name)
            enumPopupWindow!!.setBg(add_timer_bg)
            enumPopupWindow!!.show(add_timer)
        }
    }


    /**
     * 显示进度条框
     */
    private fun showNumberPopup(entity: DevicePropertyEntity) {
        if (numberPopupWindow == null) {
            numberPopupWindow = NumberPopupWindow(this)
            numberPopupWindow!!.showDeleteButton(true)
        }
        entity.run {
            numberPopupWindow!!.showTitle(this.name)
            numberPopupWindow!!.setUnit(entity.getUnit())
            val p = deviceAction.getIntValue(id)
            val min = numberEntity!!.min.toDouble().toInt()
            numberPopupWindow!!.setProgress(if (p < min) min else p)
            numberPopupWindow!!.setRange(
                min,
                numberEntity!!.max.toDouble().toInt()
            )
            numberPopupWindow!!.setBg(add_timer_bg)
            numberPopupWindow!!.show(add_timer)
            numberPopupWindow!!.onDeleteListener = object : NumberPopupWindow.OnDeleteListener {
                override fun onDelete() {
                    deviceAction.remove(id)
                    numberPopupWindow?.dismiss()
                    crv_add_timer.notifyDataChanged()
                }
            }
            numberPopupWindow!!.onUploadListener = object : NumberPopupWindow.OnUploadListener {
                override fun upload(progress: Int) {
                    deviceAction[id] = progress
                    numberPopupWindow?.dismiss()
                    crv_add_timer.notifyDataChanged()
                }
            }
        }
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        val holder = DeviceActionViewHolder(this, parent, R.layout.item_device_action)
        holder.deviceAction = deviceAction
        return holder
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1024 && resultCode == 200) {
            mTimerListEntity.Days = WeekRepeatActivity.days
            mTimerListEntity.Repeat = if (mTimerListEntity.Days.contains("1")) {
                1
            } else {
                0
            }
            crv_add_timer.notifyDataChanged()
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
