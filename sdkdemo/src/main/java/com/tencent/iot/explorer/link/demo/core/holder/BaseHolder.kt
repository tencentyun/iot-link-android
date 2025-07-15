package com.tencent.iot.explorer.link.demo.core.holder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.tencent.iot.explorer.link.demo.core.adapter.BaseAdapter

abstract class BaseHolder<T : Any, VB : ViewBinding>(protected val binding: VB)
    : RecyclerView.ViewHolder(binding.root) {

    internal lateinit var adapter: BaseAdapter

    lateinit var data: T

    fun setAdapter(adapter: BaseAdapter) {
        this.adapter = adapter
    }

    abstract fun show(holder: BaseHolder<*, *>, position: Int)

    fun parseData(any: Any): Boolean {
        (any as? T)?.run {
            data = this
            return true
        }
        return false
    }

    fun getColor(@ColorRes color: Int): Int {
        return adapter.mContext.resources.getColor(color)
    }

    /**
     * 点击回调
     */
    fun clickItem(holder: BaseHolder<*, *>, clickView: View, position: Int) {
        adapter.onClickItem(holder, clickView, position)
    }

    fun getContext(): Context {
        return adapter.mContext
    }

    fun getString(resId: Int): String {
        return adapter.mContext.getString(resId)
    }

}
