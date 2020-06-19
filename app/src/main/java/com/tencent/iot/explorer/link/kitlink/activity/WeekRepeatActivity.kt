package com.tencent.iot.explorer.link.kitlink.activity

import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.entity.WeekRepeatEntity
import com.tencent.iot.explorer.link.kitlink.holder.WeekRepeatHolder
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_week_repeat.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 重复设置
 */
class WeekRepeatActivity : BaseActivity(), CRecyclerView.RecyclerItemView {

    companion object {
        var days = "0000000"
    }

    private val list = arrayListOf<WeekRepeatEntity>()

    override fun getContentView(): Int {
        return R.layout.activity_week_repeat
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = "重复"
        for (i in days.indices) {
            val entity = WeekRepeatEntity()
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
        crv_week_repeat.setList(list)
        crv_week_repeat.addRecyclerItemView(this)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        when (position) {
            -1 -> save()
            else -> selected(position)
        }
    }

    /**
     * 选择
     */
    private fun selected(position: Int) {
        list[position].run {
            value = (value + 1) % 2
        }
        crv_week_repeat.notifyDataChanged()
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
        setResult(200)
        finish()
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return WeekRepeatHolder(this, parent, R.layout.item_week_repeat)
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

}
