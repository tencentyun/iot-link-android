package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.TextUtils
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.kitlink.adapter.SelectPicAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCustomCallBack
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
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
        picUrl = intent.getStringExtra(CommonField.EXTRA_PIC_URL)
        val layoutManager = GridLayoutManager(this, 3)
        gv_pic.setLayoutManager(layoutManager)
        adapter?.setOnItemClicked(onItemClicked)
        gv_pic.setAdapter(adapter)

        HttpRequest.instance.getTaskPicList(CommonField.TASK_PIC_LIST_URL, loadPicUrlListener, RequestCode.get_tasl_pic_list)
    }

    var loadPicUrlListener = object: MyCustomCallBack {
        override fun fail(msg: String?, reqCode: Int) {
            T.show(msg)
        }

        override fun success(str: String, reqCode: Int) {
            var json = convertArr(str)
            if (json != null && json.size > 0) {
                for(i in 0 until json.size) {
                    listUrl.add(json.getString(i))
                }
            }
            adapter?.index = 0
            for (i in 0 until listUrl.size) {
                if (listUrl.get(i) == picUrl) {
                    adapter?.index = i
                }
            }
            adapter?.notifyDataSetChanged()
            picUrl = listUrl.get(adapter!!.index)
        }
    }

    private fun convertArr(str: String) :JSONArray {
        var arr = JSONArray()
        val start = str.indexOf('[')
        val end = str.indexOf(']')
        val urlArray = str.substring(start, end + 1)
        if (!TextUtils.isEmpty(urlArray)) {
            arr = JSONArray.parseArray(urlArray)
        }
        return arr
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