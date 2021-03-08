package com.tencent.iot.explorer.link.kitlink.adapter

import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSON
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.entity.ProductUIDevShortCutConfig
import com.tencent.iot.explorer.link.customview.dialog.KeyBooleanValue
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class RoomDevAdapter(list: MutableList<DeviceEntity>) : RecyclerView.Adapter<RoomDevAdapter.ViewHolder>() {
    var list: MutableList<DeviceEntity> = ArrayList()
    var shortCuts: MutableMap<String, ProductUIDevShortCutConfig> = ConcurrentHashMap()

    init {
        this.list = list
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var devName: TextView
        var devImg: ImageView
        var status: ImageView
        var statusSrc: ImageView
        var more: ImageView
        var backgroundLayout: ConstraintLayout

        init {
            devName = layoutView.findViewById(R.id.tv_dev_name)
            devImg = layoutView.findViewById(R.id.iv_dev)
            status = layoutView.findViewById(R.id.iv_dev_status)
            more = layoutView.findViewById(R.id.iv_more)
            backgroundLayout = layoutView.findViewById(R.id.item_layout_background)
            statusSrc = layoutView.findViewById(R.id.iv_dev_status_src)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_dev, parent, false)
        val holder = ViewHolder(view)
        view.setOnClickListener {
            val position = holder.adapterPosition
            if (onItemClicked != null) {
                onItemClicked!!.onItemClicked(position, list[position])
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.devName.setText(list.get(position)?.AliasName)
        if (TextUtils.isEmpty(list.get(position)?.IconUrl)) {
            Picasso.get().load(R.drawable.device_placeholder).into(holder.devImg)
        } else {
            Picasso.get().load(list.get(position)?.IconUrl).into(holder.devImg)
        }
        if (list.get(position)?.online == 1) {
            holder.backgroundLayout.visibility = View.GONE
        } else {
            holder.backgroundLayout.visibility = View.VISIBLE
        }

        holder.status.visibility = View.INVISIBLE
        holder.more.visibility = View.INVISIBLE
        var shortCut = shortCuts.get(list.get(position).ProductId)
        if (shortCuts != null) {
            if (shortCut != null && !TextUtils.isEmpty(shortCut.powerSwitch)) {
                holder.status.visibility = View.VISIBLE
                holder.statusSrc.visibility = View.VISIBLE
                holder.status.isClickable = true
                if (checkStatus(position, shortCut)) {
                    holder.statusSrc.setImageResource(R.mipmap.dev_switch_status_on)
                } else {
                    holder.statusSrc.setImageResource(R.mipmap.dev_switch_status_off)
                }
            } else {
                holder.status.visibility = View.INVISIBLE
                holder.statusSrc.visibility = View.INVISIBLE
                holder.status.isClickable = false
            }

            if (shortCut != null && shortCut.shortcut != null && shortCut.shortcut.size > 0) {
                holder.more.visibility = View.VISIBLE
                holder.more.isClickable = true
            } else {
                holder.more.visibility = View.INVISIBLE
                holder.more.isClickable = false
            }
        }

        holder.more.setOnClickListener {
            if (onItemClicked != null && list.get(position)?.online == 1) {
                onItemClicked!!.onMoreClicked(position, list[position])
            }
        }
        holder.status.setOnClickListener {
            if (onItemClicked != null && list.get(position)?.online == 1) {
                onItemClicked!!.onSwitchClicked(position, list[position], shortCut)
            }
        }
    }

    private fun checkStatus(position: Int, shortCut: ProductUIDevShortCutConfig):Boolean {
        if (list[position].deviceDataList != null) {
            for (deviceDataEntity in list[position].deviceDataList) {
                if (deviceDataEntity.id == shortCut?.powerSwitch && deviceDataEntity.value == "1") {
                    return true
                }
            }
        }
        return false
    }

    override fun getItemCount(): Int {
        if (list == null) {
            return 0
        }
        return list.size
    }

    interface OnItemClicked {
        fun onItemClicked(pos: Int, dev: DeviceEntity)
        fun onSwitchClicked(pos: Int, dev: DeviceEntity, shortCut: ProductUIDevShortCutConfig?)
        fun onMoreClicked(pos: Int, dev: DeviceEntity)
    }

    private var onItemClicked: OnItemClicked? = null

    fun setOnItemClicked(onItemClicked: OnItemClicked?) {
        this.onItemClicked = onItemClicked
    }
}