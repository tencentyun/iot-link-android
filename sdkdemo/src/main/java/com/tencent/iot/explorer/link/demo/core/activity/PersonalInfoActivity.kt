package com.tencent.iot.explorer.link.demo.core.activity

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.text.TextUtils
import com.tencent.iot.explorer.link.demo.core.popup.EditPopupWindow
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.demo.core.upload.UploadCallback
import com.tencent.iot.explorer.link.core.auth.consts.RequestCode
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.log.L.ld
import com.tencent.iot.explorer.link.demo.common.log.L.le
import com.tencent.iot.explorer.link.demo.core.popup.CameraPopupWindow
import com.tencent.iot.explorer.link.demo.core.popup.CommonPopupWindow
import com.tencent.iot.explorer.link.demo.core.response.UserInfoResponse
import com.tencent.iot.explorer.link.demo.core.upload.UploadImpl
import com.tencent.iot.explorer.link.demo.core.upload.UploadService
import com.tencent.iot.explorer.link.demo.common.util.ImageSelect
import com.tencent.iot.explorer.link.demo.databinding.ActivityPersonalInfoBinding
import java.io.File

/**
 * 个人信息
 */
class PersonalInfoActivity : BaseActivity<ActivityPersonalInfoBinding>(), MyCallback {

    private var popupWindow: CameraPopupWindow? = null
    private var editPopupWindow: EditPopupWindow? = null
    private var commonPopupWindow: CommonPopupWindow? = null
    private lateinit var uploadImpl: UploadImpl

    private var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun getViewBinding(): ActivityPersonalInfoBinding = ActivityPersonalInfoBinding.inflate(layoutInflater)

    override fun initView() {
        binding.menuPersonalInfo.tvTitle.text = getString(R.string.personal_info)
        IoTAuth.userImpl.userInfo(this)
        uploadImpl = UploadService()
    }

    override fun setListener() {
        with(binding) {
            menuPersonalInfo.ivBack.setOnClickListener { finish() }
            tvTitleNick.setOnClickListener {
                showEditPopup()
            }
            tvTitleTelephoneNumber.setOnClickListener {

            }
            tvTitleModifyPassword.setOnClickListener {
                if (TextUtils.isEmpty(App.data.userInfo.PhoneNumber)) {
                    showCommonPopup()
                } else {
                    jumpActivity(SetPasswordActivity::class.java)
                }
            }
            userInfoPortrait.setOnClickListener {
                if (checkPermissions(permissions))
                    showCameraPopup()
                else requestPermission(permissions)
            }
            tvUserInfoLogout.setOnClickListener {
                IoTAuth.userImpl.logout(this@PersonalInfoActivity)
            }
        }
    }

    /**
     * 上传图片
     */
    private fun uploadImage(file: File) {
        uploadImpl.uploadSingleFile(this, file.absolutePath, object :
            UploadCallback {
            override fun onSuccess(url: String, filePath: String, isOver: Boolean) {
                ld { "上传成功：$url" }
                modifyAvatar(url)
            }

            override fun onFail(filePath: String, isOver: Boolean) {
                ld { "上传失败：$filePath" }
            }
        })
    }

    private fun modifyAvatar(url: String) {
        IoTAuth.userImpl.modifyPortrait(url, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                ld { "头像修改失败" }
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                App.data.userInfo.Avatar = url
                showUserInfo()
            }
        })
    }

    private fun modifyNick(nick: String) {
        IoTAuth.userImpl.modifyAlias(nick, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                ld { "昵称修改失败" }
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                App.data.userInfo.NickName = nick
                showUserInfo()
            }
        })
    }

    private fun showUserInfo() {
        App.data.userInfo.let {
            binding.tvNick.text = it.NickName
            binding.tvTelephoneNumber.text = it.PhoneNumber
            if (!TextUtils.isEmpty(it.Avatar))
                Picasso.get().load(it.Avatar).into(binding.userInfoPortrait)
        }
    }

    private fun showCameraPopup() {
        if (popupWindow == null) {
            popupWindow = CameraPopupWindow(this)
        }
        popupWindow?.setBg(binding.personalInfoPopupBg)
        popupWindow?.show(binding.personalInfo)
    }

    private fun showCommonPopup() {
        if (commonPopupWindow == null) {
            commonPopupWindow = CommonPopupWindow(this)
        }
        commonPopupWindow?.setBg(binding.personalInfoPopupBg)
        commonPopupWindow?.setCommonParams(
            "请先绑定手机号",
            "当前未绑定手机号，无法进行修改密码"
        )
        commonPopupWindow?.setMenuText("", "绑定")
        commonPopupWindow?.onKeyListener = object : CommonPopupWindow.OnKeyListener {
            override fun cancel(popupWindow: CommonPopupWindow) {
                popupWindow.dismiss()
            }

            override fun confirm(popupWindow: CommonPopupWindow) {
                jumpActivity(BindMobilePhoneActivity::class.java)
                popupWindow.dismiss()
            }
        }
        commonPopupWindow?.show(binding.personalInfo)
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
                    show("请输入昵称")
                    return
                }
                modifyNick(text)
                editPopupWindow?.dismiss()
            }
        }
        editPopupWindow?.setBg(binding.personalInfoPopupBg)
        editPopupWindow?.show(binding.personalInfo)
    }

    override fun fail(msg: String?, reqCode: Int) {
        le { msg ?: "" }
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.user_info -> {
                    response.parse(UserInfoResponse::class.java)?.Data?.let {
                        App.data.userInfo.update(it)
                        showUserInfo()
                    }
                }
                RequestCode.logout -> logout()
            }
        } else {
            le { response.msg }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return
        when (requestCode) {
            ImageSelect.CAMERA -> {
                data.extras?.get("data")?.let {
                    (it as? Bitmap)?.run {
                        uploadImage(ImageSelect.saveBitmap(this))
                    }
                }
            }
            ImageSelect.GALLERY -> {
                data.data?.let {
                    uploadImage(ImageSelect.uri2File(this, it))
                }
            }
        }
    }

    /**
     * 退出登录
     */
    private fun logout() {
        while (App.data.activityList.isNotEmpty()) {
            //不是当前activity关闭
            if (App.data.activityList.first != this) {
                App.data.activityList.first.finish()
            }
            App.data.activityList.removeFirst()
        }
        App.data.clear()
        jumpActivity(LoginActivity::class.java, true)
    }
}
