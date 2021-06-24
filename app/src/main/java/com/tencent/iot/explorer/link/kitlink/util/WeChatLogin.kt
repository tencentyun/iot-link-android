package com.tencent.iot.explorer.link.kitlink.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.BuildConfig
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.kitlink.wxapi.WXEntryActivity
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

    /**
     * 分享小程序
     * context 分享使用的上下文
     * url 小程序依赖的链接
     * path 小程序跳转的页面
     * picPath 分享显示的图片
     */
    fun shareMiniProgram(context: Context, url: String, path: String, picPath: String) {
        if (TextUtils.isEmpty(url)) return

        var wxApi = WXAPIFactory.createWXAPI(context, APP_ID)
        if (wxApi.isWXAppInstalled) {
            Thread(Runnable {       // 需要在线程中同步的解析图片
                val miniProgramObj = WXMiniProgramObject()
                miniProgramObj.webpageUrl = url
                val msg = WXMediaMessage(miniProgramObj)
                msg.title = context.getString(R.string.app_name)
                miniProgramObj.userName = "gh_2aa6447f2b7c"
                miniProgramObj.path = path
                if (!TextUtils.isEmpty(picPath)) {
                    val bitmap: Bitmap = Picasso.get().load(picPath).get()
                    val sendBitmap = Bitmap.createScaledBitmap(bitmap!!, 200, 200, true)
                    bitmap?.recycle()
                    msg.thumbData = Utils.bmpToByteArray(sendBitmap)
                } else {
                    val bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
                    val sendBitmap = Bitmap.createScaledBitmap(bitmap!!, 200, 200, true)
                    bitmap?.recycle()
                    msg.thumbData = Utils.bmpToByteArray(sendBitmap)
                }

                val req = SendMessageToWX.Req()
                req.message = msg
                req.scene = SendMessageToWX.Req.WXSceneSession

                wxApi.sendReq(req)
            }).start()

        } else {
            T.show(context.resources.getString(R.string.not_install_wechat_client))
        }
    }

    /**
     * 分享网页
     * context 分享使用的上下文
     * shareContent 被分享的网页链接
     */
    fun sharehtml(context: Context, shareContent: String?) {
        var wxApi = WXAPIFactory.createWXAPI(context, APP_ID)
        if (wxApi.isWXAppInstalled) {
            wxApi.registerApp(APP_ID)
            val webpage = WXWebpageObject()
            webpage.webpageUrl = shareContent
            val msg = WXMediaMessage(webpage)
            msg.title = context.getString(R.string.app_name)
            msg.description = " "
            val thumb = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
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