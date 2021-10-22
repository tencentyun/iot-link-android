package com.tencent.iot.explorer.link.kitlink.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.entity.PermissionAccessInfo

class PermissionsAdapter(list: MutableList<PermissionAccessInfo>) : RecyclerView.Adapter<PermissionsAdapter.ViewHolder>() {
    var list: MutableList<PermissionAccessInfo> = ArrayList()

    init {
        this.list = list
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var permissionName: TextView = layoutView.findViewById(R.id.tv_permission)
        var permissionSwitch: Switch = layoutView.findViewById(R.id.switch_permission)
        var swicth: View = layoutView.findViewById(R.id.v_real_switch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_permission_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.permissionName.setText(list.get(position)?.permissionName)
        holder.permissionSwitch.isClickable = false
        holder.permissionSwitch.isChecked = list.get(position)?.permissionAccessed
        holder.swicth.setOnClickListener {
            onItemActionListener?.onItemSwitched(position, list.get(position))
        }
    }

    override fun getItemCount(): Int {
        if (list == null) {
            return 0
        }
        return list.size
    }

    interface OnItemActionListener {
        fun onItemSwitched(pos: Int, permissionAccessInfo: PermissionAccessInfo)
    }

    private var onItemActionListener: OnItemActionListener? = null

    fun setOnItemClicked(onItemActionListener: OnItemActionListener?) {
        this.onItemActionListener = onItemActionListener
    }
}