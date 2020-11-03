package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.adapter.ManualTaskAdapter
import com.tencent.iot.explorer.link.kitlink.adapter.SelectPicAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import kotlinx.android.synthetic.main.activity_add_manual_task.*
import kotlinx.android.synthetic.main.activity_delay_time.*
import kotlinx.android.synthetic.main.activity_select_smart_pic.*
import kotlinx.android.synthetic.main.menu_back_layout.*


class SelectTaskPicActivity : BaseActivity() {
    private var listUrl = ArrayList<String>()
    private var adapter: SelectPicAdapter? = SelectPicAdapter(listUrl)
    @Volatile
    private var picUrl = ""

    override fun getContentView(): Int {
        return R.layout.activity_select_smart_pic
    }

    override fun initView() {
        tv_title.setText(R.string.select_smart_pic)
        val layoutManager = GridLayoutManager(this, 3)
        gv_pic.setLayoutManager(layoutManager)
        adapter?.setOnItemClicked(onItemClicked)
        gv_pic.setAdapter(adapter)
        listUrl.add("https://main.qcloudimg.com/raw/c05e0ef33ff62962a089649800cd5ce9/scene1.jpg")
        listUrl.add("https://main.qcloudimg.com/raw/c05e0ef33ff62962a089649800cd5ce9/scene1.jpg")
        listUrl.add("https://main.qcloudimg.com/raw/c05e0ef33ff62962a089649800cd5ce9/scene1.jpg")
        listUrl.add("https://main.qcloudimg.com/raw/c05e0ef33ff62962a089649800cd5ce9/scene1.jpg")
        listUrl.add("https://main.qcloudimg.com/raw/c05e0ef33ff62962a089649800cd5ce9/scene1.jpg")
        listUrl.add("https://main.qcloudimg.com/raw/c05e0ef33ff62962a089649800cd5ce9/scene1.jpg")
        listUrl.add("https://main.qcloudimg.com/raw/c05e0ef33ff62962a089649800cd5ce9/scene1.jpg")
        listUrl.add("https://main.qcloudimg.com/raw/c05e0ef33ff62962a089649800cd5ce9/scene1.jpg")
        listUrl.add("https://main.qcloudimg.com/raw/c05e0ef33ff62962a089649800cd5ce9/scene1.jpg")
        listUrl.add("https://main.qcloudimg.com/raw/c05e0ef33ff62962a089649800cd5ce9/scene1.jpg")
        listUrl.add("https://main.qcloudimg.com/raw/c05e0ef33ff62962a089649800cd5ce9/scene1.jpg")

    }

    var onItemClicked = object: SelectPicAdapter.OnItemClicked{
        override fun onItemClicked(pos: Int, url: String) {
            picUrl = url
            adapter?.index = pos
            adapter?.notifyDataSetChanged()
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_save.setOnClickListener {
            val intent = Intent()
            intent.putExtra(CommonField.EXTRA_PIC_URL, picUrl)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}