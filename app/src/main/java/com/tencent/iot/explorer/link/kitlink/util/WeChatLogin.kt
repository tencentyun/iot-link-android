package com.tencent.iot.explorer.link.kitlink.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.BuildConfig
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.wxapi.WXEntryActivity
import com.tencent.iot.explorer.link.util.T
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.openapi.WXAPIFactory


/**
 * 微信登录
 */
class WeChatLogin {

    private constructor()

    companion object {
        const val APP_ID = BuildConfig.WXAccessAppId
        private var login: WeChatLogin? = null

        fun getInstance(): WeChatLogin {
            if (login == null) {
                synchronized(this) {
                    if (login == null) {
                        login = WeChatLogin()
                    }
                    return login!!
                }
            }
            return login!!
        }
    }

    fun login(activity: Activity, listener: OnLoginListener) {
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        val api = WXAPIFactory.createWXAPI(activity, APP_ID, true)
        if (api.isWXAppInstalled) {
            // 将应用的appId注册到微信
            if (api.registerApp(APP_ID)) {
                WXEntryActivity.onLoginListener = listener
                val req = SendAuth.Req()
                req.scope = "snsapi_userinfo"
                req.state = "tenext"
                api?.sendReq(req)
            }
        } else {
            T.show(activity.resources.getString(R.string.not_wechat_client))
        }
    }

    fun bind(activity: Activity, listener: OnLoginListener) {
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        val api = WXAPIFactory.createWXAPI(activity, APP_ID, true)
        if (api.isWXAppInstalled) {
            // 将应用的appId注册到微信
            if (api.registerApp(APP_ID)) {
                WXEntryActivity.onLoginListener = listener
                val req = SendAuth.Req()
                req.scope = "snsapi_userinfo"
                req.state = "tenext"
                api?.sendReq(req)
            }
        } else {
            T.show(activity.resources.getString(R.string.not_wechat_client))
        }
    }

    interface OnLoginListener {
        fun onSuccess(reqCode: String)
        fun cancel()
        fun onFail(msg:String)
    }

    fun shareMiniProgram(context: Context, url: String, path: String, picPath: String) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        var wxApi = WXAPIFactory.createWXAPI(context, APP_ID)
        if (wxApi.isWXAppInstalled) {
            val miniProgramObj = WXMiniProgramObject()
            miniProgramObj.webpageUrl = url // 兼容低版本的网页链接

//            miniProgramObj.miniprogramType =
//                WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE // 正式版:0，测试版:1，体验版:2
            val msg = WXMediaMessage(miniProgramObj)
            msg.title = context.getString(R.string.app_name) // 小程序消息title
            miniProgramObj.userName = "gh_2aa6447f2b7c"
            miniProgramObj.path = path

            val bitmap: Bitmap = Picasso.get().load(picPath).get()

            if (bitmap == null) {
                bitmap?.recycle()
                return
            }

            val sendBitmap = Bitmap.createScaledBitmap(bitmap!!, 200, 200, true)
            bitmap?.recycle()
            msg.thumbData = Utils.bmpToByteArray(sendBitmap)
            val req = SendMessageToWX.Req()
            req.message = msg
            req.scene = SendMessageToWX.Req.WXSceneSession // 目前支持会话

            wxApi.sendReq(req)
        } else {
            T.show(context.resources.getString(R.string.not_wechat_client))
        }
    }

    fun sharehtml(context: Context, shareContent: String?) {
        var wxApi = WXAPIFactory.createWXAPI(context, APP_ID)
        if (wxApi.isWXAppInstalled) {
            wxApi.registerApp(APP_ID)
            val webpage = WXWebpageObject()
            webpage.webpageUrl = shareContent
            val msg = WXMediaMessage(webpage)
            msg.title = context.getString(R.string.app_name)
            msg.description = " "
            //这里替换一张自己工程里的图片资源
            val thumb =
                BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
            msg.setThumbImage(thumb)
            val req = SendMessageToWX.Req()
            req.transaction = System.currentTimeMillis().toString()
            req.message = msg
            req.scene = SendMessageToWX.Req.WXSceneSession
            wxApi.sendReq(req)
        } else {
            T.show(context.resources.getString(R.string.not_wechat_client))
        }
    }


}