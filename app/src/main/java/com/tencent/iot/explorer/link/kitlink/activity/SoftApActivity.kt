package com.tencent.iot.explorer.link.kitlink.activity

import android.util.Log
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.customview.progress.bean.StepBean
import com.tencent.iot.explorer.link.kitlink.fragment.*
import com.tencent.iot.explorer.link.kitlink.popup.CommonPopupWindow
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.GetBindDeviceTokenPresenter
import com.tencent.iot.explorer.link.mvp.view.GetBindDeviceTokenView
import com.tencent.iot.explorer.link.util.T
import com.tencent.iot.explorer.link.util.check.LocationUtil
import kotlinx.android.synthetic.main.activity_smart_connect.*
import kotlinx.android.synthetic.main.activity_soft_ap.*
import java.util.ArrayList

class SoftApActivity : PActivity(), GetBindDeviceTokenView {

    private lateinit var presenter: GetBindDeviceTokenPresenter
    private var ssid = ""
    private var bssid = ""
    private var password = ""

    private lateinit var softAppStepFragment: SoftAppStepFragment
    private lateinit var wifiFragment: WifiFragment
    private lateinit var devWifiFragment: WifiFragment
    private lateinit var softHotspotFragment: SoftHotspotFragment
    private lateinit var connectProgressFragment: ConnectProgressFragment

    private var closePopup: CommonPopupWindow? = null

    override fun getContentView(): Int {
        return R.layout.activity_soft_ap
    }

    private fun showProgress() {
        val stepsBeanList = ArrayList<StepBean>()
        stepsBeanList.add(StepBean(getString(R.string.config_hardware)))
        stepsBeanList.add(StepBean(getString(R.string.set_target_wifi)))
        stepsBeanList.add(StepBean(getString(R.string.connect_device)))
        stepsBeanList.add(StepBean(getString(R.string.start_config_network)))
        softap_step_progress.currentStep = 1
        softap_step_progress.setStepViewTexts(stepsBeanList)
        softap_step_progress.setTextSize(12)
    }

    override fun initView() {
        showProgress()

        presenter = GetBindDeviceTokenPresenter(this)
        softAppStepFragment = SoftAppStepFragment()
        wifiFragment = WifiFragment(WifiFragment.soft_ap)
        devWifiFragment = WifiFragment(WifiFragment.soft_ap, true)
        softHotspotFragment = SoftHotspotFragment()
        connectProgressFragment = ConnectProgressFragment(WifiFragment.soft_ap)

        this.supportFragmentManager.beginTransaction()
            .add(R.id.container_soft_ap, softAppStepFragment)
            .show(softAppStepFragment).commit()
    }

    override fun setListener() {

        tv_soft_ap_cancel.setOnClickListener {
            if (connectProgressFragment.isVisible) {
                showPopup()
            } else {
                finish()
            }
        }

        softAppStepFragment.onNextListener = object : SoftAppStepFragment.OnNextListener {
            override fun onNext() {
                softap_step_progress.currentStep = 2
                softap_step_progress.refreshStepViewState()
                showFragment(wifiFragment, softAppStepFragment)
            }
        }

        wifiFragment.onCommitWifiListener = object : WifiFragment.OnCommitWifiListener {
            override fun commitWifi(ssid: String, bssid: String?, password: String) {
                this@SoftApActivity.let {
                    it.ssid = ssid
                    it.bssid = if (bssid == null) "" else bssid
                    it.password = password
                }
                presenter.getBindDeviceToken()
            }
        }

        softHotspotFragment.onNextListener = object : SoftHotspotFragment.OnNextListener {
            override fun onNext() {
                showFragment(devWifiFragment, softHotspotFragment)
            }
        }

        devWifiFragment.onCommitWifiListener = object: WifiFragment.OnCommitWifiListener {
            override fun commitWifi(ssid: String, bssid: String?, password: String) {
                softap_step_progress.currentStep = 4
                softap_step_progress.refreshStepViewState()
                connectProgressFragment.setWifiInfo(this@SoftApActivity.ssid,
                    this@SoftApActivity.bssid, this@SoftApActivity.password)
                showFragment(connectProgressFragment, devWifiFragment)
            }
        }

        connectProgressFragment.onRestartListener = object : ConnectProgressFragment.OnRestartListener {
            override fun restart() {
                showFragment(softAppStepFragment, connectProgressFragment)
            }
        }
    }

    private fun showFragment(showFragment: BaseFragment, hideFragment: BaseFragment) {
        if (showFragment.isAdded) {
            this.supportFragmentManager.beginTransaction()
                .show(showFragment)
                .hide(hideFragment)
                .commit()

        } else {
            this.supportFragmentManager.beginTransaction()
                .add(R.id.container_soft_ap, showFragment)
                .show(showFragment)
                .hide(hideFragment)
                .commit()
        }
    }

    private fun showPopup() {
        if (closePopup == null) {
            closePopup = CommonPopupWindow(this)
            closePopup?.setCommonParams(
                getString(R.string.exit_toast_title),
                getString(R.string.exit_toast_content)
            )
            closePopup?.setMenuText(getString(R.string.cancel), getString(R.string.confirm))
            closePopup?.setBg(soft_ap_bg)
            closePopup?.onKeyListener = object : CommonPopupWindow.OnKeyListener {
                override fun confirm(popupWindow: CommonPopupWindow) {
                    finish()
                }

                override fun cancel(popupWindow: CommonPopupWindow) {
                    popupWindow.dismiss()
                }
            }
        }
        closePopup?.show(soft_ap)
    }

    override fun onBackPressed() {
        if (connectProgressFragment.isVisible) {
            showPopup()
        } else {
            super.onBackPressed()
        }
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun onDestroy() {
        closePopup?.dismiss()
        super.onDestroy()
    }

    override fun onSuccess(token: String) {
        softap_step_progress.currentStep = 3
        softap_step_progress.refreshStepViewState()
        showFragment(softHotspotFragment, wifiFragment)
        L.e("getToken onSuccess token:" + token)
    }

    override fun onFail(msg: String) {
        L.e("getToken onFail msg:" + msg)
    }

}
