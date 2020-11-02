package com.tencent.iot.explorer.link.kitlink.activity

import android.text.TextUtils
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.customview.progress.bean.StepBean
import com.tencent.iot.explorer.link.kitlink.fragment.BaseFragment
import com.tencent.iot.explorer.link.kitlink.fragment.ConnectProgressFragment
import com.tencent.iot.explorer.link.kitlink.fragment.SCStepFragment
import com.tencent.iot.explorer.link.kitlink.fragment.WifiFragment
import com.tencent.iot.explorer.link.kitlink.popup.CommonPopupWindow
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.mvp.presenter.GetBindDeviceTokenPresenter
import com.tencent.iot.explorer.link.mvp.view.GetBindDeviceTokenView
import com.tencent.iot.explorer.link.core.utils.LocationUtil
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.consts.LoadViewTxtType
import kotlinx.android.synthetic.main.activity_smart_connect.*

/**
 * 智能配网
 */
class SmartConnectActivity : BaseActivity(), GetBindDeviceTokenView {

    private lateinit var presenter: GetBindDeviceTokenPresenter

    private lateinit var scStepFragment: SCStepFragment
    private lateinit var wifiFragment: WifiFragment
    private lateinit var connectProgressFragment: ConnectProgressFragment
    private var loadViewTextType = LoadViewTxtType.LoadLocalViewTxt.ordinal // 0 加载本地文案  1 尝试加载远端配置文案
    private var productId = ""

    private var closePopup: CommonPopupWindow? = null

    override fun getContentView(): Int {
        return R.layout.activity_smart_connect
    }

    private fun showProgress() {
        val stepsBeanList = ArrayList<StepBean>()
        stepsBeanList.add(StepBean(getString(R.string.config_hardware)))
        stepsBeanList.add(StepBean(getString(R.string.select_wifi)))
        stepsBeanList.add(StepBean(getString(R.string.start_config_network)))
        smart_config_step_progress.currentStep = 1
        smart_config_step_progress.setStepViewTexts(stepsBeanList)
        smart_config_step_progress.setTextSize(12)
    }

    override fun initView() {
        loadViewTextType = intent.getIntExtra(CommonField.LOAD_VIEW_TXT_TYPE, LoadViewTxtType.LoadLocalViewTxt.ordinal)
        if (loadViewTextType != LoadViewTxtType.LoadLocalViewTxt.ordinal) {
            productId = intent.getStringExtra(CommonField.PRODUCT_ID)
        }

        showProgress()

        presenter = GetBindDeviceTokenPresenter(this)
        scStepFragment = SCStepFragment()
        scStepFragment.onNextListener = object : SCStepFragment.OnNextListener {
            override fun onNext() {
                smart_config_step_progress.currentStep = 2
                smart_config_step_progress.refreshStepViewState()
                showFragment(wifiFragment, scStepFragment)
                if (!LocationUtil.isLocationServiceEnable(this@SmartConnectActivity)) {
                    T.showLonger(getString(R.string.open_location_service_for_wifi)) //请您打开手机位置以便获取WIFI名称
                }
            }
        }
        wifiFragment = WifiFragment(WifiFragment.smart_config,false)
        wifiFragment.onCommitWifiListener = object : WifiFragment.OnCommitWifiListener {
            override fun commitWifi(ssid: String, bssid: String?, password: String) {
                if (TextUtils.isEmpty(bssid)) {
                    T.show(getString(R.string.connecting_to_wifi))
                    return
                }

                connectProgressFragment.setWifiInfo(ssid, bssid!!, password)
                presenter.getBindDeviceToken()
            }
        }
        connectProgressFragment = ConnectProgressFragment(WifiFragment.smart_config, loadViewTextType, productId)
        connectProgressFragment.onRestartListener = object : ConnectProgressFragment.OnRestartListener {
            override fun restart() {
                showFragment(scStepFragment, connectProgressFragment)
            }
        }
        supportFragmentManager.beginTransaction()
            .add(R.id.container_smart_connect, scStepFragment)
            .commit()
    }

    private fun showFragment(showFragment: BaseFragment, hideFragment: BaseFragment) {
        val transaction = this.supportFragmentManager.beginTransaction()
        if (showFragment.isAdded) {
            transaction.show(showFragment).hide(hideFragment).commit()

        } else {
            transaction.add(R.id.container_smart_connect, showFragment).hide(hideFragment).commit()
        }
    }

    override fun setListener() {
        tv_smart_config_back.setOnClickListener {
            if (connectProgressFragment.isVisible) {
                showPopup()
            } else {
                finish()
            }
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
            closePopup?.setBg(smart_config_bg)
            closePopup?.onKeyListener = object : CommonPopupWindow.OnKeyListener {
                override fun confirm(popupWindow: CommonPopupWindow) {
                    finish()
                }

                override fun cancel(popupWindow: CommonPopupWindow) {
                    popupWindow.dismiss()
                }
            }
        }
        closePopup?.show(smart_config)
    }

    override fun onBackPressed() {
        if (connectProgressFragment.isVisible) {
            showPopup()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        closePopup?.dismiss()
        super.onDestroy()
    }

    override fun onSuccess(token: String) {
        smart_config_step_progress.currentStep = 3
        smart_config_step_progress.refreshStepViewState()
        showFragment(connectProgressFragment, wifiFragment)
    }

    override fun onFail(msg: String) {
        L.e("getToken onFail msg:" + msg)
    }
}
