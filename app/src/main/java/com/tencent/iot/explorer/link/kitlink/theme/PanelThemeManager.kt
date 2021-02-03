package com.tencent.iot.explorer.link.kitlink.theme

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.tencent.iot.explorer.link.kitlink.activity.ControlPanelActivity
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerDivider
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView

/**
 *  控制面板父类
 */
class PanelThemeManager private constructor() {

    private object ThemeManagerHolder {
        val manager = PanelThemeManager()
    }

    companion object {
        val instance = ThemeManagerHolder.manager
    }

    private var theme: PanelTheme? = null
    private var crv: CRecyclerView? = null

    /**
     * 绑定控件
     */
    fun bindView(activity: ControlPanelActivity, cRecyclerView: CRecyclerView) {
        crv = cRecyclerView
        val gridLayoutManager = GridLayoutManager(activity, 6)
        val myGridDivider =
            CRecyclerDivider(activity.dp2px(10), activity.dp2px(9), activity.dp2px(16))
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                theme?.run {
                    return getSpanSize(position)
                }
                return 6
            }
        }

        myGridDivider.gridItemOffsetsListener = object : CRecyclerDivider.GridItemOffsetsListener {

            override fun setItemOffsets(position: Int, viewType: Int): Boolean {
                return (viewType == PanelTheme.BIG_SWITCH || viewType == PanelTheme.BIG_NUMBER || viewType == PanelTheme.BIG_ENUM)
            }

            override fun itemOffsets(
                position: Int,
                spanSize: Int,
                span: Int,
                spanCount: Int,
                row: Int,
                maxRow: Int,
                viewType: Int
            ): Rect {
                return Rect(0, 0, 0, 0)
            }
        }
        cRecyclerView.addItemDecoration(myGridDivider)
        cRecyclerView.layoutManager = gridLayoutManager
    }

    /**
     * 显示主题
     */
    @Synchronized
    fun showTheme(activity: ControlPanelActivity, timingProject: Boolean) {
        if (crv == null) return
        if (theme == null) {
            theme = SimplePanelTheme(activity)
            if (timingProject) {
                crv!!.addFooter(theme!!.getTimingProject(crv!!))
            }
        }
        crv?.notifyDataChanged()
    }

    /**
     * 监听
     */
    fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        theme?.doAction(viewHolder, clickView, position)
    }


    fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*>? {
        if (theme != null) {
            return theme!!.getViewHolder(parent, viewType)
        } else {
            return null
        }
    }

    /**
     * 判断面板类型
     */
    fun getViewType(entity: DevicePropertyEntity): Int {
        return when (entity.type) {
            "btn-big" -> {
                when {
                    entity.isBoolType() -> PanelTheme.BIG_SWITCH
                    entity.isNumberType() -> PanelTheme.BIG_NUMBER
                    entity.isEnumType() -> PanelTheme.BIG_ENUM
                    else -> PanelTheme.LONG //无法判断出来的就统一用长按钮
                }
            }
            "btn-col-1" -> {
                when {
                    entity.isBoolType() -> PanelTheme.LONG_SWITCH
                    else -> PanelTheme.LONG
                }
            }
            "btn-col-2" -> {
                PanelTheme.MEDIUM
            }
            else -> {//btn-col-3
                PanelTheme.SMALL
            }
        }
    }

    fun destroy() {
        theme = null
        crv = null
    }
}