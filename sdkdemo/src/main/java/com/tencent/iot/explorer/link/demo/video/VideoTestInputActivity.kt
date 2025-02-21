package com.tencent.iot.explorer.link.demo.video

import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.core.content.ContextCompat
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.video.preview.VideoTestActivity
import com.tencent.iot.video.link.consts.VideoConst
import kotlinx.android.synthetic.main.activity_video_input_authorize.btn_login
import kotlinx.android.synthetic.main.activity_video_input_authorize.product_id_layout
import kotlinx.android.synthetic.main.activity_video_test_input.btn_paste
import kotlinx.android.synthetic.main.activity_video_test_input.device_name_layout
import kotlinx.android.synthetic.main.activity_video_test_input.p2p_info_layout
import kotlinx.android.synthetic.main.blue_title_layout.iv_back
import kotlinx.android.synthetic.main.blue_title_layout.tv_title
import kotlinx.android.synthetic.main.input_item_layout.view.ev_content
import kotlinx.android.synthetic.main.input_item_layout.view.tv_tip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class VideoTestInputActivity : VideoBaseActivity(), CoroutineScope by MainScope() {

    override fun getContentView(): Int {
        return R.layout.activity_video_test_input
    }

    override fun initView() {
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
        tv_title.setText(R.string.iot_test_demo_name)
        product_id_layout.tv_tip.setText(R.string.product_id)
        device_name_layout.tv_tip.setText(R.string.device_name)
        p2p_info_layout.tv_tip.setText(R.string.p2p_info)
        if (productId.isNotEmpty()) {
            product_id_layout.ev_content.setText(productId)
        }
        product_id_layout.ev_content.setHint(R.string.hint_product_id)
        product_id_layout.ev_content.inputType = InputType.TYPE_CLASS_TEXT
        if (deviceName.isNotEmpty()) {
            device_name_layout.ev_content.setText(deviceName)
        }
        device_name_layout.ev_content.setHint(R.string.hint_device_name)
        device_name_layout.ev_content.inputType = InputType.TYPE_CLASS_TEXT
        if (p2pInfo.isNotEmpty()) {
            p2p_info_layout.ev_content.setText(p2pInfo)
        }
        p2p_info_layout.ev_content.setHint(R.string.hint_p2p_info)
        p2p_info_layout.ev_content.inputType = InputType.TYPE_CLASS_TEXT
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        btn_login.setOnClickListener(loginClickedListener)
        btn_paste.setOnClickListener {
            val clipboard = ContextCompat.getSystemService(this, ClipboardManager::class.java);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                clipboard.primaryClip?.getItemAt(0)?.text.toString().split("\n")
                    .forEachIndexed { index, s ->
                        when (index) {
                            0 -> product_id_layout.ev_content.setText(s)
                            1 -> device_name_layout.ev_content.setText(s)
                            2 -> p2p_info_layout.ev_content.setText(s)
                        }
                    }
            }
        }
    }

    var loginClickedListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            if (product_id_layout.ev_content.text.isNullOrEmpty()) {
                show(getString(R.string.hint_product_id))
                return
            }
            SharePreferenceUtil.saveString(
                this@VideoTestInputActivity,
                VideoConst.VIDEO_CONFIG,
                VideoConst.MULTI_VIDEO_PROD_ID,
                product_id_layout.ev_content.text.toString()
            )
            if (device_name_layout.ev_content.text.isNullOrEmpty()) {
                show(getString(R.string.hint_device_name))
                return
            }
            SharePreferenceUtil.saveString(
                this@VideoTestInputActivity,
                VideoConst.VIDEO_CONFIG,
                VideoConst.VIDEO_WLAN_DEV_NAMES,
                device_name_layout.ev_content.text.toString()
            )
            if (p2p_info_layout.ev_content.text.isNullOrEmpty()) {
                show(getString(R.string.hint_p2p_info))
                return
            }
            SharePreferenceUtil.saveString(
                this@VideoTestInputActivity,
                VideoConst.VIDEO_CONFIG,
                VideoConst.MULTI_VIDEO_P2P_INFO,
                p2p_info_layout.ev_content.text.toString()
            )
            val intent = Intent(this@VideoTestInputActivity, VideoTestActivity::class.java)
            intent.putExtra("productId", product_id_layout.ev_content.text.toString())
            intent.putExtra("deviceName", device_name_layout.ev_content.text.toString())
            intent.putExtra("p2pInfo", p2p_info_layout.ev_content.text.toString())
            val bundle = Bundle()
            intent.putExtra(VideoConst.VIDEO_CONFIG, bundle)
            startActivity(intent)
        }
    }
}
