package com.tencent.iot.explorer.link.demo.video.fragment

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.fragment.BaseFragment
import com.tencent.iot.explorer.link.demo.databinding.FragmentSingleVideoInputBinding
import com.tencent.iot.explorer.link.demo.video.preview.VideoTestActivity
import com.tencent.iot.video.link.consts.VideoConst

/**
 * 单设备直连参数输入
 */
class SingleVideoInputFragment : BaseFragment<FragmentSingleVideoInputBinding>() {

    private var isStartCross = false
    private var protocol = "auto"
    private var saveRawAv = false
    private var enableAec = false

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSingleVideoInputBinding =
        FragmentSingleVideoInputBinding.inflate(inflater, container, false)

    override fun startHere(view: View) {
        initView()
        setListener()
    }

    private fun initView() {
        with(binding) {
            val ctx = requireContext()
            val productId = SharePreferenceUtil.getString(
                ctx,
                VideoConst.VIDEO_CONFIG,
                VideoConst.MULTI_VIDEO_PROD_ID
            )
            val deviceName = SharePreferenceUtil.getString(
                ctx,
                VideoConst.VIDEO_CONFIG,
                VideoConst.VIDEO_WLAN_DEV_NAMES
            )
            val p2pInfo = SharePreferenceUtil.getString(
                ctx,
                VideoConst.VIDEO_CONFIG,
                VideoConst.MULTI_VIDEO_P2P_INFO
            )
            // 跟随设备配置默认打开：无本地记录时默认开启，有记录则按记录
            val sp = ctx.getSharedPreferences(VideoConst.VIDEO_CONFIG, Context.MODE_PRIVATE)
            val startCross = if (sp.contains("isStartCross")) {
                sp.getInt("isStartCross", 0) == 1
            } else {
                true
            }
            val appKey = SharePreferenceUtil.getString(
                ctx,
                VideoConst.VIDEO_CONFIG,
                VideoConst.VIDEO_APP_KEY
            )
            val appSecret = SharePreferenceUtil.getString(
                ctx,
                VideoConst.VIDEO_CONFIG,
                VideoConst.VIDEO_APP_SECRET
            )
            productIdLayout.tvTip.setText(R.string.product_id_text)
            deviceNameLayout.tvTip.setText(R.string.device_name_text)
            p2pInfoLayout.tvTip.setText(R.string.p2p_info_text)
            appKeyLayout.tvTip.setText(R.string.app_key_text)
            appSecretLayout.tvTip.setText(R.string.app_secret)
            if (productId.isNotEmpty()) {
                productIdLayout.evContent.setText(productId)
            }
            productIdLayout.evContent.setHint(R.string.hint_product_id)
            productIdLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            if (deviceName.isNotEmpty()) {
                deviceNameLayout.evContent.setText(deviceName)
            }
            deviceNameLayout.evContent.setHint(R.string.hint_device_name)
            deviceNameLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            if (p2pInfo.isNotEmpty()) {
                p2pInfoLayout.evContent.setText(p2pInfo)
            }
            p2pInfoLayout.evContent.setHint(R.string.hint_p2p_info)
            p2pInfoLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            if (appKey.isNotEmpty()) {
                appKeyLayout.evContent.setText(appKey)
            }
            this@SingleVideoInputFragment.isStartCross = startCross
            swtCross.isChecked = startCross
            btnAppPaste.isVisible = startCross
            appKeyLayout.root.isVisible = startCross
            appSecretLayout.root.isVisible = startCross
            // 跟随设备配置时不展示传输协议
            protocolCard.isVisible = !startCross
            appKeyLayout.evContent.setHint(R.string.hint_app_key)
            appKeyLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            if (appSecret.isNotEmpty()) {
                appSecretLayout.evContent.setText(appSecret)
            }
            appSecretLayout.evContent.setHint(R.string.hint_app_secret)
            appSecretLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            swtSaveRawAv.isChecked = saveRawAv
            swtEnableAec.isChecked = enableAec
        }
    }

