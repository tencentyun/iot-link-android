package com.tencent.iot.explorer.link.demo.video

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.databinding.ItemVideoListDevBinding

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

    class ViewHolder(val binding: ItemVideoListDevBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVideoListDevBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)

        binding.root.setOnClickListener {
            if (showCheck) return@setOnClickListener
            val position = holder.adapterPosition
            if (position < list.size && position >= 0) {
                onItemClicked?.onItemClicked(position, list[position])
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            tvDevName.setText(list.get(position).DeviceName)

            videoProductInfo?.let {
                when(it.DeviceType ) {
                    VideoProductInfo.DEV_TYPE_IPC -> {
                        Picasso.get().load(R.mipmap.ipc).into(ivDev)
                    }
                    VideoProductInfo.DEV_TYPE_NVR -> {
                        Picasso.get().load(R.mipmap.nvr).into(ivDev)
                    }
                }
            }

            ivSelect.visibility = View.GONE
            tipText?.visibility = View.GONE
            if (showCheck) {
                ivSelect.visibility = View.VISIBLE
                tipText?.visibility = View.VISIBLE
                tipText?.setText(context?.getString(R.string.devs_checked, checkedIds?.size))
            }

            if (list.get(position)?.Online == 1) {
                itemLayoutBackground.visibility = View.GONE
                tvDevStatus.setText(R.string.online)
                context?.let {
                    tvDevStatus.setTextColor(it.resources.getColor(R.color.green_29CC85))
                    tvDevName.setTextColor(it.resources.getColor(R.color.black_15161A))
                }

            } else {
                itemLayoutBackground.visibility = View.VISIBLE
                tvDevStatus.setText(R.string.offline)
                context?.let {
                    tvDevStatus.setTextColor(it.resources.getColor(R.color.gray_C2C5CC))
                    tvDevName.setTextColor(it.resources.getColor(R.color.gray_C2C5CC))
                }
            }

            if (checkedIds.contains(position)) {
                ivSelect.setImageResource(R.mipmap.selected)
            } else {
                ivSelect.setImageResource(R.mipmap.unchecked)
            }

            ivMore.visibility = View.VISIBLE
            ivSelect.setOnClickListener {
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
                this@DevsAdapter.notifyDataSetChanged()
            }
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