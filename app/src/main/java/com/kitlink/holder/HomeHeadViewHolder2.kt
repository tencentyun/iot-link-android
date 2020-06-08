package com.kitlink.holder

import android.view.View
import com.kitlink.App
import com.kitlink.entity.FamilyEntity
import com.kitlink.entity.WeatherEntity
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.head_home1.view.*
import kotlinx.android.synthetic.main.head_home2.view.*

/**
 * 显示添加设备
 */
class HomeHeadViewHolder2 : CRecyclerView.HeadViewHolder<FamilyEntity> {
    var weatherEntity: WeatherEntity? = null

    constructor(itemView: View) : super(itemView)

    override fun show() {
        weatherEntity?.let {
            itemView.tv_weather_number.text = it.number
            itemView.tv_weather.text = it.weather
        }
        App.data.getCurrentFamily().let {
            itemView.tv_home_name.text = it.FamilyName
        }
        itemView.iv_more.visibility = View.GONE
        itemView.iv_audio.visibility = View.GONE

        itemView.iv_no_device_add.setOnClickListener {
            headListener?.doAction(this, itemView.iv_no_device_add, 0)
        }
        itemView.iv_audio.setOnClickListener {
            headListener?.doAction(this, itemView.iv_audio, 1)
        }
        itemView.tv_home_name.setOnClickListener {
            headListener?.doAction(
                this@HomeHeadViewHolder2,
                it,
                2
            )
        }
        itemView.iv_home_name.setOnClickListener {
            headListener?.doAction(
                this@HomeHeadViewHolder2,
                it,
                2
            )
        }
    }
}