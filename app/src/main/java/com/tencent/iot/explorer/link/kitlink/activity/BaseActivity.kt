package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.DataHolder
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.User
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.customview.dialog.TipShareDevDialog
import com.tencent.iot.explorer.link.customview.status.StatusBarUtil
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.ProductEntity
import com.tencent.iot.explorer.link.kitlink.entity.ProductsEntity
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import java.util.*

/**
 * baseActivity
 */
abstract class BaseActivity : AppCompatActivity() {

    /**
     * 数据共享角色
     */
    private val role by lazy {
        DataHolder.instance.register(this)
    }
    /**
     * 存放数据
     */
    fun put(key: String, any: Any) {
        role.put(key, any)
    }

    /**
     * 更新数据，不存在的key无法更新
     */
    fun update(key: String, any: Any) {
        role.update(key, any)
    }

    /**
     * 移除数据
     */
    fun remove(key: String) {
        role.remove(key)
    }

    /**
     * 获得数据
     */
    fun <T> get(key: String): T? {
        return role.get(key)
    }

    /**
     * 放弃某个字段的管理权
     */
    fun giveUp(key: String) {
        role.giveUp(key)
    }

    /**
     * 获取某个字段的管理权
     */
    fun pickUp(key: String) {
        role.pickUp(key)
    }

    fun login(activity: MainActivity) {
        App.data.activityList.forEach {
            if (it !is MainActivity) {
                it.finish()
            }
        }
        App.data.activityList.clear()
        App.data.activityList.add(activity)
    }

    fun logout(activity: LoginActivity) {
        App.data.activityList.forEach {
            if (it !is LoginActivity) {
                it.finish()
            }
        }
        App.data.activityList.clear()
        App.data.activityList.add(activity)
    }

    fun backToMain() {
        var stop = false
        while (!stop) {
            if (!App.data.activityList.isEmpty()) {
                if (App.data.activityList.last is MainActivity) {
                    stop = true
                } else {
                    App.data.activityList.last.finish()
                    App.data.activityList.removeLast()
                }
            }
        }
    }

    fun backTo(level: Int) {
        if (level <= 0) return
        for (i in 1..level) {
            if (App.data.activityList.isEmpty()) return
            App.data.activityList.last.finish()
            App.data.activityList.removeLast()
        }
    }

    fun exitApp() {
        App.data.activityList.forEach {
            it.finish()
        }
        App.data.activityList.clear()
    }

    fun saveUser(user: User?) {
        var expireAt = 0L
        var token = ""
        var cancelAccountTime = 0L
        user?.let {
            expireAt = it.ExpireAt
            token = it.Token
            cancelAccountTime = it.CancelAccountTime
        }
        SharePreferenceUtil.saveLong(this, App.CONFIG, CommonField.EXPIRE_AT, expireAt)
        SharePreferenceUtil.saveString(this, App.CONFIG, CommonField.TOKEN, token)
        SharePreferenceUtil.saveLong(this, App.CONFIG, CommonField.CANCEL_ACCOUNT_TIME, cancelAccountTime)
    }

