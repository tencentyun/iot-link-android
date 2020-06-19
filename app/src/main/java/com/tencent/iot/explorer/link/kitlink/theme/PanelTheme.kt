package com.tencent.iot.explorer.link.kitlink.theme

import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.kitlink.activity.ControlPanelActivity
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView

/**
 * 主题父类
 */
abstract class PanelTheme(activity: ControlPanelActivity) {

    var mActivity = activity

    companion object {
        //     标准
        const val STANDARD = "standard"
        //     简约
        const val SIMPLE = "simple"
        //     暗黑
        const val DARK = "dark"

        const val BIG_SWITCH = 0
        const val BIG_NUMBER = 1
        const val BIG_ENUM = 2
        const val LONG_SWITCH = 3
        const val LONG = 4
        const val MEDIUM = 5
        const val SMALL = 6
    }

    abstract fun getSpanSize(position: Int): Int

    abstract fun getTag(): String

    abstract fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*>

    abstract fun doAction(viewHolder: CRecyclerView.CViewHolder<*>, clickView: View, position: Int)

    abstract fun getTimingProject(cRecyclerView: CRecyclerView):CRecyclerView.FootViewHolder<*>
}