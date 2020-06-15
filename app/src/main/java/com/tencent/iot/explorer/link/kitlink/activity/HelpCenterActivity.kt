package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.PropertyListParser
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.holder.HelpViewHolder

import com.view.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_help_center.*
import kotlinx.android.synthetic.main.menu_back_layout.*


class HelpCenterActivity : BaseActivity(), CRecyclerView.RecyclerItemView {

    private var askList = arrayListOf<String>()
    private var answerList = arrayListOf<String>()

    override fun getContentView(): Int {
        return R.layout.activity_help_center
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.help_center)
        parseList()
        crv_help.setList(askList)
        crv_help.addRecyclerItemView(this)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    private fun parseList() {
        val nsDictionary =
            PropertyListParser.parse(assets.open("ask.plist")) as? NSDictionary
        nsDictionary?.let {
            (it["ask"] as? NSArray)?.run {
                array.forEachIndexed { _, nsObject ->
                    askList.add(nsObject.toString())
                }
            }
            (it["answer"] as? NSArray)?.run {
                array.forEachIndexed { _, nsObject ->
                    answerList.add(nsObject.toString())
                }

            }
        }
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return HelpViewHolder(
            LayoutInflater.from(this)
                .inflate(R.layout.item_help, parent, false)
        )
    }

    override fun doAction(viewHolder: CRecyclerView.CViewHolder<*>, clickView: View, position: Int) {
        val intent = Intent(this, WebActivity::class.java)
        intent.putExtra("title", getString(R.string.help_center))
        intent.putExtra("text", answerList[position])
        startActivity(intent)
    }
}
