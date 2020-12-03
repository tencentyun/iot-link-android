package com.tencent.iot.explorer.link.core.demo.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tencent.iot.explorer.link.core.demo.App
import com.tencent.iot.explorer.link.core.demo.DataHolder

/**
 * baseActivity
 */
abstract class BaseActivity : AppCompatActivity() {

    val TAG: String by lazy {
        this.packageName.let {
            it.substring(it.lastIndexOf("."), it.lastIndex)
        }
    }

    /**
     * 数据共享角色
     */
    private val role by lazy {
        DataHolder.instance.register(this)
    }

    fun getMyColor(colorRes: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(colorRes, null)
        } else {
            resources.getColor(colorRes)
        }
    }

    abstract fun getContentView(): Int

    abstract fun initView()

    abstract fun setListener()

    open fun startHere() {
        initView()
        setListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(getContentView())
        App.data.activityList.add(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        //在setContentView()后调用
        startHere()
    }

    override fun onResume() {
        super.onResume()
        App.activity = this
    }

    /**
     * 检查权限
     */
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

    /**
     * 请求权限
     */
    protected fun requestPermission(permissions: Array<String>) {
        ActivityCompat.requestPermissions(this, permissions, 102)
    }

    /**
     * 权限申请结果回调
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
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

    /**
     * 快速跳转Activity
     */
    open fun jumpActivity(clazz: Class<*>) {
        jumpActivity(clazz, false)
    }

    /**
     * 快速跳转Activity,并选择关闭当前页面
     */
    open fun jumpActivity(clazz: Class<*>, finish: Boolean) {
        startActivity(Intent(this, clazz))
        if (finish) finish()
    }

    /**
     * 返回MainActivity
     */
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

    fun show(text: String?) {
        if (TextUtils.isEmpty(text)) return
        runOnUiThread {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
    }

    fun dp2px(dp: Int): Int {
        return (resources.displayMetrics.density * dp + 0.5).toInt()
    }

    /**
     * 页面销毁
     */
    override fun onDestroy() {
        App.data.activityList.remove(this)
        //清除管理权限内的DataHolder中存放的数据
        DataHolder.instance.unregister(this)
        super.onDestroy()
    }

}
