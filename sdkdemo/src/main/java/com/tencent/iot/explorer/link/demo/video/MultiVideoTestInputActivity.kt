package com.tencent.iot.explorer.link.demo.video

import android.content.Intent
import android.text.InputType
import android.view.View
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityMultiDeviceInputBinding
import com.tencent.iot.explorer.link.demo.video.preview.MultiVideoTestActivity


class MultiVideoTestInputActivity : VideoBaseActivity<ActivityMultiDeviceInputBinding>() {

    override fun getViewBinding(): ActivityMultiDeviceInputBinding =
        ActivityMultiDeviceInputBinding.inflate(layoutInflater)

    override fun initView() {
        with(binding) {
            vTitle.tvTitle.setText(R.string.multi_device_connection)
            
            // 设备1
            device1ProductIdLayout.tvTip.setText(R.string.product_id_text)
            device1ProductIdLayout.evContent.setHint(R.string.hint_product_id)
            device1ProductIdLayout.evContent.setText("CE3P21B2JW")
            device1ProductIdLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device1DeviceNameLayout.tvTip.setText(R.string.device_name_text)
            device1DeviceNameLayout.evContent.setHint(R.string.hint_device_name)
            device1DeviceNameLayout.evContent.setText("4C_40300001_152384222_4")
            device1DeviceNameLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device1P2pInfoLayout.tvTip.setText(R.string.p2p_info_text)
            device1P2pInfoLayout.evContent.setHint(R.string.hint_p2p_info)
            device1P2pInfoLayout.evContent.setText("XP2P1HNek+BperPLzEXSyb67SI7k%2.4.50")
            device1P2pInfoLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            // 设备2
            device2ProductIdLayout.tvTip.setText(R.string.product_id_text)
            device2ProductIdLayout.evContent.setHint(R.string.hint_product_id)
            device2ProductIdLayout.evContent.setText("CE3P21B2JW")
            device2ProductIdLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device2DeviceNameLayout.tvTip.setText(R.string.device_name_text)
            device2DeviceNameLayout.evContent.setHint(R.string.hint_device_name)
            device2DeviceNameLayout.evContent.setText("4C_40300001_162157562_78")
            device2DeviceNameLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device2P2pInfoLayout.tvTip.setText(R.string.p2p_info_text)
            device2P2pInfoLayout.evContent.setHint(R.string.hint_p2p_info)
            device2P2pInfoLayout.evContent.setText("XP2PiMbQ1FQE+6ovftkH0GeChw==%2.4.50")
            device2P2pInfoLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            // 设备3
            device3ProductIdLayout.tvTip.setText(R.string.product_id_text)
            device3ProductIdLayout.evContent.setHint(R.string.hint_product_id)
            device3ProductIdLayout.evContent.setText("CE3P21B2JW")
            device3ProductIdLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device3DeviceNameLayout.tvTip.setText(R.string.device_name_text)
            device3DeviceNameLayout.evContent.setHint(R.string.hint_device_name)
            device3DeviceNameLayout.evContent.setText("4C_40300001_162157562_54")
            device3DeviceNameLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device3P2pInfoLayout.tvTip.setText(R.string.p2p_info_text)
            device3P2pInfoLayout.evContent.setHint(R.string.hint_p2p_info)
            device3P2pInfoLayout.evContent.setText("XP2Pgvn3TmlXF16x+9WTJvSt96EI%2.4.50")
            device3P2pInfoLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            // 设备4
            device4ProductIdLayout.tvTip.setText(R.string.product_id_text)
            device4ProductIdLayout.evContent.setHint(R.string.hint_product_id)
            device4ProductIdLayout.evContent.setText("CE3P21B2JW")
            device4ProductIdLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device4DeviceNameLayout.tvTip.setText(R.string.device_name_text)
            device4DeviceNameLayout.evContent.setHint(R.string.hint_device_name)
            device4DeviceNameLayout.evContent.setText("4C_40300001_162157562_57")
            device4DeviceNameLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device4P2pInfoLayout.tvTip.setText(R.string.p2p_info_text)
            device4P2pInfoLayout.evContent.setHint(R.string.hint_p2p_info)
            device4P2pInfoLayout.evContent.setText("XP2PAbTOrSl+BGVu3siirwE+Joda%2.4.50")
            device4P2pInfoLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
        }
    }

    override fun setListener() {
        with(binding) {
            vTitle.ivBack.setOnClickListener { finish() }
            btnLogin.setOnClickListener(loginClickedListener)
        }
    }

    private var loginClickedListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            with(binding) {
                // 验证设备1信息
                if (device1ProductIdLayout.evContent.text.isNullOrEmpty()) {
                    show("设备1产品ID不能为空")
                    return
                }
                if (device1DeviceNameLayout.evContent.text.isNullOrEmpty()) {
                    show("设备1设备名称不能为空")
                    return
                }
                if (device1P2pInfoLayout.evContent.text.isNullOrEmpty()) {
                    show("设备1P2P信息不能为空")
                    return
                }

                // 跳转到多设备测试页面
                val intent = Intent(this@MultiVideoTestInputActivity, MultiVideoTestActivity::class.java)
                
                // 传递设备1信息
                intent.putExtra("device1_productId", device1ProductIdLayout.evContent.text.toString())
                intent.putExtra("device1_deviceName", device1DeviceNameLayout.evContent.text.toString())
                intent.putExtra("device1_p2pInfo", device1P2pInfoLayout.evContent.text.toString())
                
                // 传递设备2信息
                intent.putExtra("device2_productId", device2ProductIdLayout.evContent.text.toString())
                intent.putExtra("device2_deviceName", device2DeviceNameLayout.evContent.text.toString())
                intent.putExtra("device2_p2pInfo", device2P2pInfoLayout.evContent.text.toString())
                
                // 传递设备3信息
                intent.putExtra("device3_productId", device3ProductIdLayout.evContent.text.toString())
                intent.putExtra("device3_deviceName", device3DeviceNameLayout.evContent.text.toString())
                intent.putExtra("device3_p2pInfo", device3P2pInfoLayout.evContent.text.toString())
                
                // 传递设备4信息
                intent.putExtra("device4_productId", device4ProductIdLayout.evContent.text.toString())
                intent.putExtra("device4_deviceName", device4DeviceNameLayout.evContent.text.toString())
                intent.putExtra("device4_p2pInfo", device4P2pInfoLayout.evContent.text.toString())
                
                startActivity(intent)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
}