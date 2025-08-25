package com.tencent.iot.explorer.link.demo.video

import android.content.ClipboardManager
import android.content.Intent
import android.text.InputType
import android.view.View
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityVideoTestInputBinding
import com.tencent.iot.explorer.link.demo.video.preview.VideoTestActivity
import com.tencent.iot.video.link.consts.VideoConst
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class VideoTestInputActivity : VideoBaseActivity<ActivityVideoTestInputBinding>(),
    CoroutineScope by MainScope() {

    private var isStartCross = false
    private var protocol = "auto"

    override fun getViewBinding(): ActivityVideoTestInputBinding =
        ActivityVideoTestInputBinding.inflate(layoutInflater)

    override fun initView() {
        with(binding) {
            val productId = SharePreferenceUtil.getString(
                this@VideoTestInputActivity,
                VideoConst.VIDEO_CONFIG,
                VideoConst.MULTI_VIDEO_PROD_ID
            )
            val deviceName = SharePreferenceUtil.getString(
                this@VideoTestInputActivity,
                VideoConst.VIDEO_CONFIG,
                VideoConst.VIDEO_WLAN_DEV_NAMES
            )
            val p2pInfo = SharePreferenceUtil.getString(
                this@VideoTestInputActivity,
                VideoConst.VIDEO_CONFIG,
                VideoConst.MULTI_VIDEO_P2P_INFO
            )
            val appKey = SharePreferenceUtil.getString(
                this@VideoTestInputActivity,
                VideoConst.VIDEO_CONFIG,
                VideoConst.VIDEO_APP_KEY
            )
            val appSecret = SharePreferenceUtil.getString(
                this@VideoTestInputActivity,
                VideoConst.VIDEO_CONFIG,
                VideoConst.VIDEO_APP_SECRET
            )
            vTitle.tvTitle.setText(R.string.iot_test_demo_name)
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
            appKeyLayout.evContent.setHint(R.string.hint_app_key)
            appKeyLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            if (appSecret.isNotEmpty()) {
                appSecretLayout.evContent.setText(appSecret)
            }
            appSecretLayout.evContent.setHint(R.string.hint_app_secret)
            appSecretLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
        }
    }

    override fun setListener() {
        with(binding) {
            vTitle.ivBack.setOnClickListener { finish() }
            btnLogin.setOnClickListener(loginClickedListener)
            btnPaste.setOnClickListener {
                val clipboard = ContextCompat.getSystemService(
                    this@VideoTestInputActivity,
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
                    this@VideoTestInputActivity,
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
            }
            rgProtocol.setOnCheckedChangeListener { group, checkedId ->
                protocol = group.findViewById<RadioButton>(checkedId).tag.toString()
            }
        }
    }

    private var loginClickedListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            with(binding) {
                if (productIdLayout.evContent.text.isNullOrEmpty()) {
                    show(getString(R.string.hint_product_id))
                    return
                }
                SharePreferenceUtil.saveString(
                    this@VideoTestInputActivity,
                    VideoConst.VIDEO_CONFIG,
                    VideoConst.MULTI_VIDEO_PROD_ID,
                    productIdLayout.evContent.text.toString()
                )
                if (deviceNameLayout.evContent.text.isNullOrEmpty()) {
                    show(getString(R.string.hint_device_name))
                    return
                }
                SharePreferenceUtil.saveString(
                    this@VideoTestInputActivity,
                    VideoConst.VIDEO_CONFIG,
                    VideoConst.VIDEO_WLAN_DEV_NAMES,
                    deviceNameLayout.evContent.text.toString()
                )
                if (p2pInfoLayout.evContent.text.isNullOrEmpty()) {
                    show(getString(R.string.hint_p2p_info))
                    return
                }
                if (isStartCross) {
                    if (appKeyLayout.evContent.text.isNullOrEmpty()) {
                        show(getString(R.string.hint_app_key))
                        return
                    }
                    SharePreferenceUtil.saveString(
                        this@VideoTestInputActivity,
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
                            this@VideoTestInputActivity,
                            VideoConst.VIDEO_CONFIG,
                            VideoConst.VIDEO_APP_SECRET,
                            appSecretLayout.evContent.text.toString()
                        )
                    }
                }
                SharePreferenceUtil.saveString(
                    this@VideoTestInputActivity,
                    VideoConst.VIDEO_CONFIG,
                    VideoConst.MULTI_VIDEO_P2P_INFO,
                    p2pInfoLayout.evContent.text.toString()
                )
                val intent = Intent(this@VideoTestInputActivity, VideoTestActivity::class.java)
                intent.putExtra("productId", productIdLayout.evContent.text.toString())
                intent.putExtra("deviceName", deviceNameLayout.evContent.text.toString())
                intent.putExtra("p2pInfo", p2pInfoLayout.evContent.text.toString())
                intent.putExtra("appKey", appKeyLayout.evContent.text.toString())
                intent.putExtra("appSecret", appSecretLayout.evContent.text.toString())
                intent.putExtra("isStartCross", isStartCross)
                intent.putExtra("protocol", protocol)
                startActivity(intent)
            }
        }
    }
}
