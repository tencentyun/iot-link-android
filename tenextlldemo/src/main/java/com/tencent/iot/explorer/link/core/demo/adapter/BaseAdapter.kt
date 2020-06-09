package com.tenext.demo.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tenext.demo.holder.BaseHolder

/**
 * 列表Adapter,不允许有空的元素，否则会不显示
 */
abstract class BaseAdapter(context: Context, list: List<Any>) :
    RecyclerView.Adapter<BaseHolder<*>>() {

    private val mList = list
    private var itemListener: OnItemListener? = null
    val mContext = context

    abstract fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*>


    fun setOnItemListener(onItemListener: OnItemListener) {
        itemListener = onItemListener
    }

    /**
     * 调用点击
     */
    fun onClickItem(holder: BaseHolder<*>, clickView: View, position: Int) {
        itemListener?.onItemClick(holder, clickView, position)
    }

    /**
     * 列表长度
     */
    override fun getItemCount(): Int {
        return mList.size
    }

    /**
     * 创建ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        val holder = createHolder(parent, viewType)
        holder.setAdapter(this)
        return holder
    }

    /**
     * 显示
     */
    override fun onBindViewHolder(holder: BaseHolder<*>, position: Int) {
        //不为空才会显示
        if (position < mList.size && holder.parseData(data(position)))
            holder.show(holder, position)
    }

    fun data(position: Int): Any {
        return mList[position]
    }

}

interface OnItemListener {
    fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int)
}


