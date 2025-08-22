package com.tencent.iot.explorer.link.demo.video

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.fragment.BaseFragment
import com.tencent.iot.explorer.link.demo.databinding.FragmentVideoDeviceBinding
import com.tencent.iot.explorer.link.demo.video.nvr.VideoNvrActivity
import com.tencent.iot.explorer.link.demo.video.playback.VideoPlaybackActivity
import com.tencent.iot.explorer.link.demo.video.preview.DevUrl2Preview
import com.tencent.iot.explorer.link.demo.video.preview.VideoMultiPreviewActivity
import com.tencent.iot.explorer.link.demo.video.preview.VideoPreviewActivity
import com.tencent.iot.explorer.link.demo.video.preview.VideoPreviewMJPEGActivity
import com.tencent.iot.explorer.link.demo.video.preview.VideoPushStreamActivity
import com.tencent.iot.explorer.link.demo.video.preview.VideoWithoutPropertyActivity
import com.tencent.iot.explorer.link.demo.video.utils.ListOptionsDialog
import com.tencent.iot.explorer.link.demo.video.utils.MultipleChannelChooseDialog
import com.tencent.iot.explorer.link.demo.video.utils.ToastDialog
import com.tencent.iot.video.link.callback.VideoCallback
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.consts.VideoRequestCode
import com.tencent.iot.video.link.service.VideoBaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class VideoDeviceFragment : BaseFragment<FragmentVideoDeviceBinding>(), VideoCallback,
    DevsAdapter.OnItemClicked,
    CoroutineScope by MainScope() {
    private var devs: MutableList<DevInfo> = ArrayList()
    private var adapter: DevsAdapter? = null
    private var videoProductInfo: VideoProductInfo? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVideoDeviceBinding = FragmentVideoDeviceBinding.inflate(inflater, container, false)

    override fun startHere(view: View) {
        setListener()
        val devGridLayoutManager = GridLayoutManager(context, 2)

        with(binding) {
            context?.let {
                adapter = DevsAdapter(it, devs)
                adapter?.let {
                    it.setOnItemClicked(this@VideoDeviceFragment)
                    it.tipText = tvTipTxt
                }
                adapter?.radioComplete = radioComplete
                adapter?.radioEdit = radioEdit
                gvDevs.layoutManager = devGridLayoutManager
                gvDevs.adapter = adapter
                loadAllVideoInfo()
            }

            adapter?.switchBtnStatus(false)
            smartRefreshLayout.setEnableRefresh(true)
            smartRefreshLayout.setRefreshHeader(ClassicsHeader(context))
            smartRefreshLayout.setEnableLoadMore(false)
            smartRefreshLayout.setRefreshFooter(ClassicsFooter(context))
        }
    }

    private fun setListener() {
        binding.radioEdit.setOnClickListener {
            val options = arrayListOf(getString(R.string.edit_devs_2_show))
            val dlg = ListOptionsDialog(context, options)
            dlg.show()
            dlg.setOnDismisListener {
                adapter?.switchBtnStatus(true)
            }
        }

        binding.radioComplete.setOnClickListener {
            adapter?.switchBtnStatus(false)
            startMultiPreview()
        }

        binding.smartRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {}
            override fun onRefresh(refreshLayout: RefreshLayout) {
                loadAllVideoInfo()
            }
        })
    }

    private fun startMultiPreview() {
        if (adapter?.checkedIds!!.size <= 0) {
            ToastDialog(
                context,
                ToastDialog.Type.WARNING,
                getString(R.string.at_least_one),
                2000
            ).show()
            return
        }

        adapter?.let {
            val allUrl = ArrayList<DevUrl2Preview>()
            for (i in 0 until it.list.size) {
                if (it.checkedIds.contains(i)) {
                    val dev = DevUrl2Preview()
                    dev.devName = it.list.get(i).DeviceName
                    dev.Status = it.list.get(i).Status
                    allUrl.add(dev)
                }
            }

//            VideoMultiPreviewActivity.startMultiPreviewActivity(context, allUrl)
        }
    }

    override fun onItemClicked(pos: Int, dev: DevInfo) {
        if (videoProductInfo?.DeviceType == VideoProductInfo.DEV_TYPE_NVR) {
            val intent = Intent(context, VideoNvrActivity::class.java)
            val bundle = Bundle()
            bundle.putString(VideoConst.VIDEO_NVR_INFO, JSON.toJSONString(dev))
            intent.putExtra(VideoConst.VIDEO_NVR_INFO, bundle)
            startActivity(intent)
            return
        }

        val options = arrayListOf(
            getString(R.string.preview),
            getString(R.string.playback),
            getString(R.string.preview_mjpeg),
            getString(R.string.video_without_property),
            getString(R.string.video_push_stream),
            getString(R.string.multiple_channel_choose)
        )
        val dlg =
            ListOptionsDialog(
                context,
                options
            )
        dlg.show()
        dlg.setOnDismisListener { it ->
            when (it) {
                0 -> {
                    VideoPreviewActivity.startPreviewActivity(context, dev)
                }

                1 -> {
                    VideoPlaybackActivity.startPlaybackActivity(context, dev)
                }

                2 -> {
                    VideoPreviewMJPEGActivity.startPreviewActivity(context, dev)
                }

                3 -> {
                    VideoWithoutPropertyActivity.startPreviewActivity(context, dev)
                }

                4 -> {
                    VideoPushStreamActivity.startPreviewActivity(context, dev)
                }

                5 -> {
                    val multipleChannelChooseDialog = MultipleChannelChooseDialog(context)
                    multipleChannelChooseDialog.show()
                    multipleChannelChooseDialog.setOnDismisListener { selectChannels ->
                        VideoMultiPreviewActivity.startMultiPreviewActivity(
                            context, dev, selectChannels
                        )
                    }
                }
            }
        }
    }

    override fun onItemMoreClicked(pos: Int, dev: DevInfo) {}
    override fun onItemCheckedClicked(pos: Int, checked: Boolean) {}
    override fun onItemCheckedLimited() {
        ToastDialog(
            context,
            ToastDialog.Type.WARNING,
            getString(R.string.devs_limit),
            2000
        ).show()
    }

    private fun loadAllVideoInfo() {
        App.data.accessInfo?.let {
            VideoBaseService(it.accessId, it.accessToken).getProductInfo(it.productId, this)
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        launch(Dispatchers.Main) {
            adapter?.notifyDataSetChanged()
            binding.smartRefreshLayout.finishRefresh()
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
                        val dev = JSON.parseObject(dataArray.getString(i), DevInfo::class.java)
                        devs.add(dev)
                    }
                }

                launch(Dispatchers.Main) {
                    adapter?.videoProductInfo = videoProductInfo
                    adapter?.notifyDataSetChanged()
                    if (adapter?.videoProductInfo?.DeviceType == VideoProductInfo.DEV_TYPE_IPC) {
                        binding.rgEditDev.visibility = View.VISIBLE
                    } else {
                        binding.rgEditDev.visibility = View.GONE
                    }
                    binding.smartRefreshLayout.finishRefresh()
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
                VideoBaseService(it.accessId, it.accessToken).describeDevices(
                    it.productId,
                    99,
                    0,
                    this
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}