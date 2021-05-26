package com.tencent.iot.explorer.link.core.demo.video.fragment

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.fragment.BaseFragment
import com.tencent.iot.explorer.link.core.demo.video.adapter.DevsAdapter
import com.tencent.iot.explorer.link.core.demo.video.entity.AccessInfo
import com.tencent.iot.explorer.link.core.demo.video.entity.DevInfo
import com.tencent.iot.video.link.callback.VideoCallback
import com.tencent.iot.video.link.service.VideoBaseService
import kotlinx.android.synthetic.main.fragment_video_device.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VideoDeviceFragment(accessInfo: AccessInfo?) : BaseFragment() {
    private var accessInfo : AccessInfo? = null
    private var devs : MutableList<DevInfo> = ArrayList()
    private var adapter : DevsAdapter? = null

    init {
        this.accessInfo = accessInfo
    }

    override fun getContentView(): Int {
        return R.layout.fragment_video_device
    }

    override fun startHere(view: View) {
        setListener()
        rg_edit_dev.check(radio_complete.id)
        var devGridLayoutManager = GridLayoutManager(context, 2)
        context?.let {
            adapter = DevsAdapter(it, devs)
            gv_devs.setLayoutManager(devGridLayoutManager)
            gv_devs.setAdapter(adapter)
            loadMoreVideoInfo()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setListener() {
        rg_edit_dev.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                radio_edit.id -> {
                    radio_edit.visibility = View.GONE
                    radio_complete.visibility = View.VISIBLE
                    adapter?.let {
                        it.showCheck = true
                        it.checkedIds.clear()
                        it.notifyDataSetChanged()
                    }
                }

                radio_complete.id -> {
                    radio_complete.visibility = View.GONE
                    radio_edit.visibility = View.VISIBLE
                    adapter?.let {
                        it.showCheck = false
                        it.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private var loadDevListener = object : VideoCallback {
        override fun fail(msg: String?, reqCode: Int) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        override fun success(response: String?, reqCode: Int) {
            val jsonObject = JSON.parse(response) as JSONObject
            val jsonResponset = jsonObject.getJSONObject("Response") as JSONObject

            if (jsonResponset.containsKey("Devices")) {
                val dataArray: JSONArray = jsonResponset.getJSONArray("Devices")
                for (i in 0 until dataArray.size) {
                    var dev = JSON.parseObject(dataArray.getString(i), DevInfo::class.java)
                    devs.add(dev)
                }
            }

            GlobalScope.launch (Dispatchers.Main) {
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun loadMoreVideoInfo() {
        devs.clear()
        accessInfo?.let {
            VideoBaseService(it.accessId, it.accessToken).getDeviceList(it.productId, 0, 99, loadDevListener)
        }
    }
}