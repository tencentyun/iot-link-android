package com.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import com.auth.IoTAuth
import com.kitlink.App
import com.kitlink.R
import com.kitlink.consts.CommonField
import com.kitlink.entity.DeviceEntity
import com.kitlink.entity.DevicePropertyEntity
import com.kitlink.entity.NavBar
import com.kitlink.popup.EnumPopupWindow
import com.kitlink.popup.NumberPopupWindow
import com.kitlink.theme.PanelTheme
import com.kitlink.theme.PanelThemeManager
import com.kitlink.util.JsonManager
import com.kitlink.util.StatusBarUtil
import com.mvp.IPresenter
import com.mvp.presenter.ControlPanelPresenter
import com.mvp.view.ControlPanelView
import com.util.L
import com.view.recyclerview.CRecyclerView
import com.kitlink.activity.PActivity
import com.kitlink.popup.OfflinePopupWindow
import kotlinx.android.synthetic.main.activity_control_panel.*
import kotlinx.android.synthetic.main.menu_back_and_right.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.coroutines.*

/**
 * 控制面板
 */
class ControlPanelActivity : PActivity(), ControlPanelView, CRecyclerView.RecyclerItemView {

    private var deviceEntity: DeviceEntity? = null

    private lateinit var presenter: ControlPanelPresenter

    //    private var aliasName = ""
    private var numberPopup: NumberPopupWindow? = null
    private var enumPopup: EnumPopupWindow? = null
    private var offlinePopup: OfflinePopupWindow? = null
    private var job: Job? = null

