package com.tencent.iot.explorer.link.kitlink.holder

import android.view.View
import com.tencent.iot.explorer.link.kitlink.App
import com.tencent.iot.explorer.link.kitlink.entity.FamilyEntity
import com.tencent.iot.explorer.link.kitlink.entity.WeatherEntity
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.head_home1.view.*

/**
 * 天气显示
 */
class HomeHeadViewHolder1 : CRecyclerView.HeadViewHolder<FamilyEntity> {

    var weatherEntity: WeatherEntity? = null

    constructor(itemView: View) : super(itemView)

    override fun show() {
        weatherEntity?.let {
            itemView.tv_weather_number.text = it.number
            itemView.tv_weather.text = it.weather
        }
        App.data.getCurrentFamily()?.let {
            itemView.tv_home_name.text = it.FamilyName
        }
        itemView.run {
            iv_more.visibility = View.VISIBLE
            iv_audio.visibility = View.GONE
            iv_more.setOnClickListener {
                headListener?.doAction(
                    this@HomeHeadViewHolder1,
                    iv_more,
                    0
                )
            }
            iv_audio.setOnClickListener {
                headListener?.doAction(
                    this@HomeHeadViewHolder1,
                    iv_audio,
                    1
                )
            }
            tv_home_name.setOnClickListener {
                headListener?.doAction(
                    this@HomeHeadViewHolder1,
                    it,
                    2
                )
            }
            iv_home_name.setOnClickListener {
                headListener?.doAction(
                    this@HomeHeadViewHolder1,
                    it,
                    2
                )
            }
        }
    }

}