package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.Fragment
import com.alibaba.fastjson.JSONObject
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.android.tpush.XGIOperateCallback
import com.tencent.android.tpush.XGPushConfig
import com.tencent.android.tpush.XGPushManager
import com.tencent.iot.explorer.link.BuildConfig
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.customview.dialog.ProgressDialog
import com.tencent.iot.explorer.link.customview.dialog.UpgradeDialog
import com.tencent.iot.explorer.link.customview.dialog.UpgradeInfo
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.utils.FileUtils
import com.tencent.iot.explorer.link.customview.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.customview.home.BottomItemEntity
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.fragment.CommentFragment
import com.tencent.iot.explorer.link.kitlink.fragment.HomeFragment
import com.tencent.iot.explorer.link.kitlink.fragment.MeFragment
import com.tencent.iot.explorer.link.kitlink.fragment.SmartFragment
import com.tencent.iot.explorer.link.kitlink.popup.FamilyListPopup
import com.tencent.iot.explorer.link.kitlink.util.DateUtils
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.tpns.baseapi.XGApiConfig
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.system.exitProcess

/**
 * main页面
 */
class MainActivity : PActivity(), MyCallback {
    private var previousPosition = 0

    private val fragments = arrayListOf<Fragment>()

    private var familyPopup: FamilyListPopup? = null
//    private var addDialog: ListOptionsDialog? = null

    private var isForceUpgrade = true

