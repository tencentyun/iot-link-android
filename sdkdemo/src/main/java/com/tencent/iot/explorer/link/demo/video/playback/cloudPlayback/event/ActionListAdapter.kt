package com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.util.PicassoTrustAll
import com.tencent.iot.explorer.link.demo.databinding.ItemRecordActionBinding

class ActionListAdapter(context: Context?, list: MutableList<ActionRecord>) : RecyclerView.Adapter<ActionListAdapter.ViewHolder>() {
    var list: MutableList<ActionRecord> = ArrayList()
    var index = -1
    var context: Context? = null

    init {
        this.list = list
        this.context = context
    }

    class ViewHolder(val binding: ItemRecordActionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            tvTime.text = list.get(position).time
            ivAction.visibility = View.GONE
            tvAction.text = list.get(position).action

            if (!TextUtils.isEmpty(list.get(position).snapshotUrl)) {
                PicassoTrustAll.getInstance(context).load(list.get(position).snapshotUrl).fit().into(ivVideoSnapshot);
            } else {
                Picasso.get().load(R.color.black).into(ivVideoSnapshot)
            }

            if (index == position) {
                ivPlay.visibility = View.GONE
            } else {
                ivPlay.visibility = View.VISIBLE
            }

            ivVideoSnapshot.setOnClickListener {
                onItemClicked?.onItemVideoClicked(position)
            }
        }
    }

    override fun getItemCount(): Int {
        if (list == null) return 0
        return list.size
    }

    interface OnItemClicked {
        fun onItemVideoClicked(pos: Int)
    }

    private var onItemClicked: OnItemClicked? = null

    fun setOnItemClicked(onItemClicked: OnItemClicked?) {
        this.onItemClicked = onItemClicked
    }
}