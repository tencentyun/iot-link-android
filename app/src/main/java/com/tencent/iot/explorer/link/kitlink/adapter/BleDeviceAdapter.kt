package com.tencent.iot.explorer.link.kitlink.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.link.entity.BleDevice
import com.tencent.iot.explorer.link.core.link.entity.BleDeviceInfo
import java.util.*

class BleDeviceAdapter(list: MutableList<BleDevice>) : RecyclerView.Adapter<BleDeviceAdapter.ViewHolder>() {
    var list: MutableList<BleDevice> = LinkedList()
    var scaningTxt: TextView? = null
    var titleTxt: TextView? = null
        set(value) {
            field = value
            field?.visibility = View.GONE
        }

    init {
        this.list = list
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var image: ImageView = layoutView.findViewById(R.id.iv_device_icon)
        var textDev: TextView = layoutView.findViewById(R.id.tv_device_name)
        var textProduct: TextView = layoutView.findViewById(R.id.tv_product_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ble_device, parent, false)
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
        if (TextUtils.isEmpty(list[position]?.url)) {
            Picasso.get().load(R.mipmap.device_placeholder).into(holder.image)
        } else {
            Picasso.get().load(list[position]?.url).into(holder.image)
        }
        holder.textDev.text = list.get(position)?.devName
        holder.textProduct.text = list.get(position)?.productName

        if (list.size > 0) {
            titleTxt?.visibility = View.VISIBLE
            scaningTxt?.setText(R.string.keep_scanning_device)
        } else {
            titleTxt?.visibility = View.GONE
            scaningTxt?.setText(R.string.scanning_device)
        }
    }

    override fun getItemCount(): Int {
        if (list == null) {
            return 0
        }
        return list.size
    }

    interface OnItemClicked {
        fun onItemClicked(pos: Int, dev: BleDevice)
    }

    private var onItemClicked: OnItemClicked? = null

    fun setOnItemClicked(onItemClicked: OnItemClicked?) {
        this.onItemClicked = onItemClicked
    }
}