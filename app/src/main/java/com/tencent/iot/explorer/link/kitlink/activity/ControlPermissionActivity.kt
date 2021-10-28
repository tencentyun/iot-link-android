package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.adapter.PermissionsAdapter
import com.tencent.iot.explorer.link.kitlink.entity.PermissionAccessInfo
import kotlinx.android.synthetic.main.activity_permissions.*
import kotlinx.android.synthetic.main.activity_select_point.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class ControlPermissionActivity : BaseActivity() {
    private var REQUEST_CODE = 0x1110
    private var permissionsList = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.WRITE_SETTINGS)

    private var permissionsData: MutableList<PermissionAccessInfo> = ArrayList()
    private var adapter: PermissionsAdapter? = null

    override fun getContentView(): Int {
        return R.layout.activity_permissions
    }

    override fun onResume() {
        super.onResume()
        // 刷新当前的权限列表，避免用户手动进入到后台，调整权限的情况
        if (permissionsData.isEmpty()) return
        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(this@ControlPermissionActivity);
        permissionsData[0].permissionAccessed = notificationManager.areNotificationsEnabled()
        for (i in 1 until permissionsData.size) {
            permissionsData[i].permissionAccessed =
                ActivityCompat.checkSelfPermission(this, permissionsData[i].permission) == PackageManager.PERMISSION_GRANTED
        }
        adapter?.notifyDataSetChanged()
    }

    override fun initView() {
        tv_title.setText(R.string.controller_of_permission)
        checkPermissionsInfo(permissionsList)

        var linearLayoutManager = LinearLayoutManager(this@ControlPermissionActivity)
        adapter = PermissionsAdapter(permissionsData)
        lv_permissions.layoutManager = linearLayoutManager
        lv_permissions.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        adapter?.setOnItemClicked(onItemClicked)
    }

    private var onItemClicked = object: PermissionsAdapter.OnItemActionListener {
        override fun onItemSwitched(pos: Int, permissionAccessInfo: PermissionAccessInfo) {
            if (pos == 0) { //通知
                val intent: Intent = Intent()
                try {
                    intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS

                    //8.0及以后版本使用这两个extra.  >=API 26
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, applicationInfo.uid)

                    //5.0-7.1 使用这两个extra.  <= API 25, >=API 21
                    intent.putExtra("app_package", packageName)
                    intent.putExtra("app_uid", applicationInfo.uid)

                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()

                    //其他低版本或者异常情况，走该节点。进入APP设置界面
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.putExtra("package", packageName)

                    //val uri = Uri.fromParts("package", packageName, null)
                    //intent.data = uri
                    startActivity(intent)
                }
                return
            }
            if (!permissionAccessInfo.permissionAccessed) {  // 没有对应的权限，尝试开启权限
                ActivityCompat.requestPermissions(this@ControlPermissionActivity, arrayOf(permissionAccessInfo.permission), REQUEST_CODE)
                return
            }
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, REQUEST_CODE) // 申请权限返回执行
        }
    }

    private fun checkPermissionsInfo(permissions: Array<String>) {
        permissionsData.add(checkNotificationPermissions())
        val pm = this@ControlPermissionActivity.packageManager
        for (permission in permissions) {
            var accessInfo = PermissionAccessInfo()
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                accessInfo.permissionAccessed = true
            }

            try {
                var permissionInfo = pm.getPermissionInfo(permission, 0)
                accessInfo.permissionName = permissionInfo.loadLabel(pm).toString()
                accessInfo.permission = permission
            } catch (e: Exception) {
                e.printStackTrace()
            }
            permissionsData.add(accessInfo)
        }
    }

    private fun checkNotificationPermissions() : PermissionAccessInfo {
        var accessInfo = PermissionAccessInfo()
        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(this@ControlPermissionActivity);
        accessInfo.permissionAccessed = notificationManager.areNotificationsEnabled()
        accessInfo.permissionName = "通知管理"
        return accessInfo
    }

}