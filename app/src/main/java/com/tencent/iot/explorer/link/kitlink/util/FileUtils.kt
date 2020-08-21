package com.tencent.iot.explorer.link.kitlink.util

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


object FileUtils {

    // 默认的暂存文件夹路径
    var folderName = "h5pic"
    var tempFolderName = "kitlink/apk/"
    var tempFileName = "/temp.png"

    fun getSdCardTempDirectory(context: Context): String {
        var sdDir: File?
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            sdDir = Environment.getExternalStorageDirectory()
        } else {
            sdDir = context.cacheDir
        }
        val cacheDir = File(sdDir, tempFolderName)

        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir.path
    }

    fun getSdCardDirectory(context: Context): String {
        var sdDir: File?
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            sdDir = Environment.getExternalStorageDirectory()
        } else {
            sdDir = context.cacheDir
        }
        val cacheDir = File(sdDir, folderName)

        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir.path
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().path + "/" + split[1]
                }

            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                return getDataColumn(context, contentUri, null, null)

            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }

        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = uri?.let { context.contentResolver.query(it, projection, selection, selectionArgs, null) }
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor?.getString(columnIndex)
            }
        } finally {
            if (cursor != null) cursor!!.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * 压缩图片（质量压缩）
     *
     * @param image
     */
    fun compressImage(image: Bitmap): File? {
        try {
            ByteArrayOutputStream().use {
                var tmp = it
                image.compress(Bitmap.CompressFormat.JPEG, 100, it) //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
                var options = 100
                while (it.toByteArray().size / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
                    it.reset() //重置baos即清空baos
                    options -= 10 //每次都减少10
                    image.compress(Bitmap.CompressFormat.JPEG, options, it) //这里压缩options%，把压缩后的数据存放到baos中

                    // 当压缩比例小于 10% 的时候，跳出循环压缩
                    if (options <= 10) break
                }

                var path = Environment.getExternalStorageDirectory().toString() + this.tempFileName
                val file = File(path)
                FileOutputStream(file).use {
                    it.write(tmp.toByteArray())
                    it.flush()
                }
                return file
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun installApk(context: Context, path: String) {
        try {
            val i = Intent(Intent.ACTION_VIEW)
            var file = File(path)
            var data: Uri
            // 判断版本大于等于7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                data = FileProvider.getUriForFile(context, context.packageName +".fileprovider", file);
                // 给目标应用一个临时授权
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                data = Uri.fromFile(file);
            }
            i.setDataAndType(data, "application/vnd.android.package-archive");
            context.startActivity(i)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}