package com.tencent.iot.explorer.link.demo.video

import android.os.Bundle
import android.text.TextUtils
import androidx.viewbinding.ViewBinding
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.common.util.StatusBarUtil
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.EventPresenter
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.EventView
import com.tencent.iot.explorer.link.demo.video.preview.DevUrl2Preview
import com.tencent.iot.video.link.callback.VideoCallback
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.service.VideoBaseService
import org.json.JSONObject
import java.util.Date

abstract class VideoPreviewBaseActivity<VB: ViewBinding> : VideoBaseActivity<VB>(), EventView, VideoCallback {

    protected lateinit var presenter: EventPresenter
    protected var xp2pInfo: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkStyle()
    }

    private fun checkStyle() {
        StatusBarUtil.setRootViewFitsSystemWindows(this, false)
        StatusBarUtil.setTranslucentStatus(this)
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            StatusBarUtil.setStatusBarColor(this, 0x55000000)
        }
    }

    override fun performInitView() {
        presenter = EventPresenter(this@VideoPreviewBaseActivity)
        val bundle = intent.getBundleExtra(VideoConst.VIDEO_CONFIG)
        bundle?.let {
            val videoConfig = bundle.getString(VideoConst.VIDEO_CONFIG)
            if (TextUtils.isEmpty(videoConfig)) return@let

            val devInfo = JSON.parseObject(videoConfig, DevUrl2Preview::class.java)
            devInfo?.let {
                presenter.setDeviceName(it.devName)
                presenter.setChannel(it.channel)
            }
        }

        App.data.accessInfo?.let {
            presenter.setAccessId(it.accessId)
            presenter.setAccessToken(it.accessToken)
            presenter.setProductId(it.productId)
            presenter.getEventsData(Date())
        }
    }

    protected fun getDeviceP2PInfo() {
        App.data.accessInfo?.let {
            VideoBaseService(it.accessId, it.accessToken).getDeviceXp2pInfo(
                it.productId,
                presenter.getDeviceName(),
                this
            )
        }
    }

    abstract fun updateXp2pInfo(xp2pInfo: String)

    override fun success(response: String?, reqCode: Int) {
        response?.let {
            val responseObject = JSONObject(it).getJSONObject("Response")
            xp2pInfo = responseObject.getString("P2PInfo")
        }
        updateXp2pInfo(xp2pInfo)
    }
}