    override fun getContentView(): Int {
        return R.layout.activity_control_panel
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun onResume() {
        super.onResume()
        tv_title.text = deviceEntity?.getAlias()
        presenter.requestDeviceData()
    }

    override fun initView() {
        presenter = ControlPanelPresenter(this)
        deviceEntity = get("device")
        deviceEntity?.run {
            presenter.setProductId(ProductId)
            presenter.setDeviceName(DeviceName)
            presenter.setDeviceId(DeviceId)
            //不能添加头部，否则bindView中gridLayoutManager的getSpanSize(position: Int)会出错
            PanelThemeManager.instance.bindView(this@ControlPanelActivity, crv_panel)
            crv_panel.setList(presenter.model!!.devicePropertyList)
            crv_panel.addRecyclerItemView(this@ControlPanelActivity)
            presenter.requestControlPanel()
            presenter.registerActivePush()

            if (online != 1) {//延时显示
                job = CoroutineScope(Dispatchers.IO).launch {
                    delay(200)
                    CoroutineScope(Dispatchers.Main).launch {
                        showOfflinePopup()
                    }
                }
            }
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        iv_right.setOnClickListener {
            if (App.data.getCurrentFamily().Role == 1 && !deviceEntity!!.shareDevice) {
                jumpActivity(DeviceDetailsActivity::class.java)
            }
        }
    }

    /**
     * 显示设备离线弹框
     */
    private fun showOfflinePopup() {
        if (offlinePopup == null)
            offlinePopup = OfflinePopupWindow(this)
        offlinePopup?.onToHomeListener = object : OfflinePopupWindow.OnToHomeListener {
            override fun toHome(popupWindow: OfflinePopupWindow) {
                popupWindow.dismiss()
                finish()
            }

            override fun toFeedback(popupWindow: OfflinePopupWindow) {
                jumpActivity(FeedbackActivity::class.java, true)
            }
        }
        offlinePopup?.setBg(control_panel_bg)
        offlinePopup?.show(control_panel)
    }

    /**
     *  获取列表对象
     */
    fun getDeviceProperty(position: Int): DevicePropertyEntity {
        presenter.model!!.devicePropertyList.run {
            if (position >= size) {// 云端定时
                val entity = DevicePropertyEntity()
                entity.type = "btn-col-1"
                return entity
            }
            return this[position]
        }
    }

    /**
     * 控制设备
     */
    fun controlDevice(id: String, value: String) {
        presenter.controlDevice(id, value)
    }

    override fun doAction(
            viewHolder: CRecyclerView.CViewHolder<*>, clickView: View, position: Int
    ) {
        deviceEntity?.let {
            if (it.online == 1)
                PanelThemeManager.instance.doAction(
                        viewHolder,
                        clickView,
                        position
                )
        }
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return PanelThemeManager.instance.getViewHolder(parent, viewType)
    }

    override fun getViewType(position: Int): Int {
        return PanelThemeManager.instance.getViewType(presenter.model!!.devicePropertyList[position])
    }

    /**
     * 显示面板
     */
    override fun showControlPanel(themeTag: String, navBar: NavBar?, timingProject: Boolean) {
        runOnUiThread {
            tabTheme(themeTag)
            PanelThemeManager.instance.showTheme(this, themeTag, timingProject)
            showNavBar(themeTag, navBar)
        }
    }

    /**
     *  显示NavBar
     */
    private fun showNavBar(themeTag: String, navBar: NavBar?) {
        navBar?.run {
            if (isShowNavBar()) {
                card_nav_bar.visibility = View.VISIBLE
                if (isShowTemplate()) {
                    ll_template.visibility = View.VISIBLE
                    presenter.model!!.getDevicePropertyForId(navBar.templateId)?.run {
                        if (isBoolType()) {
                            tv_template_name.text = name
                            iv_template.setOnClickListener {
                                when (id) {
                                    "power_switch" -> controlDevice(id, if (getValue() == "1") "0" else "1")
                                }
                            }
                        }
                    }
                } else {
                    ll_template.visibility = View.GONE
                }
                ll_timing_project.visibility = if (isShowTimingProject()) {
                    iv_timing_project.setOnClickListener { jumpToCloudTiming() }
                    View.VISIBLE
                } else {
                    View.GONE
                }
                when (themeTag) {
                    PanelTheme.DARK -> {
                        card_nav_bar.background = getDrawable(R.drawable.control_dark_nav_bar_bg)
                        tv_template_name.setTextColor(getMyColor(R.color.white))
                        tv_timing_project.setTextColor(getMyColor(R.color.white))
                        iv_template.setImageResource(R.mipmap.icon_nav_bar_dark_switch)
                        iv_timing_project.setImageResource(R.mipmap.icon_nav_bar_dark_timer)
                    }
                    PanelTheme.SIMPLE -> {
                        card_nav_bar.background = getDrawable(R.drawable.control_simple_nav_bar_bg)
                        tv_template_name.setTextColor(getMyColor(R.color.black_333333))
                        tv_timing_project.setTextColor(getMyColor(R.color.black_333333))
                        iv_template.setImageResource(R.mipmap.icon_nav_bar_simple_switch)
                        iv_timing_project.setImageResource(R.mipmap.icon_nav_bar_simple_timer)
                    }
                    else -> {
                        card_nav_bar.background =
                                getDrawable(R.drawable.control_standard_nav_bar_bg)
                        tv_template_name.setTextColor(getMyColor(R.color.white))
                        tv_timing_project.setTextColor(getMyColor(R.color.white))
                        iv_template.setImageResource(R.mipmap.icon_nav_bar_standard_switch)
                        iv_timing_project.setImageResource(R.mipmap.icon_nav_bar_standard_timer)
                    }
                }
            } else {
                card_nav_bar.visibility = View.GONE
            }
        }
    }

    /**
     * 跳转到云端定时
     */
    fun jumpToCloudTiming() {
        put("property", presenter.model!!.devicePropertyList)
        jumpActivity(CloudTimingActivity::class.java)
    }

    /**
     * 切换主题背景
     */
    private fun tabTheme(themeTag: String) {
        when (themeTag) {
            PanelTheme.DARK -> {
                StatusBarUtil.setStatusBarDarkTheme(this, false)
                iv_back.setColorFilter(resources.getColor(R.color.white))
                tv_title.setTextColor(resources.getColor(R.color.white))
                iv_right.setImageResource(R.mipmap.icon_white_more)
                control_panel.background = resources.getDrawable(R.mipmap.bg_control_dark)
            }
            PanelTheme.SIMPLE -> {
                StatusBarUtil.setStatusBarDarkTheme(this, true)
                iv_back.setColorFilter(resources.getColor(R.color.black_333333))
                tv_title.setTextColor(resources.getColor(R.color.black_333333))
                iv_right.setImageResource(R.mipmap.icon_black_more)
                control_panel.setBackgroundColor(resources.getColor(R.color.white))
            }
            else -> {
                StatusBarUtil.setStatusBarDarkTheme(this, false)
                iv_back.setColorFilter(resources.getColor(R.color.white))
                tv_title.setTextColor(resources.getColor(R.color.white))
                iv_right.setImageResource(R.mipmap.icon_white_more)
                control_panel.setBackgroundResource(R.mipmap.image_control_standard)
            }
        }
    }

    /**
     * 显示进度弹框
     */
    fun showNumberPopup(entity: DevicePropertyEntity) {
        if (numberPopup == null) {
            numberPopup = NumberPopupWindow(this)
            numberPopup?.onUploadListener = object : NumberPopupWindow.OnUploadListener {
                override fun upload(progress: Int) {
                    controlDevice(entity.id, progress.toString())
                    numberPopup?.dismiss()
                }
            }
        }
        numberPopup!!.showTitle(entity.name)
        val min = entity.numberEntity!!.min.toDouble().toInt()
        numberPopup!!.setRange(
                min,
                entity.numberEntity!!.max.toDouble().toInt()
        )
        val p = entity.getValue().toDouble().toInt()
        numberPopup!!.setProgress(if (p < min) min else p)
        numberPopup!!.setUnit(entity.numberEntity!!.unit)
        numberPopup?.setBg(control_panel_bg)
        numberPopup?.show(control_panel)
    }

    /**
     * 显示枚举弹框
     */
    fun showEnumPopup(entity: DevicePropertyEntity) {
        if (enumPopup == null) {
            enumPopup = EnumPopupWindow(this)
            enumPopup?.onUploadListener = object : EnumPopupWindow.OnUploadListener {
                override fun upload(value: String) {
                    controlDevice(entity.id, value)
                    enumPopup?.dismiss()
                }
            }
        }
        enumPopup!!.showTitle(entity.name)
        enumPopup!!.selectKey = entity.getValue()
        enumPopup!!.setList(entity.enumEntity!!.mapping)
        enumPopup?.setBg(control_panel_bg)
        enumPopup?.show(control_panel)
    }

    override fun onBackPressed() {
        enumPopup?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        numberPopup?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        offlinePopup?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        PanelThemeManager.instance.destroy()
        job?.cancel()
        super.onDestroy()
    }

}
