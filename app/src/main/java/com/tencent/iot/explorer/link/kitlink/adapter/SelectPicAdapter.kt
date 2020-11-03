package com.tencent.iot.explorer.link.kitlink.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import kotlinx.android.synthetic.main.activity_complete_task_info.*
import java.util.*

class SelectPicAdapter(list: MutableList<String>) : RecyclerView.Adapter<SelectPicAdapter.ViewHolder>() {
    var list: MutableList<String> = LinkedList()
    var index = -1;

    init {
        this.list = list
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var pic: ImageView
        var picBackground: ImageView

        init {
            pic = layoutView.findViewById(R.id.iv_pic)
            picBackground = layoutView.findViewById(R.id.iv_pic_background)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_pic, parent, false)
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
        Picasso.get().load(list.get(position)).into(holder.pic)
        if (position == index) {
            holder.picBackground.visibility = View.VISIBLE
        } else {
            holder.picBackground.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        if (list == null) {
            return 0
        }
        return list.size
    }

    interface OnItemClicked {
        fun onItemClicked(pos: Int, url:String)
    }

    private var onItemClicked: OnItemClicked? = null

    fun setOnItemClicked(onItemClicked: OnItemClicked?) {
        this.onItemClicked = onItemClicked
    }
}