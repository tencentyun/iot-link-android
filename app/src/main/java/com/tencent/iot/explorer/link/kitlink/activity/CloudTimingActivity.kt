package com.tencent.iot.explorer.link.kitlink.activity

import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Switch
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.kitlink.entity.TimerListEntity
import com.tencent.iot.explorer.link.kitlink.holder.FootAddTimingProjectHolder
import com.tencent.iot.explorer.link.kitlink.holder.TimerListViewHolder
import com.tencent.iot.explorer.link.mvp.IModel
import com.tencent.iot.explorer.link.mvp.model.TimerListModel
import com.tencent.iot.explorer.link.mvp.view.TimerListView
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.activity_cloud_timing.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.util.*

/**
 * 云端定时
 */
class CloudTimingActivity : MActivity(), TimerListView, CRecyclerView.RecyclerItemView {

    private var deviceEntity: DeviceEntity? = null
    private var devicePropertyList: LinkedList<DevicePropertyEntity>? = null

    private lateinit var model: TimerListModel
    private var addTimingProjectFoot: FootAddTimingProjectHolder? = null

    override fun getContentView(): Int {
        return R.layout.activity_cloud_timing
    }

    override fun getModel(): IModel? {
        return model
    }

    override fun onResume() {
        super.onResume()
        model.refreshTimerList()
    }

    override fun initView() {
        model = TimerListModel(this)
        deviceEntity = get("device")
        devicePropertyList = get("property")
        tv_title.text = getString(R.string.cloud_timing)
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        crv_cloud_timing.setList(model.timerList)
        crv_cloud_timing.addRecyclerItemView(this)
        model.deviceEntity = deviceEntity
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_no_timing_project.setOnClickListener {
            goAddTimerActivity(null)
        }
    }

    /**
     * 添加footer
     */
    private fun addFooter() {
        if (addTimingProjectFoot == null) {
            addTimingProjectFoot = FootAddTimingProjectHolder(
                this,
                crv_cloud_timing,
                R.layout.footer_add_timing_project
            )
        }
        addTimingProjectFoot?.footListener = object : CRecyclerView.FootListener {
            override fun doAction(
                holder: CRecyclerView.FootViewHolder<*>,
                clickView: View,
                position: Int
            ) {
                goAddTimerActivity(null)
            }
        }
        crv_cloud_timing.addFooter(addTimingProjectFoot!!)
    }

    /**
     * 显示列表
     */
    override fun showTimerList(size: Int) {
        if (size <= 0) {
            addTimingProjectFoot?.let {
                crv_cloud_timing.removeFooter(it)
            }
            crv_cloud_timing.visibility = View.GONE
            rl_no_timing_project.visibility = View.VISIBLE
        } else {
            rl_no_timing_project.visibility = View.GONE
            crv_cloud_timing.visibility = View.VISIBLE
            addFooter()
        }
        crv_cloud_timing.notifyDataChanged()
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        when (clickView) {
            is Switch -> {
                model.switchTimer(position)
            }
            is RelativeLayout -> model.deleteCloudTiming(position)
            else -> {
                goAddTimerActivity(model.timerList[position])
            }
        }
    }

    /**
     *  跳转到添加界面
     */
    private fun goAddTimerActivity(timerListEntity: TimerListEntity?) {
        timerListEntity?.let {
            put("timer", it)
        }
        jumpActivity(AddTimerActivity::class.java)
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return TimerListViewHolder(this, parent, R.layout.item_timer_list)
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

}