    fun getMyColor(colorRes: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(colorRes, null)
        } else {
            resources.getColor(colorRes)
        }
    }

    abstract fun getContentView(): Int

    private val language: String get() = this.resources.configuration.locale.language

    abstract fun initView()

    abstract fun setListener()

    open fun startHere() {
        initView()
        setListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        checkLanguage()
        super.onCreate(savedInstanceState)
        super.setContentView(getContentView())
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        App.data.activityList.addLast(this)
        //在setContentView()后调用
        checkStyle()
        startHere()
    }

    override fun onResume() {
        App.activity = this
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        App.data.activityList.remove(this)
        //清除管理权限内的DataHolder中存放的数据
        DataHolder.instance.unregister(this)
    }

    private fun checkStyle() {
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, false)
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this)
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x55000000)
        }
    }

    private fun checkLanguage() {
        if (TextUtils.isEmpty(App.language))
            setAutoLanguage()
        else
            setLanguage()
    }

    private fun setAutoLanguage() {
        val configuration = this.resources.configuration
        val displayMetrics = this.resources.displayMetrics
        if (language == "zh") {
            configuration.locale = Locale.SIMPLIFIED_CHINESE
        } else {
            configuration.locale = Locale.ENGLISH
        }
        this.resources.updateConfiguration(configuration, displayMetrics)
    }

    private fun setLanguage() {
        val configuration = this.resources.configuration
        val displayMetrics = this.resources.displayMetrics
        if (App.language == "zh") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(Locale.SIMPLIFIED_CHINESE)
            } else {
                configuration.locale = Locale.SIMPLIFIED_CHINESE
            }
        } else {
            configuration.locale = Locale.ENGLISH
        }
        this.resources.updateConfiguration(configuration, displayMetrics)
    }

    fun saveLanguage(language: String) {
        val editor = this.getSharedPreferences("config", Context.MODE_PRIVATE).edit()
        editor.putString("language", language)
        editor.commit()
    }

    protected fun checkPermissions(permissions: Array<String>): Boolean {
        for (p in permissions) {
            if (ActivityCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_DENIED) {
                L.e(p + "被拒绝")
                return false
            }
            L.e(p + "已经申请成功")
        }
        return true
    }

    protected fun requestPermission(permissions: Array<String>) {
        ActivityCompat.requestPermissions(this, permissions, 102)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 102) {
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionDenied(permissions[i])
                    return
                }
            }
            permissionAllGranted()
        }
    }

    open fun jumpActivity(clazz: Class<*>) {
        jumpActivity(clazz, false)
    }

    open fun jumpActivity(clazz: Class<*>, finish: Boolean) {
        startActivity(Intent(this, clazz))
        if (finish) finish()
    }

    /**
     * 申请的权限都已经通过
     */
    open fun permissionAllGranted() {

    }

    /**
     * 申请的权限都已经通过
     */
    open fun permissionDenied(permission: String) {

    }

    fun show(text: String?) {
        if (TextUtils.isEmpty(text)) return
        runOnUiThread {
            T.show(text)
        }
    }

    fun dp2px(dp: Int): Int {
        return (resources.displayMetrics.density * dp + 0.5).toInt()
    }

    fun jumpAddDevActivity() {
        var jsonStr = Utils.getStringValueFromXml(T.getContext(), CommonField.COUNTRY_CODE,
            CommonField.COUNTRY_CODE)
        if (TextUtils.isEmpty(jsonStr) || jsonStr == "{}") {
            jumpActivity(DeviceCategoryActivity::class.java)
            return
        }

        var json = JSONObject.parseObject(jsonStr)
        if (json != null && json.containsKey(CommonField.COUNTRY_CODE) && json.getString(CommonField.COUNTRY_CODE) == "1") {
            var agreeStr = Utils.getStringValueFromXml(T.getContext(), CommonField.AGREE_TAG, CommonField.AGREE_TAG)
            if (TextUtils.isEmpty(agreeStr) || !agreeStr.equals(CommonField.AGREED_TAG)) {
                var dlg = TipShareDevDialog(this)
                dlg.show()
                dlg.setOnDismisListener {
                    Utils.setXmlStringValue(T.getContext(), CommonField.AGREE_TAG, CommonField.AGREE_TAG, CommonField.AGREED_TAG)
                    jumpActivity(DeviceCategoryActivity::class.java)
                }
                return
            }
        }

        jumpActivity(DeviceCategoryActivity::class.java)
    }

    interface OnTypeGeted {
        fun onType(type: String)
    }

    fun getDeviceType(productId: String, onTypeGeted: OnTypeGeted) {
        var productsList = arrayListOf(productId)
        HttpRequest.instance.deviceProducts(productsList, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {}

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    if (TextUtils.isEmpty(response.data.toString())) return

                    var products = JSON.parseObject(response.data.toString(), ProductsEntity::class.java)
                    products?.Products?.let {
                        var product = JSON.parseObject(it.getString(0), ProductEntity::class.java)
                        product?.let {
                            onTypeGeted?.onType(it.NetType)
                        }
                    }
                }
            }
        })
    }
}
