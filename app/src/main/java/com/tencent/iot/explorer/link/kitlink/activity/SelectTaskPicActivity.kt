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
    val array = arrayOf("https://main.qcloudimg.com/raw/c05e0ef33ff62962a089649800cd5ce9/scene1.jpg",
        "https://main.qcloudimg.com/raw/a699919a2d7df048757facf781f9449e/scene2.jpg",
        "https://main.qcloudimg.com/raw/41a727f20f200a1100c6e5cf7ac40088/scene3.jpg",
        "https://main.qcloudimg.com/raw/493cd8e417bb990c3662f5689bf32074/scene4.jpg",
        "https://main.qcloudimg.com/raw/a383821b3bf8eab99ccc4e51935bbf95/scene5.jpg",
        "https://main.qcloudimg.com/raw/9c04afe82f2d18448efa45e239ee1244/scene6.jpg",
        "https://main.qcloudimg.com/raw/4aa0aff9c1f0f67df0d0ad7e906d736a/scene7.jpg",
        "https://main.qcloudimg.com/raw/0a5e2254ef293ef32a4749e77c4e73fa/scene8.jpg")

    private var listUrl = ArrayList<String>()
    private var adapter: SelectPicAdapter? = SelectPicAdapter(listUrl)
    @Volatile
    private var picUrl = ""

    override fun getContentView(): Int {
        return R.layout.activity_select_smart_pic
    }

    override fun initView() {
        tv_title.setText(R.string.select_smart_pic)
        picUrl = intent.getStringExtra(CommonField.EXTRA_PIC_URL)
        val layoutManager = GridLayoutManager(this, 3)
        gv_pic.setLayoutManager(layoutManager)
        adapter?.setOnItemClicked(onItemClicked)
        gv_pic.setAdapter(adapter)
        listUrl.addAll(array)
        adapter?.index = 0
        for (i in 0 until listUrl.size) {
            if (listUrl.get(i) == picUrl) {
                adapter?.index = i
            }
        }
        adapter?.notifyDataSetChanged()
        picUrl = listUrl.get(adapter!!.index)
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