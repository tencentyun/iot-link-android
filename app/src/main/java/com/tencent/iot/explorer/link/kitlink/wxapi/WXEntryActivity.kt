package com.tencent.iot.explorer.link.kitlink.wxapi

import android.app.Activity
import android.os.Bundle
import com.tencent.iot.explorer.link.kitlink.util.WeChatLogin
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.iot.explorer.link.util.L

/**
 * 微信回调
 */
class WXEntryActivity : Activity(), IWXAPIEventHandler {

    private var respCode = ""
    // 微信接口
    private var iwxapi: IWXAPI? = null

    companion object {
        var onLoginListener: WeChatLogin.OnLoginListener? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 微信注册
        regToWx()
        //如果没回调onResp，八成是这句没有写
        iwxapi?.handleIntent(intent, this)
    }

    /**
     * 将应用注册到微信
     */
    private fun regToWx() {
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        iwxapi = WXAPIFactory.createWXAPI(this, WeChatLogin.APP_ID, true)
    }

    /**
     * 微信发送请求到第三方应用时，会回调到该方法
     *
     * @param baseReq
     */
    override fun onReq(baseReq: BaseReq?) {
        baseReq?.let {
            if (it is SendAuth.Req) {
                L.e("it.scope=${it.scope}")
            }
        }
    }

    /**
     * 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
     * app发送消息给微信，处理返回消息的回调
     *
     * @param resp
     */
    override fun onResp(resp: BaseResp?) {
        resp?.let {
            when (it.errCode) {
                BaseResp.ErrCode.ERR_OK -> {
                    if (resp is SendAuth.Resp) {
                        //获取微信传回的code
                        respCode = resp.code
                        L.e("respCode：$respCode")
                        onLoginListener?.onSuccess(respCode)
                    }
                }
                BaseResp.ErrCode.ERR_USER_CANCEL -> {
                    //发送取消
                    onLoginListener?.cancel()
                    finish()
                }

                BaseResp.ErrCode.ERR_AUTH_DENIED -> {
                    L.e("授权失败")
                    onLoginListener?.onFail("授权失败")
                    //发送被拒绝
                    finish()
                }
                else -> {
                    L.e("onResp default errCode " + resp.errCode)
                    onLoginListener?.onFail("授权失败")
                    //发送返回
                    finish()
                }
            }
        }
        finish()
    }

    override fun onDestroy() {
        onLoginListener = null
        super.onDestroy()
    }

}