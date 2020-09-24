package com.tencent.iot.explorer.link.kitlink.fragment

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.fragment_soft_ap_step.*

class SoftAppStepFragment : BaseFragment() {

    var onNextListener: OnNextListener? = null
    private var tipContent: TextView? = null
    private var pic: ImageView? = null
    private var nextBtn: TextView? = null

    private var picUrl = "picUrl"
    private var tipContentStr = "tipContentStr"
    private var nextBtnStr = "nextBtnStr"

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_soft_ap_step
    }

    override fun startHere(view: View) {
        tipContent = view.findViewById(R.id.tv_soft_banner_title)
        pic = view.findViewById(R.id.iv_soft_ap)
        nextBtn = view.findViewById(R.id.tv_soft_first_commit)

        loadViewInfo()

        tv_soft_first_commit.setOnClickListener {
            onNextListener?.onNext()
        }
    }

    // 网络请求成功且返回自定义的内容，调用页面内容加载方法，网络请求失败无需调用
    private fun loadViewInfo() {
        nextBtn?.setText(nextBtnStr)
        tipContent?.setText(tipContentStr)
        Picasso.get().load(picUrl).placeholder(R.drawable.imageselector_default_error)
            .resize(App.data.screenWith / 5, App.data.screenWith / 5).centerCrop()
            .into(pic)
    }

    // 网络请求成功且返回标准内容
    private fun loadViewStandradInfo() {
        nextBtn?.setText(R.string.soft_ap_first_button)
        tipContent?.setText(R.string.soft_ap_first_toast)
        pic?.setImageResource(R.mipmap.image_soft_ap)
    }

    interface OnNextListener {
        fun onNext()
    }
}