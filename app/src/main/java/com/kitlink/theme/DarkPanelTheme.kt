package com.kitlink.theme

import android.view.LayoutInflater
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
 * 暗黑主题
 */
class DarkPanelTheme(activity: ControlPanelActivity) : PanelTheme(activity) {

    override fun getTag(): String {
        return "dark"
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
            BIG_SWITCH -> ControlDarkSwitchBigHolder(
                LayoutInflater.from(mActivity).inflate(
                    R.layout.control_dark_big_switch,
                    parent,
                    false
                )
            )
            BIG_ENUM -> ControlDarkEnumBigHolder(
                mActivity, parent, R.layout.control_dark_big_enum
            )
            BIG_NUMBER -> {
                ControlDarkNumberBigHolder(
                    mActivity, parent, R.layout.control_dark_big_int
                )
            }
            LONG_SWITCH -> ControlDarkSwitchLongHolder(
                mActivity, parent, R.layout.control_dark_long_switch
            )
            LONG -> ControlDarkLongHolder(
                mActivity, parent, R.layout.control_dark_long
            )
            MEDIUM -> ControlDarkMediumHolder(
                mActivity, parent, R.layout.control_dark_medium
            )
            else -> {
                ControlDarkSmallHolder(mActivity, parent, R.layout.control_dark_medium)
            }
        }
    }

    private var timingProject: ControlDarkCloudHolder? = null

    override fun getTimingProject(cRecyclerView: CRecyclerView): CRecyclerView.FootViewHolder<*> {
        if (timingProject == null) {
            timingProject =
                ControlDarkCloudHolder(mActivity, cRecyclerView, R.layout.control_dark_long)
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
            is ControlDarkSwitchBigHolder -> switch(position)
            is ControlDarkNumberBigHolder -> {
                if (clickView is SeekProgress)
                    onProgress(mActivity.getDeviceProperty(position), clickView)
            }
            is ControlDarkEnumBigHolder -> {
                enum(position)
            }
            is ControlDarkSwitchLongHolder -> switch(position)
            is ControlDarkLongHolder -> {
                showPopup(position)
            }
            is ControlDarkMediumHolder -> {
                when (clickView is Switch) {
                    true -> switch(position)
                    false -> {
                        showPopup(position)
                    }
                }
            }
            is ControlDarkSmallHolder -> {
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
    private fun showPopup(position: Int) {
        mActivity.getDeviceProperty(position).run {
            when {
                isEnumType() -> mActivity.showEnumPopup(this)
                isNumberType() -> mActivity.showNumberPopup(this)
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