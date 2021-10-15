package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.adapter.PermissionsAdapter
import com.tencent.iot.explorer.link.kitlink.entity.PermissionAccessInfo
import kotlinx.android.synthetic.main.activity_permissions.*
import kotlinx.android.synthetic.main.activity_select_point.*
import kotlinx.android.synthetic.main.menu_back_layout.*


class ControlPermissionActivity : BaseActivity() {
    private var REQUEST_CODE = 1110
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
            Log.e("XXX", "aaa " + permissionAccessInfo.permission)
            if (!permissionAccessInfo.permissionAccessed) {  // 没有对应的权限，尝试开启权限
                ActivityCompat.requestPermissions(this@ControlPermissionActivity, arrayOf(permissionAccessInfo.permission), REQUEST_CODE)
                return
            }
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, 1) // 申请权限返回执行
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    var index = indexOfPermissions(permissions[i])
                    if (index < 0) continue
                    permissionsData[index].permissionAccessed = true
                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun indexOfPermissions(permission: String): Int {
        for (i in permissionsList.indices) {
            if (permission == permissionsList[i]) {
                return i
            }
        }
        return -1
    }

    private fun checkPermissionsInfo(permissions: Array<String>) {
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

}