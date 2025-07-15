package com.tencent.iot.explorer.link.demo.core.activity

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.adapter.BaseAdapter
import com.tencent.iot.explorer.link.demo.core.entity.TimingProject
import com.tencent.iot.explorer.link.demo.core.entity.WeekRepeat
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.databinding.ActivityWeekRepeatBinding
import com.tencent.iot.explorer.link.demo.databinding.ItemWeekRepeatBinding

/**
 * 重复设置
 */
class WeekRepeatActivity : BaseActivity<ActivityWeekRepeatBinding>() {

    private val list = arrayListOf<WeekRepeat>()
    private lateinit var days: String

    private val adapter = object : BaseAdapter(this, list) {
        override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*, ItemWeekRepeatBinding> {
            val holderBinding = ItemWeekRepeatBinding.inflate(layoutInflater)

            return object :
                BaseHolder<WeekRepeat, ItemWeekRepeatBinding>(holderBinding) {
                override fun show(holder: BaseHolder<*, *>, position: Int) {
                    data.run {
                        holderBinding.tvWeekRepeatTitle.text = text
                        holderBinding.ivWeekRepeatSelected.setImageResource(
                            if (value == 1) R.mipmap.icon_checked
                            else R.mipmap.icon_unchecked
                        )
                        holderBinding.tvWeekRepeatCommit.visibility =
                            if (position == 6) View.VISIBLE else View.GONE
                    }
                    holderBinding.tvWeekRepeatCommit.setOnClickListener {
                        save()
                    }
                    holderBinding.tvWeekRepeatTitle.setOnClickListener {
                        selected(position)
                    }
                }
            }
        }
    }

    override fun getViewBinding(): ActivityWeekRepeatBinding = ActivityWeekRepeatBinding.inflate(layoutInflater)

    override fun initView() {
        binding.menuWeekRepeat.tvTitle.text = "重复"
        days = get<TimingProject>("repeat")?.Days ?: "0000000"
        binding.rvWeekRepeat.layoutManager = LinearLayoutManager(this)
        binding.rvWeekRepeat.adapter = adapter
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
        binding.menuWeekRepeat.ivBack.setOnClickListener { finish() }
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
