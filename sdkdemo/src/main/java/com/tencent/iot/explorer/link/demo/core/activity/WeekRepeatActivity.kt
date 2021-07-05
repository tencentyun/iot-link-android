package com.tencent.iot.explorer.link.demo.core.activity

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.demo.core.adapter.BaseAdapter
import com.tencent.iot.explorer.link.demo.core.entity.TimingProject
import com.tencent.iot.explorer.link.demo.core.entity.WeekRepeat
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import kotlinx.android.synthetic.main.activity_week_repeat.*
import kotlinx.android.synthetic.main.item_week_repeat.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 重复设置
 */
class WeekRepeatActivity : BaseActivity() {

    private val list = arrayListOf<WeekRepeat>()
    private lateinit var days: String

    private val adapter = object : BaseAdapter(this, list) {
        override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
            return object :
                BaseHolder<WeekRepeat>(this@WeekRepeatActivity, parent, R.layout.item_week_repeat) {
                override fun show(holder: BaseHolder<*>, position: Int) {
                    data.run {
                        itemView.tv_week_repeat_title.text = text
                        itemView.iv_week_repeat_selected.setImageResource(
                            if (value == 1) R.mipmap.icon_checked
                            else R.mipmap.icon_unchecked
                        )
                        itemView.tv_week_repeat_commit.visibility =
                            if (position == 6) View.VISIBLE else View.GONE
                    }
                    itemView.tv_week_repeat_commit.setOnClickListener {
                        save()
                    }
                    itemView.tv_week_repeat_title.setOnClickListener {
                        selected(position)
                    }
                }
            }
        }
    }

    override fun getContentView(): Int {
        return R.layout.activity_week_repeat
    }

    override fun initView() {
        tv_title.text = "重复"
        days = get<TimingProject>("repeat")?.Days ?: "0000000"
        rv_week_repeat.layoutManager = LinearLayoutManager(this)
        rv_week_repeat.adapter = adapter
        for (i in 0 until days.length) {
            val entity = WeekRepeat()
            when (i) {
                0 -> entity.text = getString(R.string.every_sunday)
                1 -> entity.text = getString(R.string.every_monday)
                2 -> entity.text = getString(R.string.every_tuesday)
                3 -> entity.text = getString(R.string.every_wednesday)
                4 -> entity.text = getString(R.string.every_thursday)
                5 -> entity.text = getString(R.string.every_friday)
                6 -> entity.text = getString(R.string.every_saturday)
            }
            entity.value = days.substring(i, i + 1).toInt()
            list.add(entity)
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    /**
     * 选择
     */
    private fun selected(position: Int) {
        list[position].run {
            value = (value + 1) % 2
        }
        adapter.notifyDataSetChanged()
    }

    /**
     *  保存
     */
    private fun save() {
        val sb = StringBuilder()
        list.forEachIndexed { _, e ->
            sb.append(e.value)
        }
        days = sb.toString()
        get<TimingProject>("repeat")?.Days = days
        setResult(200)
        finish()
    }

}
