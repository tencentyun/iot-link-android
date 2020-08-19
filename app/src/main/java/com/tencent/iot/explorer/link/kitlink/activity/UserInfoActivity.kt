package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.popup.CameraPopupWindow
import com.tencent.iot.explorer.link.kitlink.popup.CommonPopupWindow
import com.tencent.iot.explorer.link.kitlink.popup.EditPopupWindow
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.UserInfoPresenter
import com.tencent.iot.explorer.link.mvp.view.UserInfoView
import com.tencent.iot.explorer.link.util.AppInfoUtils
import com.tencent.iot.explorer.link.util.SharePreferenceUtil
import com.tencent.iot.explorer.link.util.T
import com.tencent.iot.explorer.link.util.picture.imageselectorbrowser.ImageSelectorConstant.REQUEST_IMAGE
import com.tencent.iot.explorer.link.util.picture.imp.ImageManager
import com.tencent.iot.explorer.link.util.picture.imp.ImageSelectorUtils
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.dialog_temperature.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.util.*


/**
 * 个人信息界面
 */
class UserInfoActivity : PActivity(), UserInfoView, View.OnClickListener, View.OnLongClickListener {

    private lateinit var presenter: UserInfoPresenter
    private var popupWindow: CameraPopupWindow? = null
    private var commonPopupWindow: CommonPopupWindow? = null
    private var editPopupWindow: EditPopupWindow? = null
    private lateinit var temperatureDialogView: View
    private lateinit var bottomDialog: Dialog

    companion object {
        const val TIMEZONE_REQUESTCODE = 100
    }