    private fun setListener() {
        with(binding) {
            btnLogin.setOnClickListener(loginClickedListener)
            btnPaste.setOnClickListener {
                val clipboard = ContextCompat.getSystemService(
                    requireContext(),
                    ClipboardManager::class.java
                )
                if (clipboard != null && clipboard.hasPrimaryClip()) {
                    clipboard.primaryClip?.getItemAt(0)?.text.toString().split("\n")
                        .forEachIndexed { index, s ->
                            when (index) {
                                0 -> productIdLayout.evContent.setText(s)
                                1 -> deviceNameLayout.evContent.setText(s)
                                2 -> p2pInfoLayout.evContent.setText(s)
                                3 -> appKeyLayout.evContent.setText(s)
                                4 -> appSecretLayout.evContent.setText(s)
                            }
                        }
                }
            }
            btnAppPaste.setOnClickListener {
                val clipboard = ContextCompat.getSystemService(
                    requireContext(),
                    ClipboardManager::class.java
                )
                if (clipboard != null && clipboard.hasPrimaryClip()) {
                    clipboard.primaryClip?.getItemAt(0)?.text.toString().split("\n")
                        .forEachIndexed { index, s ->
                            when (index) {
                                0 -> appKeyLayout.evContent.setText(s)
                                1 -> appSecretLayout.evContent.setText(s)
                            }
                        }
                }
            }
            swtCross.setOnCheckedChangeListener { _, checked ->
                isStartCross = checked
                btnAppPaste.isVisible = checked
                appKeyLayout.root.isVisible = checked
                appSecretLayout.root.isVisible = checked
                // 跟随设备配置开启时隐藏传输协议，关闭时显示
                protocolCard.isVisible = !checked
            }
            rgProtocol.setOnCheckedChangeListener { group, checkedId ->
                protocol = group.findViewById<RadioButton>(checkedId).tag.toString()
            }
            swtSaveRawAv.setOnCheckedChangeListener { _, checked -> saveRawAv = checked }
            swtEnableAec.setOnCheckedChangeListener { _, checked -> enableAec = checked }
        }
    }

    private var loginClickedListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            with(binding) {
                val ctx = requireContext()
                if (productIdLayout.evContent.text.isNullOrEmpty()) {
                    show(getString(R.string.hint_product_id))
                    return
                }
                SharePreferenceUtil.saveString(
                    ctx,
                    VideoConst.VIDEO_CONFIG,
                    VideoConst.MULTI_VIDEO_PROD_ID,
                    productIdLayout.evContent.text.toString()
                )
                if (deviceNameLayout.evContent.text.isNullOrEmpty()) {
                    show(getString(R.string.hint_device_name))
                    return
                }
                SharePreferenceUtil.saveString(
                    ctx,
                    VideoConst.VIDEO_CONFIG,
                    VideoConst.VIDEO_WLAN_DEV_NAMES,
                    deviceNameLayout.evContent.text.toString()
                )
                if (p2pInfoLayout.evContent.text.isNullOrEmpty()) {
                    show(getString(R.string.hint_p2p_info))
                    return
                }
                SharePreferenceUtil.saveInt(
                    ctx,
                    VideoConst.VIDEO_CONFIG,
                    "isStartCross",
                    if (isStartCross) 1 else 0
                )
                if (isStartCross) {
                    if (appKeyLayout.evContent.text.isNullOrEmpty()) {
                        show(getString(R.string.hint_app_key))
                        return
                    }
                    SharePreferenceUtil.saveString(
                        ctx,
                        VideoConst.VIDEO_CONFIG,
                        VideoConst.VIDEO_APP_KEY,
                        appKeyLayout.evContent.text.toString()
                    )
                    if (appSecretLayout.evContent.text.isNullOrEmpty()) {
                        show(getString(R.string.hint_app_secret))
                        return
                    }
                    if (appSecretLayout.evContent.text.isNotEmpty()) {
                        SharePreferenceUtil.saveString(
                            ctx,
                            VideoConst.VIDEO_CONFIG,
                            VideoConst.VIDEO_APP_SECRET,
                            appSecretLayout.evContent.text.toString()
                        )
                    }
                }
                val intent = Intent(ctx, VideoTestActivity::class.java)
                intent.putExtra("productId", productIdLayout.evContent.text.toString())
                intent.putExtra("deviceName", deviceNameLayout.evContent.text.toString())
                intent.putExtra("p2pInfo", p2pInfoLayout.evContent.text.toString())
                intent.putExtra("appKey", appKeyLayout.evContent.text.toString())
                intent.putExtra("appSecret", appSecretLayout.evContent.text.toString())
                intent.putExtra("isStartCross", isStartCross)
                // 跟随设备配置时协议由设备侧决定，避免隐藏后的旧选择仍生效
                intent.putExtra("protocol", if (isStartCross) "auto" else protocol)
                intent.putExtra("saveRawAv", saveRawAv)
                intent.putExtra("enableAec", enableAec)
                startActivity(intent)
            }
        }
    }

    private fun show(text: String?) {
        if (text.isNullOrEmpty()) return
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}
