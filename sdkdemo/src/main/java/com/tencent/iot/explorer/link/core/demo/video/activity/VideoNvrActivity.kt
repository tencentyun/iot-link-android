package com.tencent.iot.explorer.link.core.demo.video.activity

import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.fastjson.JSON
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import com.tencent.iot.explorer.link.core.demo.video.adapter.DevsAdapter
import com.tencent.iot.explorer.link.core.demo.video.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.core.demo.video.dialog.ToastDialog
import com.tencent.iot.explorer.link.core.demo.video.entity.DevInfo
import com.tencent.iot.explorer.link.core.demo.video.entity.VideoProductInfo
import com.tencent.iot.video.link.consts.VideoConst
import kotlinx.android.synthetic.main.fragment_video_device.*
import kotlinx.android.synthetic.main.title_layout.*

class VideoNvrActivity : BaseActivity(), DevsAdapter.OnItemClicked {
    private var devs : MutableList<DevInfo> = ArrayList()
    private var adapter : DevsAdapter? = null
    private var videoProductInfo = VideoProductInfo()

    override fun getContentView(): Int {
        return R.layout.activity_video_nvr_devs
    }

    override fun initView() {
        var bundle = intent.getBundleExtra(VideoConst.VIDEO_NVR_INFO)
        bundle?.let {
            var devInfoStr = bundle.getString(VideoConst.VIDEO_NVR_INFO)
            if (TextUtils.isEmpty(devInfoStr)) return@let

            var devInfo = JSON.parseObject(devInfoStr, DevInfo::class.java)
            devInfo?.let {
                tv_title.setText(it.deviceName)
            }
        }

        var devGridLayoutManager = GridLayoutManager(this@VideoNvrActivity, 2)
        adapter = DevsAdapter(this@VideoNvrActivity, devs)
        adapter?.let {
            it.setOnItemClicked(this)
            it.tipText = tv_tip_txt
        }
        tv_my_devs.visibility = View.GONE
        gv_devs.setLayoutManager(devGridLayoutManager)
        gv_devs.setAdapter(adapter)
        adapter?.radioComplete = radio_complete
        adapter?.radioEdit = radio_edit
        adapter?.switchBtnStatus(false)

        smart_refresh_layout.setEnableRefresh(true)
        smart_refresh_layout.setRefreshHeader(ClassicsHeader(this@VideoNvrActivity))
        smart_refresh_layout.setEnableLoadMore(false)
        smart_refresh_layout.setRefreshFooter(ClassicsFooter(this@VideoNvrActivity))

        showDev()
    }

    private fun showDev() {
        devs.clear()
        for (i in 0 .. 5) {
            var d1 = DevInfo()
            d1.deviceName = "name " + i
            d1.Status = 1
            devs.add(d1)
        }
        rg_edit_dev.visibility = View.VISIBLE
        adapter?.videoProductInfo = videoProductInfo
        adapter?.notifyDataSetChanged()
        if (smart_refresh_layout.isRefreshing) smart_refresh_layout.finishRefresh()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        radio_edit.setOnClickListener {
            var options = arrayListOf(getString(R.string.edit_devs_2_show))
            var dlg = ListOptionsDialog(this@VideoNvrActivity, options)
            dlg.show()
            dlg.setOnDismisListener {
                adapter?.switchBtnStatus(true)
            }
        }

        radio_complete.setOnClickListener {
            adapter?.switchBtnStatus(false)

            if (adapter?.checkedIds!!.size <= 0) {
                ToastDialog(this@VideoNvrActivity, ToastDialog.Type.WARNING, getString(R.string.at_least_one), 2000).show()
                return@setOnClickListener
            }
        }

        smart_refresh_layout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {}
            override fun onRefresh(refreshLayout: RefreshLayout) {
                showDev()
            }
        })
    }

    override fun onItemMoreClicked(pos: Int, dev: DevInfo) {}
    override fun onItemCheckedClicked(pos: Int, checked: Boolean) {}

    override fun onItemCheckedLimited() {
        ToastDialog(this@VideoNvrActivity, ToastDialog.Type.WARNING, getString(R.string.devs_limit), 2000).show()
    }

    override fun onItemClicked(pos: Int, dev: DevInfo) {
        var options = arrayListOf(getString(R.string.preview), getString(R.string.playback))
        var dlg = ListOptionsDialog(this@VideoNvrActivity, options)
        dlg.show()
        dlg.setOnDismisListener {
            when(it) {
                0 -> { VideoPreviewActivity.startPreviewActivity(this@VideoNvrActivity, dev) }
                1 -> { VideoPlaybackActivity.startPlaybackActivity(this@VideoNvrActivity, dev) }
            }
        }
    }
}



