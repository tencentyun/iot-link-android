package com.tencent.iot.explorer.link.core.demo.video.adapter

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.video.entity.ActionRecord
import com.tencent.iot.explorer.link.core.demo.video.utils.PicassoTrustAll

class ActionListAdapter(context: Context?, list: MutableList<ActionRecord>) : RecyclerView.Adapter<ActionListAdapter.ViewHolder>() {
    var list: MutableList<ActionRecord> = ArrayList()
    var index = -1
    var context: Context? = null

    init {
        this.list = list
        this.context = context
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var time: TextView
        var actionTv: TextView
        var action: ImageView
        var snapshot: ImageView
        var play: ImageView

        init {
            time = layoutView.findViewById(R.id.tv_time)
            actionTv = layoutView.findViewById(R.id.tv_action)
            action = layoutView.findViewById(R.id.iv_action)
            snapshot = layoutView.findViewById(R.id.iv_video_snapshot)
            play = layoutView.findViewById(R.id.iv_play)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record_action, parent, false)
        val holder = ViewHolder(view)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.time.text = list.get(position).time
        holder.action.visibility = View.GONE
        holder.actionTv.text = list.get(position).action
        if (!TextUtils.isEmpty(list.get(position).snapshotUrl)) {
            PicassoTrustAll.getInstance(context).load(list.get(position).snapshotUrl).fit().into(holder.snapshot);
        } else {
            Picasso.get().load(R.color.black).into(holder.snapshot)
        }

        if (index == position) {
            holder.play.visibility = View.GONE
        } else {
            holder.play.visibility = View.VISIBLE
        }

        holder.snapshot.setOnClickListener {
            onItemClicked?.onItemVideoClicked(position)
        }
    }

    override fun getItemCount(): Int {
        if (list == null) {
            return 0
        }
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