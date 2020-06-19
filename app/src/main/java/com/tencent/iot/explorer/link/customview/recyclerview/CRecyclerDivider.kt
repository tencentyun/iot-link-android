package com.tencent.iot.explorer.link.customview.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CRecyclerDivider : RecyclerView.ItemDecoration {

    private var horizontalDecoration = 0 // 水平间距
    private var verticalDecoration = 0 // 垂直间距
    private var edgeDecoration = 0 // 间距
    var gridItemOffsetsListener: GridItemOffsetsListener? = null
    var linearItemOffsetsListener: LinearItemOffsetsListener? = null

    constructor(
        horizontalDecoration: Int,
        verticalDecoration: Int,
        edgeDecoration: Int
    ) {
        this.horizontalDecoration = horizontalDecoration
        this.verticalDecoration = verticalDecoration
        this.edgeDecoration = if (edgeDecoration < 0) 0 else edgeDecoration
    }

    /**
     * 设置分割线
     */
    fun setDecoration(horizontalDecoration: Int, verticalDecoration: Int) {
        this.horizontalDecoration = horizontalDecoration
        this.verticalDecoration = verticalDecoration
    }

    /**
     * 边沿分割线设置大于零时
     */
    fun setEdgeDecoration(edgeDecoration: Int) {
        this.edgeDecoration = if (edgeDecoration < 0) 0 else edgeDecoration
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        when (parent.layoutManager) {
            //先判断GridLayoutManager,因为GridLayoutManager继承于LinearLayoutManager
            is GridLayoutManager -> setGridItemOffset(outRect, view, parent)
            is LinearLayoutManager -> setLinearItemOffset(outRect, view, parent)
        }

    }

    /**
     * LinearLayoutManager
     */
    private fun setLinearItemOffset(outRect: Rect, view: View, parent: RecyclerView) {
        (parent.layoutManager as LinearLayoutManager).run {
            val position = parent.getChildAdapterPosition(view)
            val viewType = getItemViewType(view)
            if (linearItemOffsetsListener != null && linearItemOffsetsListener!!.setItemOffsets(
                    position,
                    viewType
                )
            ) {
                linearItemOffsetsListener?.itemOffsets(position, viewType)?.run {
                    outRect.left = left
                    outRect.right = right
                    outRect.top = top
                    outRect.bottom = right
                }
            } else {
                when (orientation) {
                    RecyclerView.HORIZONTAL -> {//水平
                        outRect.left =
                            if (position == 0) edgeDecoration else verticalDecoration
                        outRect.top = 0
                        outRect.bottom = 0
                        outRect.right =
                            if (position + 1 == itemCount) edgeDecoration else 0
                    }
                    else -> {//竖直
                        outRect.top = if (position == 0) edgeDecoration else horizontalDecoration
                        outRect.bottom =
                            if (position + 1 == itemCount) edgeDecoration else 0
                        outRect.left = edgeDecoration
                        outRect.right = edgeDecoration
                    }
                }
            }
        }
    }

    /**
     * GridLayoutManager
     */
    private fun setGridItemOffset(outRect: Rect, view: View, parent: RecyclerView) {
        (parent.layoutManager as GridLayoutManager).run {
            //列表第几个
            val position = parent.getChildAdapterPosition(view)
            //占用宽度跨度
            val spanSize = spanSizeLookup.getSpanSize(position)
            //在第几行
            val row = spanSizeLookup.getSpanGroupIndex(position, spanCount)
            //最大行序号
            val maxRow = spanSizeLookup.getSpanGroupIndex(itemCount - 1, spanCount)
            //行内第几列开始
            val span = spanSizeLookup.getSpanIndex(position, spanCount)
            val viewType = getItemViewType(view)
            //如果没有设置监听器，或者设置监听器但返回false，表示开发者不另行自定义设置outRect
            if (gridItemOffsetsListener != null && gridItemOffsetsListener!!.setItemOffsets(
                    position,
                    viewType
                )
            ) {
                gridItemOffsetsListener!!.itemOffsets(
                    position, spanSize, span, spanCount,
                    row, maxRow, viewType
                ).run {
                    outRect.left = left
                    outRect.right = right
                    outRect.top = top
                    outRect.bottom = right
                }
            } else {
                // includeEdge=true   绘制边界
                outRect.top = when (row) {
                    //第一行
                    0 -> edgeDecoration
                    //中间
                    else -> horizontalDecoration
                }
                outRect.left = when (span) {
                    //第一列
                    0 -> edgeDecoration
                    else -> verticalDecoration
                }
                outRect.right = when {
                    (span + spanSize) >= spanCount -> edgeDecoration
                    else -> 0
                }
                outRect.bottom = when (row) {
                    //最后一行
                    maxRow -> edgeDecoration
                    else -> 0
                }
            }
        }
    }


    /**
     * GridLayoutManager
     */
    interface GridItemOffsetsListener {

        fun setItemOffsets(position: Int, viewType: Int): Boolean

        fun itemOffsets(
            position: Int,
            spanSize: Int,
            span: Int,
            spanCount: Int,
            row: Int,
            maxRow: Int,
            viewType: Int
        ): Rect
    }

    interface LinearItemOffsetsListener {

        fun setItemOffsets(position: Int, viewType: Int): Boolean

        fun itemOffsets(position: Int, viewType: Int): Rect
    }

}