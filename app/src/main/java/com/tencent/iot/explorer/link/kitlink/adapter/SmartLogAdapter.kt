package com.tencent.iot.explorer.link.kitlink.adapter

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.entity.LogMessage
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest.Companion.instance
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import java.util.*

class SmartLogAdapter(context: Context, list: MutableList<LogMessage>) : RecyclerView.Adapter<SmartLogAdapter.ViewHolder>() {
    var list: MutableList<LogMessage> = LinkedList()
    var index = -1;
    var context: Context? = null

    init {
        this.list = list
        this.context = context
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var allLayout: View
        var dateTipLayout: ConstraintLayout
        var runSuccessStaus: ImageView
        var day: TextView
        var mouth: TextView
        var taskName: TextView
        var taskDesc: TextView
        var moreBtn: ImageView
        var moreInfo: ConstraintLayout
        var failedDetail: ListView

        init {
            allLayout = layoutView
            dateTipLayout = layoutView.findViewById(R.id.title_layout)
            day = layoutView.findViewById(R.id.tv_day)
            mouth = layoutView.findViewById(R.id.tv_mouth)
            runSuccessStaus = layoutView.findViewById(R.id.iv_status)
            taskName = layoutView.findViewById(R.id.tv_task_name)
            taskDesc = layoutView.findViewById(R.id.tv_run_desc)
            moreBtn = layoutView.findViewById(R.id.iv_more)
            moreInfo = layoutView.findViewById(R.id.more_layout)
            failedDetail = layoutView.findViewById(R.id.lv_failed_detail)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_run_task_log, parent, false)
        val holder = ViewHolder(view)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (position == 0) {
            holder.dateTipLayout.visibility = View.VISIBLE
        } else if (list.get(position).mouth == list.get(position - 1).mouth &&
                list.get(position).day == list.get(position - 1).day) {
            holder.dateTipLayout.visibility = View.GONE
        } else {
            holder.dateTipLayout.visibility = View.VISIBLE
        }
        holder.mouth.setText(list.get(position).mouth + context?.getString(R.string.unit_mouth))
        holder.day.setText(list.get(position).day)

        if (list.get(position).resultCode == 0) {
            holder.runSuccessStaus.setImageResource(R.mipmap.right)
            holder.moreBtn.visibility = View.GONE
            holder.moreInfo.visibility = View.GONE
        } else {
            holder.runSuccessStaus.setImageResource(R.mipmap.warning)
            holder.moreBtn.visibility = View.VISIBLE
            holder.moreInfo.visibility = View.GONE
        }

        if (TextUtils.isEmpty(list.get(position).automationName)) {
            holder.taskName.setText(list.get(position).sceneName)
        } else {
            holder.taskName.setText(list.get(position).automationName)
        }

        holder.taskDesc.setText(list.get(position).time + " " + list.get(position).result)
        holder.failedDetail.adapter = ActionResultAdapter(context, list.get(position).actionResults)

        holder.allLayout.setOnClickListener{
            if (list.get(position).resultCode != 0 && holder.moreInfo.visibility == View.VISIBLE) {
                holder.moreBtn.rotation = -90F
                holder.moreInfo.visibility = View.GONE
                list.get(position).opened = false
            } else if (list.get(position).resultCode != 0 && holder.moreInfo.visibility == View.GONE) {
                holder.moreBtn.rotation = 90F
                holder.moreInfo.visibility = View.VISIBLE
                list.get(position).opened = true
            }
        }

        if (list.get(position).opened) {
            holder.moreBtn.rotation = 90F
            holder.moreInfo.visibility = View.VISIBLE
        } else {
            holder.moreInfo.visibility = View.GONE
            holder.moreBtn.rotation = -90F
        }
    }

    override fun getItemCount(): Int {
        if (list == null) {
            return 0
        }
        return list.size
    }

    interface OnItemClicked {
        fun onItemClicked(pos: Int, logMessage: LogMessage)
    }

    private var onItemClicked: OnItemClicked? = null

    fun setOnItemClicked(onItemClicked: OnItemClicked?) {
        this.onItemClicked = onItemClicked
    }
}