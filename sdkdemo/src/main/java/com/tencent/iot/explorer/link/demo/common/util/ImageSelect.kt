package com.tencent.iot.explorer.link.demo.common.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


object ImageSelect {

    const val GALLERY = 10 // 图库选取图片标识请求码
    const val CLIP = 12 // 裁剪图片标识请求码
    const val CAMERA = 13 // 裁剪图片标识请求码

    private val fileList = LinkedList<File>()

    /**
     * 打开图库
     */
    fun showSingle(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(activity.packageManager) != null) {
            // 以startActivityForResult的方式启动一个activity用来获取返回的结果
            activity.startActivityForResult(intent, GALLERY)
        } else {
            Toast.makeText(activity, "未找到图片管理器", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 打开相机
     */
    fun showCamera(activity: Activity) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity.packageManager) != null) {
            // 以startActivityForResult的方式启动一个activity用来获取返回的结果
            activity.startActivityForResult(intent, CAMERA)
        } else {
            Toast.makeText(activity, "相机不可用", Toast.LENGTH_LONG).show()
        }
    }

    /***
     * 裁剪图片
     */
    fun clipImage(activity: Activity, uri: Uri, width: Int, height: Int) {

        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")

        // 设置裁剪
        intent.putExtra("crop", "true")
        intent.putExtra("scale", true)
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", width)
        intent.putExtra("outputY", height)

        intent.putExtra("return-data", false)
        intent.putExtra("noFaceDetection", true)

        // 创建文件保存裁剪的图片
        val imageFile = File(
            activity.applicationContext.cacheDir.path, "${System.currentTimeMillis()}.jpg"
        )
        fileList.add(imageFile)
        val imageUri = Uri.fromFile(imageFile)
        if (imageUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        }
        activity.startActivityForResult(intent, CLIP)

    }

    /**
     * user转换为file文件
     *返回值为file类型
     * @param uri
     * @return
     */
    fun uri2File(activity: Activity, uri: Uri): File {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        activity.managedQuery(uri, projection, null, null, null).run {
            val index = getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            moveToFirst()
            return File(getString(index))
        }
    }

    fun saveBitmap(bitmap: Bitmap): File {
        return saveBitmap(null, bitmap)
    }

    fun saveBitmap(context: Context?, bitmap: Bitmap): File {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath, "${System.currentTimeMillis()}.jpg")
        return saveBitmap(context, bitmap, file.absolutePath)
    }

    fun saveBitmap(context: Context?, bitmap: Bitmap, path: String): File {
        val file = File(path)
        BufferedOutputStream(FileOutputStream(file)).run {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, this)
            flush()
            close()
        }
        fileList.add(file)

        context?.let {
            if (!file.exists()) return@let
            MediaStore.Images.Media.insertImage(it.getContentResolver(), file.absolutePath, file.name, null)
            it.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
        }
        return file
    }

    /**
     * 清除剪切图片
     */
    fun clearImage() {
        try {
            while (fileList.isNotEmpty()) {
                if (fileList.first.exists()) {
                    fileList.first.delete()
                    fileList.removeFirst()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}