    private var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun getContentView(): Int {
        return R.layout.activity_main
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun onResume() {
        super.onResume()
        login(this)
        val cancelAccountTime = SharePreferenceUtil.getLong(this, App.CONFIG, CommonField.CANCEL_ACCOUNT_TIME)
        if (cancelAccountTime > 0) {
            showCancelAccountStoppedDialog(cancelAccountTime)
        }
        if (isForceUpgrade) {
            startUpdateApp()
        }
    }

    private fun startUpdateApp() {
        HttpRequest.instance.getLastVersion(object: MyCallback{
            override fun fail(msg: String?, reqCode: Int) {
                T.show(msg)
            }
            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    val json = response.data as JSONObject
                    val info = UpgradeInfo.convertJson2UpgradeInfo(json)
                    if (App.needUpgrade(info!!.version) && info.upgradeType != 2 && !UpgradeDialog.dialogShowing()) {
                        isForceUpgrade = info.upgradeType == 1 // 2:静默更新不提示 1:强制升级 0:用户确认
                        if (isForceUpgrade || (!isForceUpgrade && !UpgradeDialog.dialogShowed())) {
                            val dialog = UpgradeDialog(this@MainActivity, info)
                            dialog.setOnDismisListener(upgradeDialogListener)
                            dialog.show()
                        }
                    }
                }
            }
        })
    }

    private var upgradeDialogListener =
        UpgradeDialog.OnDismisListener { url ->
            val dialog = ProgressDialog(this@MainActivity, url)
            dialog.setOnDismisListener(downloadListener)
            dialog.show()
        }

    private var downloadListener = object: ProgressDialog.OnDismisListener {
        override fun onDownloadSuccess(path: String) {
            FileUtils.installApk(this@MainActivity, path)
        }
        override fun onDownloadFailed() {
            T.show(resources.getString(R.string.download_failed))
        }
        override fun onDownloadProgress(currentProgress: Int, size: Int) { }
    }

    override fun initView() {
        val userId = SharePreferenceUtil.getString(this@MainActivity, App.CONFIG, CommonField.USER_ID)
        FirebaseCrashlytics.getInstance().setUserId(userId)
        FirebaseAnalytics.getInstance(this).setUserId(userId)
        openXGPush()
        requestPermission(permissions)
        home_bottom_view.addMenu(
            BottomItemEntity(
                getString(R.string.main_tab_1),
                resources.getColor(R.color.main_tab_normal), resources.getColor(R.color.main_tab_hover),
                R.mipmap.main_tab_1_normal, R.mipmap.main_tab_1_hover
            )
        )

            .addMenu(
                BottomItemEntity(
                    getString(R.string.main_tab_5),
                    resources.getColor(R.color.main_tab_normal), resources.getColor(R.color.main_tab_hover),
                    R.mipmap.smart_unpressed, R.mipmap.smart_pressed
                )
            )
            .addMenu(
                BottomItemEntity(
                    getString(R.string.main_tab_4),
                    resources.getColor(R.color.main_tab_normal), resources.getColor(R.color.main_tab_hover),
                    R.mipmap.commet_unpressed, R.mipmap.commet_pressed
                )
            )
            .addMenu(
                BottomItemEntity(
                    getString(R.string.main_tab_3),
                    resources.getColor(R.color.main_tab_normal), resources.getColor(R.color.main_tab_hover),
                    R.mipmap.main_tab_3_normal, R.mipmap.main_tab_3_hover
                )
            ).showMenu()

        fragments.clear()
        fragments.add(HomeFragment())
        fragments.add(SmartFragment())
        fragments.add(CommentFragment())
        fragments.add(MeFragment())
        this.supportFragmentManager.beginTransaction()
            .add(R.id.main_container, fragments[0])
            .show(fragments[0])
            .commit()
    }

    override fun setListener() {
        iv_main_add.setOnClickListener{}

        home_bottom_view.setOnItemClickListener { _, position, previewPosition ->
            showFragment(position)
        }
        (fragments[0] as? HomeFragment)?.run {
            popupListener = object : HomeFragment.PopupListener {
                override fun onPopupListener(familyList: List<FamilyEntity>) {
                    this@MainActivity.showFamilyPopup(familyList)
                }
            }
        }
    }

    private fun openXGPush() {
        XGPushConfig.init(applicationContext)
        if (App.data.regionId == "1") {// 中国大陆
            XGPushConfig.setAccessId(applicationContext, BuildConfig.XgAccessId.toLong())
            XGPushConfig.setAccessKey(applicationContext, BuildConfig.XgAccessKey)
            XGApiConfig.setServerSuffix(applicationContext, CommonField.XG_ACCESS_POINT_CHINA)
        } else if (App.data.regionId == "22") {// 美国
            XGPushConfig.setAccessId(applicationContext, BuildConfig.XgUSAAccessId.toLong())
            XGPushConfig.setAccessKey(applicationContext, BuildConfig.XgUSAAccessKey)
            XGApiConfig.setServerSuffix(applicationContext, CommonField.XG_ACCESS_POINT_USA)
        }
        XGPushManager.registerPush(applicationContext, object : XGIOperateCallback {
            override fun onSuccess(data: Any?, p1: Int) {
                L.e("注册成功，设备token为：$data")
                data?.let {
                    App.data.xg_token = it.toString()
                    bindXG()
                }
            }

            override fun onFail(data: Any?, errCode: Int, msg: String?) {
                L.e("注册失败，错误码：$errCode ,错误信息：$msg")
            }
        })
    }

    /**
     * 绑定信鸽推送
     */
    private fun bindXG() {
        if (TextUtils.isEmpty(App.data.xg_token)) return
        HttpRequest.instance.bindXG(App.data.xg_token, this)
    }

    /**
     * 解绑信鸽推送
     */
    private fun unbindXG() {
        if (TextUtils.isEmpty(App.data.xg_token)) return
        HttpRequest.instance.unbindXG(App.data.xg_token, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
    }

    private fun showFragment(position: Int) {
        val transaction = this.supportFragmentManager.beginTransaction()
        if (fragments[position].isAdded) {
            transaction.show(fragments[position]).hide(fragments[previousPosition]).commit()
        } else {
            transaction.add(R.id.main_container, fragments[position])
                .show(fragments[position]).hide(fragments[previousPosition])
                .commit()
        }
        previousPosition = position
    }

    private fun showFamilyPopup(familyList: List<FamilyEntity>) {
        if (familyPopup == null) {
            familyPopup = FamilyListPopup(this)
            familyPopup?.setList(familyList)
        }
        familyPopup?.setBg(main_bg)
        familyPopup?.show(main)
        familyPopup?.onItemClickListener = object : FamilyListPopup.OnItemClickListener {
            override fun onItemClick(popupWindow: FamilyListPopup, position: Int) {
                popupWindow.dismiss()
                (fragments[0] as? HomeFragment)?.run {
                    tabFamily(position)
                }
            }
        }
        familyPopup?.setOnClickManagerListener(View.OnClickListener {
            jumpActivity(FamilyListActivity::class.java)
            familyPopup?.dismiss()
        })
    }


    override fun onDestroy() {
        unbindXG()
        super.onDestroy()
    }

    private var timestamp = 0L

    /**
     * 连续按两个关闭app
     */
    override fun onBackPressed() {
        val t = System.currentTimeMillis()
        if (t - timestamp < 1000) {
            exitApp()
            App.data.clear()
            exitProcess(0)
        } else {
            timestamp = t
            T.show(getString(R.string.tap_more_exit)) //再按一下退出应用
        }
    }

    private fun showCancelAccountStoppedDialog(time: Long){
        var content = getString(R.string.cancel_account_stopped_content)
        val cancelAccountTime = DateUtils.getFormatDate(Date(time*1000))
        content = content.replace("date", cancelAccountTime)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.cancel_account_stopped_title)
            .setMessage(content)
            .setCancelable(false)
            .setPositiveButton(R.string.have_known,
                DialogInterface.OnClickListener { dialog, id ->
                    SharePreferenceUtil.saveLong(this, App.CONFIG, CommonField.CANCEL_ACCOUNT_TIME, 0)
                })
        builder.create()
        builder.show()
    }
}
