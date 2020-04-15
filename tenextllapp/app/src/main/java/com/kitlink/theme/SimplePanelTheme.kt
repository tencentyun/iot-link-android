package com.kitlink.theme

import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.kitlink.R
import com.kitlink.activity.ControlPanelActivity
import com.kitlink.entity.DevicePropertyEntity
import com.kitlink.holder.*
import com.view.progress.SeekProgress
import com.view.recyclerview.CRecyclerView

/**
 * 简约主题
 */
class SimplePanelTheme(activity: ControlPanelActivity) : PanelTheme(activity) {

    override fun getTag(): String {
        return "simple"
    }

    override fun getSpanSize(position: Int): Int {
        return when (mActivity.getDeviceProperty(position).type) {
            "btn-big" -> {
                6
            }
            "btn-col-1" -> {
                6
            }
            "btn-col-2" -> {
                3
            }
            else -> {//btn-col-3
                2
            }
        }
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return when (viewType) {
            BIG_SWITCH -> ControlSimpleSwitchBigHolder(
                mActivity, parent, R.layout.control_simple_big_switch
            )
            BIG_ENUM -> ControlSimpleEnumBigHolder(
                mActivity, parent, R.layout.control_simple_big_enum
            )
            BIG_NUMBER -> {
                ControlSimpleNumberBigHolder(
                    mActivity, parent, R.layout.control_simple_big_int
                )
            }
            LONG_SWITCH -> ControlSimpleSwitchLongHolder(
                mActivity, parent, R.layout.control_simple_long_switch
            )
            LONG -> ControlSimpleLongHolder(
                mActivity, parent, R.layout.control_simple_long
            )
            MEDIUM -> ControlSimpleMediumHolder(
                mActivity, parent, R.layout.control_simple_medium
            )
            else -> {
                ControlSimpleSmallHolder(mActivity, parent, R.layout.control_simple_medium)
            }
        }
    }

    private var timingProject: ControlSimpleCloudHolder? = null

    override fun getTimingProject(cRecyclerView: CRecyclerView): CRecyclerView.FootViewHolder<*> {
        if (timingProject == null) {
            timingProject =
                ControlSimpleCloudHolder(mActivity, cRecyclerView, R.layout.control_simple_long)
            timingProject!!.footListener = object : CRecyclerView.FootListener {
                override fun doAction(
                    holder: CRecyclerView.FootViewHolder<*>,
                    clickView: View,
                    position: Int
                ) {
                    mActivity.jumpToCloudTiming()
                }
            }
        }
        return timingProject!!
    }

    /**
     * 列表监听
     */
    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        when (viewHolder) {
            is ControlSimpleSwitchBigHolder -> switch(position)
            is ControlSimpleNumberBigHolder -> {
                if (clickView is SeekProgress)
                    onProgress(mActivity.getDeviceProperty(position), clickView)
            }
            is ControlSimpleEnumBigHolder -> {
                enum(position)
            }
            is ControlSimpleSwitchLongHolder -> switch(position)
            is ControlSimpleLongHolder -> {
                showPopup(position)
            }
            is ControlSimpleMediumHolder -> {
                when (clickView is Switch) {
                    true -> switch(position)
                    false -> {
                        showPopup(position)
                    }
                }
            }
            is ControlSimpleSmallHolder -> {
                when (clickView is Switch) {
                    true -> switch(position)
                    false -> {
                        showPopup(position)
                    }
                }
            }
        }
    }

    /**
     * 显示对应的弹框
     */
    private fun showPopup(position: Int){
        mActivity.getDeviceProperty(position).run {
            when{
                isEnumType()->mActivity.showEnumPopup(this)
                isNumberType()->mActivity.showNumberPopup(this)
            }
        }
    }

    /**
     *  开关
     */
    private fun switch(position: Int) {
        mActivity.getDeviceProperty(position).run {
            mActivity.controlDevice(
                id,
                if (getValue() == "0") "1" else "0"
            )
        }
    }

    /**
     * 大按钮进度条
     */
    private fun onProgress(entity: DevicePropertyEntity, sp: SeekProgress) {
        mActivity.controlDevice(entity.id, sp.getProgress().toString())
    }

    /**
     * 大按钮列表
     */
    private fun enum(position: Int) {
        mActivity.getDeviceProperty(0).let {
            it.enumEntity?.run {
                mActivity.controlDevice(it.id, position.toString())
            }
        }
    }

}