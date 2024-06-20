package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.popup.CameraPopupWindow
import com.tencent.iot.explorer.link.kitlink.popup.CommonPopupWindow
import com.tencent.iot.explorer.link.kitlink.popup.EditPopupWindow
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.UserInfoPresenter
import com.tencent.iot.explorer.link.mvp.view.UserInfoView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.customview.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.customview.dialog.PermissionDialog
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.EditNameValue
import com.tencent.iot.explorer.link.kitlink.util.picture.imageselectorbrowser.ImageSelectorActivity
import com.tencent.iot.explorer.link.kitlink.util.picture.imageselectorbrowser.ImageSelectorConstant.REQUEST_IMAGE
import com.tencent.iot.explorer.link.kitlink.util.picture.imp.ImageManager
import com.tencent.iot.explorer.link.kitlink.util.picture.imp.ImageSelectorUtils
import com.tencent.iot.explorer.link.kitlink.util.safe
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.dialog_temperature.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*


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

    private val counts = 5 //点击次数
    private val duration = 3 * 1000.toLong() //规定有效时间
    private val hits = LongArray(counts)

    private var optionDialog: ListOptionsDialog? = null
    private var permissionDialog: PermissionDialog? = null
    private var clickCamera = false
    private var clickAlbum = false

    companion object {
        const val TIMEZONE_REQUESTCODE = 100
    }

    private var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun getContentView(): Int {
        return R.layout.activity_user_info
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun initView() {
        presenter = UserInfoPresenter(this)
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
        iv_user_id_copy.setOnClickListener(this)
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
        tv_empty_area0.setOnClickListener(this)
        tv_empty_area.setOnClickListener(this)

        temperatureDialogView.tv_fahrenheit.setOnClickListener(this)
        temperatureDialogView.tv_celsius.setOnClickListener(this)
        temperatureDialogView.tv_cancel.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_user_id_copy -> {
                // 获取AndroidID，并保存至剪切板
                Utils.copy(this, App.data.userInfo.UserID)
                Toast.makeText(App.activity, "内容已复制", Toast.LENGTH_LONG).show()
            }
            tv_title_avatar, iv_avatar, iv_avatar_arrow -> {
                showCameraPopup()
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
                var intent = Intent(this, TimeZoneActivity::class.java);
                var bundle = Bundle()
                bundle.putString(CommonField.EXTRA_TIME_ZONE_INFO, tv_time_zone.text.toString())
                intent.putExtra(CommonField.EXTRA_TIME_ZONE_BUNDLE_TAG, bundle)
                startActivityForResult(intent, TIMEZONE_REQUESTCODE)
            }
            tv_empty_area0, tv_empty_area -> {// 连续点击五次复制AndroidID
                System.arraycopy(hits, 1, hits, 0, hits.size - 1)
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于duration，即连续5次点击
                hits[hits.size - 1] = SystemClock.uptimeMillis()
                if (hits[0] >= SystemClock.uptimeMillis() - duration) {
                    if (hits.size == 5) {
                        // 获取AndroidID，并保存至剪切板
                        Utils.copy(this, App.uuid)
                    }
                }
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
//        if (popupWindow == null) {
//            popupWindow = CameraPopupWindow(this)
//        }
//        popupWindow?.setBg(user_info_popup_bg)
//        popupWindow?.show(user_info)
        var options = ArrayList<String>()
        options.add(getString(R.string.take_photo))
        options.add(getString(R.string.select_local_album))
        optionDialog = ListOptionsDialog(this, options)
        optionDialog!!.show()
        optionDialog!!.setOnDismisListener {
            if (it == 0) {
                ImageSelectorUtils.show(this, ImageSelectorActivity.Mode.MODE_SINGLE, true, 1)
//                if (checkPermissions(arrayOf(Manifest.permission.CAMERA)))
//                    ImageSelectorUtils.show(this, ImageSelectorActivity.Mode.MODE_SINGLE, true, 1)
//                else {
//                    clickCamera = true
//                    // 查看请求camera权限的时间是否大于48小时
//                    var cameraJsonString = Utils.getStringValueFromXml(T.getContext(), CommonField.PERMISSION_CAMERA, CommonField.PERMISSION_CAMERA)
//                    var cameraJson: JSONObject? = JSONObject.parse(cameraJsonString) as JSONObject?
//                    val lasttime = cameraJson?.getLong(CommonField.PERMISSION_CAMERA)
//                    if (lasttime != null && lasttime > 0 && System.currentTimeMillis() / 1000 - lasttime < 48*60*60) {
//                        T.show(resources.getString(R.string.permission_of_camera_refuse))
//                        return@setOnDismisListener
//                    }
//                    permissionDialog = PermissionDialog(App.activity, R.mipmap.permission_camera ,getString(R.string.permission_camera_lips), getString(R.string.permission_camera_avatar))
//                    permissionDialog!!.show()
//                    requestPermission(arrayOf(Manifest.permission.CAMERA))
//
//                    // 记录请求camera权限的时间
//                    var json = JSONObject()
//                    json.put(CommonField.PERMISSION_CAMERA, System.currentTimeMillis() / 1000)
//                    Utils.setXmlStringValue(T.getContext(), CommonField.PERMISSION_CAMERA, CommonField.PERMISSION_CAMERA, json.toJSONString())
//                }
            } else if (it == 1) {
                if (checkPermissions(permissions))
                    ImageSelectorUtils.show(this, ImageSelectorActivity.Mode.MODE_MULTI, false, 1)
                else {
                    clickAlbum = true
                    // 查看请求album权限的时间是否大于48小时
                    var albumJsonString = Utils.getStringValueFromXml(T.getContext(), CommonField.PERMISSION_ALBUM, CommonField.PERMISSION_ALBUM)
                    var albumJson: JSONObject? = JSONObject.parse(albumJsonString) as JSONObject?
                    val lasttime = albumJson?.getLong(CommonField.PERMISSION_ALBUM)
                    if (lasttime != null && lasttime > 0 && System.currentTimeMillis() / 1000 - lasttime < 48*60*60) {
                        T.show(resources.getString(R.string.permission_of_album_refuse))
                        return@setOnDismisListener
                    }
                    permissionDialog = PermissionDialog(App.activity, R.mipmap.permission_album ,getString(R.string.permission_album_lips), getString(R.string.permission_album_avatar))
                    permissionDialog!!.show()
                    requestPermission(permissions)

                    // 记录请求album权限的时间
                    var json = JSONObject()
                    json.put(CommonField.PERMISSION_ALBUM, System.currentTimeMillis() / 1000)
                    Utils.setXmlStringValue(T.getContext(), CommonField.PERMISSION_ALBUM, CommonField.PERMISSION_ALBUM, json.toJSONString())
                }
            }
        }
    }

    private fun showEditPopup() {
//        if (editPopupWindow == null) {
//            editPopupWindow = EditPopupWindow(this)
//        }
//        editPopupWindow?.setShowData(
//            getString(R.string.nick),
//            App.data.userInfo.NickName
//        )
//        editPopupWindow?.onVerifyListener = object : EditPopupWindow.OnVerifyListener {
//            override fun onVerify(text: String) {
//                if (TextUtils.isEmpty(text)) {
//                    T.show(getString(R.string.input_nick_name)) //请输入昵称
//                    return
//                }
//                presenter.modifyNick(text)
//                editPopupWindow?.dismiss()
//            }
//        }
//        editPopupWindow?.setBg(user_info_popup_bg)
//        editPopupWindow?.show(user_info)

        var intent = Intent(this, EditNameActivity::class.java)
        var editNameValue = EditNameValue()
        editNameValue.name = App.data.userInfo.NickName
        editNameValue.title = getString(R.string.modify_nick)
        editNameValue.tipName = getString(R.string.nick)
        editNameValue.btn = getString(R.string.save)
        editNameValue.errorTip = getString(R.string.toast_alias_length)
        intent.putExtra(CommonField.EXTRA_INFO, JSON.toJSONString(editNameValue))
        startActivityForResult(intent, CommonField.EDIT_NAME_REQ_CODE)
    }

    private fun showRegion(region: String) {
        if (!TextUtils.isEmpty(region))
            tv_time_zone.text = region
    }

    override fun permissionAllGranted() {
        /*if (clickCamera) {
            clickCamera = false
            ImageSelectorUtils.show(this, ImageSelectorActivity.Mode.MODE_SINGLE, true, 1)
        } else */if (clickAlbum) {
            clickAlbum = false
            ImageSelectorUtils.show(this, ImageSelectorActivity.Mode.MODE_MULTI, false, 1)
        }
        permissionDialog?.dismiss()
        permissionDialog = null
    }

    override fun permissionDenied(permission: String) {
        if (permission.contains(Manifest.permission.CAMERA)) {
            T.show(resources.getString(R.string.permission_of_camera_refuse))
        } else if (permissions.contains(permission)) {
            T.show(resources.getString(R.string.permission_of_album_refuse))
        }
        permissionDialog?.dismiss()
        permissionDialog = null
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

        if (requestCode == CommonField.EDIT_NAME_REQ_CODE &&
            resultCode == Activity.RESULT_OK && data != null) {
            val extraInfo = data.getStringExtra(CommonField.EXTRA_TEXT).safe()
            presenter.modifyNick(extraInfo)
        }
    }

    override fun onLongClick(v: View?): Boolean {
        if (v is TextView) {
            Utils.copy(this@UserInfoActivity, v.text.toString())
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
