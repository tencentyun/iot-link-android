package com.tencent.iot.explorer.link.core.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File


object PhotoUtils {

    const val RESULT_CODE_CAMERA = 0x02
    const val RESULT_CODE_PHOTO = 0x04
    const val PIC_NAME = "/temp.png"

    lateinit var PATH_PHOTO: String

    /**
    * 拍照
    * @param context Activity
    */
    fun startCamera(context: Activity) {
        PATH_PHOTO = FileUtils.getSdCardDirectory(context) + PIC_NAME
        val temp = File(PATH_PHOTO)

        if (!temp.parentFile.exists()) temp.parentFile.mkdirs()
        if (temp.exists()) temp.delete()

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            // 通过FileProvider创建一个content类型的Uri
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", temp)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(temp))
        }
        context.startActivityForResult(intent,
            RESULT_CODE_CAMERA
        )
    }

    /**
     * 打开相册
     * @param context Activity
     */
    fun startAlbum(context: Activity) {
        val albumIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        albumIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        context.startActivityForResult(albumIntent,
            RESULT_CODE_PHOTO
        )
    }
}

