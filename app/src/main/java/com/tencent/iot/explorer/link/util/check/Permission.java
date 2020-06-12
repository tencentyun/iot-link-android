package com.tencent.iot.explorer.link.util.check;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 6.0动态权限检查
 * Created by THINK on 2017/12/27.
 */

public class Permission {
    /**
     * 6.0动态权限：
     */
    public static final String[] ps = new String[]{"android.permission.CAMERA",
//            "android.permission.READ_CONTACTS",
//            "android.permission.ACCESS_FINE_LOCATION",
//            "android.permission.RECORD_AUDIO",
//            "android.permission.READ_PHONE_STATE",
            "android.permission.READ_EXTERNAL_STORAGE"};
    final public static int REQUEST_CODE_ASK_CALL_PHONE = 123;


    /**
     * 6.0权限申请：如果没有权限先申请
     */
    public static void requestPermission(Activity context) {
        if (Build.VERSION.SDK_INT >= 23) {
            for (String s : ps) {
                int checkCallPhonePermission = ContextCompat.checkSelfPermission(context, s);
                if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(context, new String[]{s}, REQUEST_CODE_ASK_CALL_PHONE);
                    return;
                }
            }
        }
    }

    /**
     * 6.0权限检测：如果没有就向跳转到系统设置
     *
     * @return
     */
    public static boolean checkPublishPermission(final Activity context) {
        if (Build.VERSION.SDK_INT >= 23) {
            for (String s : ps) {
                if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, s)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("权限开启提醒")
                            .setMessage("为了保证APP正常使用，请打开相应权限")
                            .setCancelable(false)
                            .setNegativeButton("退出应用", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //用户取消设置权限
                                    System.exit(0);
                                }
                            })
                            .setPositiveButton("前往开启", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Uri packageURI = Uri.parse("package:" + "com.kitlink");
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                                    context.startActivity(intent);
                                }
                            })
                            .show();
                    return false;
                }
            }

        }
        return true;
    }

}
