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
import com.tencent.iot.explorer.link.core.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.fragment.BaseFragment
import com.tencent.iot.explorer.link.core.demo.video.activity.VideoMultiPreviewActivity
import com.tencent.iot.explorer.link.core.demo.video.activity.VideoNvrActivity
import com.tencent.iot.explorer.link.core.demo.video.activity.VideoPlaybackActivity
import com.tencent.iot.explorer.link.core.demo.video.activity.VideoPreviewActivity
import com.tencent.iot.explorer.link.core.demo.video.adapter.DevsAdapter
import com.tencent.iot.explorer.link.core.demo.video.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.core.demo.video.dialog.ToastDialog
import com.tencent.iot.explorer.link.core.demo.video.entity.DevInfo
import com.tencent.iot.explorer.link.core.demo.video.entity.DevUrl2Preview
import com.tencent.iot.explorer.link.core.demo.video.entity.VideoProductInfo
import com.tencent.iot.video.link.callback.VideoCallback
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.consts.VideoRequestCode
import com.tencent.iot.video.link.service.VideoBaseService
import kotlinx.android.synthetic.main.fragment_video_device.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VideoDeviceFragment : BaseFragment(), VideoCallback, DevsAdapter.OnItemClicked {
    private var devs : MutableList<DevInfo> = ArrayList()
    private var adapter : DevsAdapter? = null
    private var videoProductInfo : VideoProductInfo? = null

    override fun getContentView(): Int {
        return R.layout.fragment_video_device
    }

    override fun startHere(view: View) {
        setListener()

        var devGridLayoutManager = GridLayoutManager(context, 2)
        context?.let {
            adapter = DevsAdapter(it, devs)
            adapter?.let {
                it.setOnItemClicked(this)
                it.tipText = tv_tip_txt
            }
            adapter?.radioComplete = radio_complete
            adapter?.radioEdit = radio_edit
            gv_devs.setLayoutManager(devGridLayoutManager)
            gv_devs.setAdapter(adapter)
            loadAllVideoInfo()
        }
        adapter?.switchBtnStatus(false)

        smart_refresh_layout.setEnableRefresh(true)
        smart_refresh_layout.setRefreshHeader(ClassicsHeader(context))
        smart_refresh_layout.setEnableLoadMore(false)
        smart_refresh_layout.setRefreshFooter(ClassicsFooter(context))
    }

    private fun setListener() {
        radio_edit.setOnClickListener {
            var options = arrayListOf(getString(R.string.edit_devs_2_show))
            var dlg = ListOptionsDialog(context, options)
            dlg.show()
            dlg.setOnDismisListener {
                adapter?.switchBtnStatus(true)
            }
        }

        radio_complete.setOnClickListener {
            adapter?.switchBtnStatus(false)
            startMultiPreview()
        }

        smart_refresh_layout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {}
            override fun onRefresh(refreshLayout: RefreshLayout) {
                loadAllVideoInfo()
            }
        })
    }

    private fun startMultiPreview() {
        if (adapter?.checkedIds!!.size <= 0) {
            ToastDialog(context, ToastDialog.Type.WARNING, getString(R.string.at_least_one), 2000).show()
            return
        }

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

            VideoMultiPreviewActivity.startMultiPreviewActivity(context, allUrl)
        }
    }

    override fun onItemClicked(pos: Int, dev: DevInfo) {
        if (videoProductInfo?.DeviceType == VideoProductInfo.DEV_TYPE_NVR) {
            var intent = Intent(context, VideoNvrActivity::class.java)
            var bundle = Bundle()
            bundle.putString(VideoConst.VIDEO_NVR_INFO, JSON.toJSONString(dev))
            intent.putExtra(VideoConst.VIDEO_NVR_INFO, bundle)
            startActivity(intent)
            return
        }

        var options = arrayListOf(getString(R.string.preview), getString(R.string.playback))
        var dlg = ListOptionsDialog(context, options)
        dlg.show()
        dlg.setOnDismisListener {
            when(it) {
                0 -> { VideoPreviewActivity.startPreviewActivity(context, dev) }
                1 -> { VideoPlaybackActivity.startPlaybackActivity(context, dev) }
            }
        }
    }
    override fun onItemMoreClicked(pos: Int, dev: DevInfo) {}
    override fun onItemCheckedClicked(pos: Int, checked: Boolean) {}
    override fun onItemCheckedLimited() {
        ToastDialog(context, ToastDialog.Type.WARNING, getString(R.string.devs_limit), 2000).show()
    }

    private fun loadAllVideoInfo() {
        App.data.accessInfo?.let {
            VideoBaseService(it.accessId, it.accessToken).getProductInfo(it.productId, this)
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        GlobalScope.launch (Dispatchers.Main) {
            adapter?.notifyDataSetChanged()
            smart_refresh_layout?.finishRefresh()
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun success(response: String?, reqCode: Int) {
        val jsonObject = JSON.parse(response) as JSONObject
        val jsonResponset = jsonObject.getJSONObject("Response") as JSONObject

        when (reqCode) {
            VideoRequestCode.video_describe_devices -> {
                if (jsonResponset.containsKey("Devices")) {
                    val dataArray: JSONArray = jsonResponset.getJSONArray("Devices")
                    for (i in 0 until dataArray.size) {
                        var dev = JSON.parseObject(dataArray.getString(i), DevInfo::class.java)
                        devs.add(dev)
                    }
                }

                GlobalScope.launch (Dispatchers.Main) {
                    adapter?.videoProductInfo = videoProductInfo
                    adapter?.notifyDataSetChanged()
                    if (adapter?.videoProductInfo?.DeviceType == VideoProductInfo.DEV_TYPE_IPC) {
                        rg_edit_dev.visibility = View.VISIBLE
                    } else {
                        rg_edit_dev.visibility = View.GONE
                    }
                    smart_refresh_layout?.finishRefresh()
                }
            }

            VideoRequestCode.video_describe_product -> {
                if (jsonResponset.containsKey("Data")) {
                    videoProductInfo = jsonResponset.getObject("Data", VideoProductInfo::class.java)
                    requestDevs()
                }
            }
        }
    }

    private fun requestDevs() {
        videoProductInfo?.let {
            App.data.accessInfo?.let {
                devs.clear()
                VideoBaseService(it.accessId, it.accessToken).getDeviceList(it.productId, 0, 99, this)
            }
        }
    }
}