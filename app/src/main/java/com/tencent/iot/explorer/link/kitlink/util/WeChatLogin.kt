package com.tencent.iot.explorer.link.kitlink.util

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import com.tencent.iot.explorer.link.BuildConfig
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.wxapi.WXEntryActivity
import com.tencent.iot.explorer.link.util.T
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
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

    fun shareText(context: Context, shareContent: String?) {
        var wxApi = WXAPIFactory.createWXAPI(context, APP_ID)
        if (wxApi.isWXAppInstalled) {
            wxApi.registerApp(APP_ID)
            val webpage = WXWebpageObject()
            webpage.webpageUrl = shareContent
            val msg = WXMediaMessage(webpage)
//            msg.title = context.getString(R.string.app_name)
        msg.description = " "
            //这里替换一张自己工程里的图片资源
//            val thumb =
//                BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
//            msg.setThumbImage(thumb)
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