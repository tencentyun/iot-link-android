package com.tencent.iot.explorer.link.core.demo.video.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.fragment.BaseFragment
import com.tencent.iot.explorer.link.core.demo.video.activity.VideoMultiPreviewActivity
import com.tencent.iot.explorer.link.core.demo.video.activity.VideoPlaybackActivity
import com.tencent.iot.explorer.link.core.demo.video.activity.VideoPreviewActivity
import com.tencent.iot.explorer.link.core.demo.video.adapter.DevsAdapter
import com.tencent.iot.explorer.link.core.demo.video.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.core.demo.video.dialog.ToastDialog
import com.tencent.iot.explorer.link.core.demo.video.entity.AccessInfo
import com.tencent.iot.explorer.link.core.demo.video.entity.DevInfo
import com.tencent.iot.explorer.link.core.demo.video.entity.DevUrl2Preview
import com.tencent.iot.video.link.callback.VideoCallback
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.service.VideoBaseService
import kotlinx.android.synthetic.main.fragment_video_device.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VideoDeviceFragment(accessInfo: AccessInfo?) : BaseFragment() {
    private var accessInfo : AccessInfo? = null
    private var devs : MutableList<DevInfo> = ArrayList()
    private var adapter : DevsAdapter? = null
    private var ITEM_MAX_NUM = 4

    init {
        this.accessInfo = accessInfo
    }

    override fun getContentView(): Int {
        return R.layout.fragment_video_device
    }

    override fun startHere(view: View) {
        setListener()
        switchBtnStatus(false)
        var devGridLayoutManager = GridLayoutManager(context, 2)
        context?.let {
            adapter = DevsAdapter(it, devs)
            adapter?.let {
                it.maxNum = ITEM_MAX_NUM
                it.setOnItemClicked(onItemClickedListener)
            }
            gv_devs.setLayoutManager(devGridLayoutManager)
            gv_devs.setAdapter(adapter)
            loadMoreVideoInfo()
        }

        smart_refresh_layout.setEnableRefresh(true)
        smart_refresh_layout.setRefreshHeader(ClassicsHeader(context))
        smart_refresh_layout.setEnableLoadMore(false)
        smart_refresh_layout.setRefreshFooter(ClassicsFooter(context))
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setListener() {
        radio_edit.setOnClickListener {
            var options = arrayListOf(getString(R.string.edit_devs_2_show))
            var dlg = ListOptionsDialog(context, options)
            dlg.show()
            dlg.setOnDismisListener {
                switchBtnStatus(true)
            }
        }

        radio_complete.setOnClickListener {
            switchBtnStatus(false)
            startMultiPreview()
        }

        smart_refresh_layout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                refreshLayout.finishLoadMore()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                loadMoreVideoInfo()
            }
        })
    }

    private fun startMultiPreview() {
        adapter?.let {
            var allUrl = ArrayList<DevUrl2Preview>()
            for (i in 0 until it.list.size) {
                if (it.checkedIds.contains(i)) {
                    var dev = DevUrl2Preview()
                    dev.devName = it.list.get(i).deviceName
                    dev.Status = it.list.get(i).Status
                    allUrl.add(dev)
                }
            }

            if (allUrl.size <= 0) {
                ToastDialog(context, ToastDialog.Type.WARNING, getString(R.string.at_least_one), 2000).show()
                return@let
            }

            var intent = Intent(context, VideoMultiPreviewActivity::class.java)
            var bundle = Bundle()
            intent.putExtra(VideoConst.VIDEO_URLS, bundle)
            bundle.putString(VideoConst.VIDEO_URLS, JSON.toJSONString(allUrl))
            startActivity(intent)
        }
    }

    private fun switchBtnStatus(status: Boolean) {
        radio_complete.visibility = if (status) View.VISIBLE else View.GONE
        radio_edit.visibility = if (status) View.GONE else View.VISIBLE

        if (status) {
            adapter?.let {
                it.showCheck = true
                it.checkedIds.clear()
            }

        } else {
            adapter?.let {
                it.showCheck = false
            }
        }

        adapter?.notifyDataSetChanged()
    }

    var onItemClickedListener = object: DevsAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int, dev: DevInfo) {
            var options = arrayListOf(getString(R.string.preview), getString(R.string.playback))
            var dlg = ListOptionsDialog(context, options)
            dlg.show()
            dlg.setOnDismisListener {
                when(it) {
                    0 -> {
                        var intent = Intent(context, VideoPreviewActivity::class.java)
                        var bundle = Bundle()
                        intent.putExtra(VideoConst.VIDEO_CONFIG, bundle)
                        var devInfo = DevUrl2Preview()
                        devInfo.devName = dev.deviceName
                        devInfo.Status = dev.Status
                        bundle.putString(VideoConst.VIDEO_CONFIG, JSON.toJSONString(devInfo))
                        startActivity(intent)
                    }
                    1 -> {
                        jumpActivity(VideoPlaybackActivity::class.java)
                    }
                }
            }
        }
        override fun onItemMoreClicked(pos: Int, dev: DevInfo) {}
        override fun onItemCheckedClicked(pos: Int, checked: Boolean) {}
        override fun onItemCheckedLimited() {
            ToastDialog(context, ToastDialog.Type.WARNING, getString(R.string.devs_limit), 2000).show()
        }
    }

    private var loadDevListener = object : VideoCallback {
        override fun fail(msg: String?, reqCode: Int) {
            GlobalScope.launch (Dispatchers.Main) {
                adapter?.notifyDataSetChanged()
                if (smart_refresh_layout.isRefreshing) smart_refresh_layout.finishRefresh()
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
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
                if (smart_refresh_layout.isRefreshing) smart_refresh_layout.finishRefresh()
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