    private var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun getContentView(): Int {
        return R.layout.activity_user_info
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun initView() {
        presenter = UserInfoPresenter(this)
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.personal_info)
        temperatureDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_temperature, null)
        bottomDialog = Dialog(this, R.style.BottomDialog)
    }

    override fun onResume() {
        super.onResume()
        showUserInfo()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_title_nick.setOnClickListener(this)
        tv_user_info_logout.setOnClickListener(this)
        tv_user_id.setOnLongClickListener(this)
        tv_account_and_safety.setOnClickListener(this)

        iv_avatar.setOnClickListener(this)
        iv_avatar_arrow.setOnClickListener(this)
        tv_title_avatar.setOnClickListener(this)

        tv_temperature_unit_title.setOnClickListener(this)
        tv_temperature_unit.setOnClickListener(this)
        iv_temperature_unit_arrow.setOnClickListener(this)

        iv_time_zone_arrow.setOnClickListener(this)
        tv_time_zone_title.setOnClickListener(this)
        tv_time_zone.setOnClickListener(this)

        temperatureDialogView.tv_fahrenheit.setOnClickListener(this)
        temperatureDialogView.tv_celsius.setOnClickListener(this)
        temperatureDialogView.tv_cancel.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_title_avatar, iv_avatar, iv_avatar_arrow -> {
                if (checkPermissions(permissions))
                    showCameraPopup()
                else requestPermission(permissions)
            }
            tv_title_nick -> {
                showEditPopup()
            }
            tv_user_info_logout -> {
                presenter.logout()
            }
            tv_account_and_safety -> {
                jumpActivity(AccountAndSafetyActivity::class.java)
            }
            tv_temperature_unit_title, tv_temperature_unit, iv_temperature_unit_arrow -> {// 温度单位
                showTemperatureDialog()
            }
            tv_time_zone_title, tv_time_zone, iv_time_zone_arrow -> {// 时区
                startActivityForResult(Intent(this, TimeZoneActivity::class.java), TIMEZONE_REQUESTCODE)
            }
            temperatureDialogView.tv_fahrenheit -> {
                presenter.setTemperatureUnit(getString(R.string.fahrenheit))
                bottomDialog.dismiss()
            }
            temperatureDialogView.tv_celsius -> {
                presenter.setTemperatureUnit(getString(R.string.celsius))
                bottomDialog.dismiss()
            }
            temperatureDialogView.tv_cancel -> {
                bottomDialog.dismiss()
            }
        }
    }

    private fun showCameraPopup() {
        if (popupWindow == null) {
            popupWindow = CameraPopupWindow(this)
        }
        popupWindow?.setBg(user_info_popup_bg)
        popupWindow?.show(user_info)
    }

    private fun showCommonPopup() {
        if (commonPopupWindow == null) {
            commonPopupWindow = CommonPopupWindow(this)
        }
        commonPopupWindow?.setBg(user_info_popup_bg)
        commonPopupWindow?.setCommonParams(
            getString(R.string.please_bind_title),
            getString(R.string.please_bind_content)
        )
        commonPopupWindow?.setMenuText("", getString(R.string.bind))
        commonPopupWindow?.onKeyListener = object : CommonPopupWindow.OnKeyListener {
            override fun cancel(popupWindow: CommonPopupWindow) {
                popupWindow.dismiss()
            }

            override fun confirm(popupWindow: CommonPopupWindow) {
                jumpActivity(BindMobilePhoneActivity::class.java)
                popupWindow.dismiss()
            }
        }
        commonPopupWindow?.show(user_info)
    }

    private fun showEditPopup() {
        if (editPopupWindow == null) {
            editPopupWindow = EditPopupWindow(this)
        }
        editPopupWindow?.setShowData(
            getString(R.string.nick),
            App.data.userInfo.NickName
        )
        editPopupWindow?.onVerifyListener = object : EditPopupWindow.OnVerifyListener {
            override fun onVerify(text: String) {
                if (TextUtils.isEmpty(text)) {
                    T.show("请输入昵称")
                    return
                }
                presenter.modifyNick(text)
                editPopupWindow?.dismiss()
            }
        }
        editPopupWindow?.setBg(user_info_popup_bg)
        editPopupWindow?.show(user_info)
    }

    private fun showRegion(region: String) {
        if (!TextUtils.isEmpty(region))
            tv_time_zone.text = region
    }

    override fun permissionAllGranted() {
        showCameraPopup()
    }

    override fun permissionDenied(permission: String) {
        requestPermission(arrayOf(permission))
    }

    override fun logout() {
        saveUser(null)
        App.data.clear()
        jumpActivity(GuideActivity::class.java)
        App.data.activityList.forEach {
            if (it !is GuideActivity) {
                it.finish()
            }
        }
        App.data.activityList.clear()
    }

    override fun showAvatar(imageUrl: String) {
        ImageManager.setImagePath(
            this,
            iv_avatar,
            imageUrl,
            R.mipmap.image_default_portrait
        )
    }

    override fun showNick(nick: String) {
        tv_nick.text = nick
    }

    override fun uploadFail(message: String) {
        T.show(message)
    }

    override fun showUserInfo() {
        tv_nick.text = App.data.userInfo.NickName
        tv_user_id.text = App.data.userInfo.UserID
        if (!TextUtils.isEmpty(App.data.userInfo.Avatar)) {
            showAvatar(App.data.userInfo.Avatar)
        }
        presenter.getUserSetting()
    }

    override fun showTemperatureUnit(unit: String) {
        if (unit == getString(R.string.celsius))
            tv_temperature_unit.text = getString(R.string.celsius_unit)
        else if (unit == getString(R.string.fahrenheit))
            tv_temperature_unit.text = getString(R.string.fahrenheit_unit)
    }

    override fun showUserSetting() {
        showTemperatureUnit(App.data.userSetting.TemperatureUnit)
        showRegion(App.data.userSetting.Region)
    }

    override fun onBackPressed() {
        editPopupWindow?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        commonPopupWindow?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        popupWindow?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE -> {// 头像上传
                if (data != null) {
                    val list = ImageSelectorUtils.getImageSelectorList(requestCode, resultCode, data)
                    if (list != null && list.size > 0) {
                        list.forEach {
                            L.e("配图:$it")
                            presenter.upload(this, it)
                        }
                    }
                }
            }
            TIMEZONE_REQUESTCODE -> {// 选择时区

            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        if (v is TextView) {
            AppInfoUtils.copy(this@UserInfoActivity, v.text.toString())
            T.show(getString(R.string.copy))
        }
        return true
    }

    private fun showTemperatureDialog() {
        bottomDialog.setContentView(temperatureDialogView)
        val params = temperatureDialogView.layoutParams as MarginLayoutParams
        params.width = resources.displayMetrics.widthPixels - dp2px(8)
        params.bottomMargin = dp2px(5)
        temperatureDialogView.layoutParams = params
        bottomDialog.setCanceledOnTouchOutside(true)
        bottomDialog.window?.setGravity(Gravity.BOTTOM)
        bottomDialog.window?.setWindowAnimations(R.style.BottomDialog_Animation)
        bottomDialog.show()
    }

}
