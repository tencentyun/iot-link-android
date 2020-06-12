package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tencent.iot.explorer.link.kitlink.App
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.User
import com.tencent.iot.explorer.link.kitlink.util.DataHolder
import com.tencent.iot.explorer.link.util.SharePreferenceUtil
import com.tencent.iot.explorer.link.util.T
import com.view.status.StatusBarUtil
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
            if (App.data.activityList.last is MainActivity) {
                stop = true
            } else {
                App.data.activityList.last.finish()
                App.data.activityList.removeLast()
            }
        }
    }

    fun backTo(level: Int) {
        if (level <= 0) return
        for (i in 1..level) {
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
        user?.let {
            expireAt = it.ExpireAt
            token = it.Token
        }
        SharePreferenceUtil.saveLong(this, App.CONFIG, CommonField.EXPIRE_AT, expireAt)
        SharePreferenceUtil.saveString(this, App.CONFIG, CommonField.TOKEN, token)
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
                Log.e("lurs", p + "被拒绝")
                return false
            }
            Log.e("lurs", p + "已经申请成功")
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
}
