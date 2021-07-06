package com.tencent.iot.explorer.link.kitlink.adapter.touch

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


class TaskItemTouchHelper : ItemTouchHelper.Callback() {
    var sort = true
    var touchListener: OnItemTouchListener? = null

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val swipeFlags = ItemTouchHelper.START
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        touchListener?.onMove(fromPosition, toPosition)
        return true
    }


    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun isLongPressDragEnabled(): Boolean {
        return sort
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}