package com.tencent.iot.explorer.link.demo.video.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.video.entity.DevInfo
import com.tencent.iot.explorer.link.demo.video.entity.VideoProductInfo
import kotlinx.android.synthetic.main.fragment_video_device.*

class DevsAdapter(context: Context, list: MutableList<DevInfo>) : RecyclerView.Adapter<DevsAdapter.ViewHolder>() {
    private var ITEM_MAX_NUM = 4
    var list: MutableList<DevInfo> = ArrayList()
    var context: Context? = null
    var showCheck = false
    var checkedIds : MutableList<Int> = ArrayList()
    var maxNum = ITEM_MAX_NUM // 选择子项目的上限， <= 0 没有上限， > 0 上限生效
    var videoProductInfo : VideoProductInfo? = null
    var tipText: TextView? = null
    var radioComplete: RadioButton? = null
    var radioEdit: RadioButton? = null

    init {
        this.list = list
        this.context = context
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var devName: TextView
        var statusTv: TextView
        var devImg: ImageView
        var more: ImageView
        var checked: ImageView
        var backgroundLayout: ConstraintLayout

        init {
            devName = layoutView.findViewById(R.id.tv_dev_name)
            devImg = layoutView.findViewById(R.id.iv_dev)
            statusTv = layoutView.findViewById(R.id.tv_dev_status)
            more = layoutView.findViewById(R.id.iv_more)
            checked = layoutView.findViewById(R.id.iv_select)
            backgroundLayout = layoutView.findViewById(R.id.item_layout_background)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_list_dev, parent, false)
        val holder = ViewHolder(view)
        view.setOnClickListener {
            if (showCheck) return@setOnClickListener
            val position = holder.adapterPosition
            if (position < list.size && position >= 0) {
                onItemClicked?.onItemClicked(position, list[position])
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.devName.setText(list.get(position)?.deviceName)

        videoProductInfo?.let {
            when(it.DeviceType ) {
                VideoProductInfo.DEV_TYPE_IPC -> {
                    Picasso.get().load(R.mipmap.ipc).into(holder.devImg)
                }
                VideoProductInfo.DEV_TYPE_NVR -> {
                    Picasso.get().load(R.mipmap.nvr).into(holder.devImg)
                }
            }
        }

        holder.checked.visibility = View.GONE
        tipText?.visibility = View.GONE
        if (showCheck) {
            holder.checked.visibility = View.VISIBLE
            tipText?.visibility = View.VISIBLE
            tipText?.setText(context?.getString(R.string.devs_checked, checkedIds?.size))
        }

        if (list.get(position)?.Status == 1) {
            holder.backgroundLayout.visibility = View.GONE
            holder.statusTv.setText(R.string.online)
            context?.let {
                holder.statusTv.setTextColor(it.resources.getColor(R.color.green_29CC85))
                holder.devName.setTextColor(it.resources.getColor(R.color.black_15161A))
            }

        } else {
            holder.backgroundLayout.visibility = View.VISIBLE
            holder.statusTv.setText(R.string.offline)
            context?.let {
                holder.statusTv.setTextColor(it.resources.getColor(R.color.gray_C2C5CC))
                holder.devName.setTextColor(it.resources.getColor(R.color.gray_C2C5CC))
            }
        }

        if (checkedIds.contains(position)) {
            holder.checked.setImageResource(R.mipmap.selected)
        } else {
            holder.checked.setImageResource(R.mipmap.unchecked)
        }

        holder.more.visibility = View.VISIBLE
        holder.checked.setOnClickListener {
            if (checkedIds.contains(position)) {
                checkedIds.remove(position)
                onItemClicked?.onItemCheckedClicked(position, false)
            } else {
                if (maxNum > 0 && checkedIds.size >= maxNum) {
                    onItemClicked?.onItemCheckedLimited()
                    return@setOnClickListener
                }
                checkedIds.add(position)
                onItemClicked?.onItemCheckedClicked(position, true)
            }
            this.notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        if (list == null) return 0
        return list.size
    }

    interface OnItemClicked {
        fun onItemClicked(pos: Int, dev: DevInfo)
        fun onItemMoreClicked(pos: Int, dev: DevInfo)
        fun onItemCheckedClicked(pos: Int, checked : Boolean)
        fun onItemCheckedLimited()
    }

    private var onItemClicked: OnItemClicked? = null

    fun setOnItemClicked(onItemClicked: OnItemClicked?) {
        this.onItemClicked = onItemClicked
    }

    fun switchBtnStatus(status: Boolean) {
        radioComplete?.visibility = if (status) View.VISIBLE else View.GONE
        radioEdit?.visibility = if (status) View.GONE else View.VISIBLE
        showCheck = status
        if (showCheck) checkedIds.clear()
        this.notifyDataSetChanged()
    }
}