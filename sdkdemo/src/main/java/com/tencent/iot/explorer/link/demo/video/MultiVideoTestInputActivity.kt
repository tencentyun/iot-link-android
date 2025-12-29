package com.tencent.iot.explorer.link.demo.video

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.InputType
import android.view.View
import android.widget.Toast
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
            device1ProductIdLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device1DeviceNameLayout.tvTip.setText(R.string.device_name_text)
            device1DeviceNameLayout.evContent.setHint(R.string.hint_device_name)
            device1DeviceNameLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device1P2pInfoLayout.tvTip.setText(R.string.p2p_info_text)
            device1P2pInfoLayout.evContent.setHint(R.string.hint_p2p_info)
            device1P2pInfoLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            // 设备2
            device2ProductIdLayout.tvTip.setText(R.string.product_id_text)
            device2ProductIdLayout.evContent.setHint(R.string.hint_product_id)
            device2ProductIdLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device2DeviceNameLayout.tvTip.setText(R.string.device_name_text)
            device2DeviceNameLayout.evContent.setHint(R.string.hint_device_name)
            device2DeviceNameLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device2P2pInfoLayout.tvTip.setText(R.string.p2p_info_text)
            device2P2pInfoLayout.evContent.setHint(R.string.hint_p2p_info)
            device2P2pInfoLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            // 设备3
            device3ProductIdLayout.tvTip.setText(R.string.product_id_text)
            device3ProductIdLayout.evContent.setHint(R.string.hint_product_id)
            device3ProductIdLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device3DeviceNameLayout.tvTip.setText(R.string.device_name_text)
            device3DeviceNameLayout.evContent.setHint(R.string.hint_device_name)
            device3DeviceNameLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device3P2pInfoLayout.tvTip.setText(R.string.p2p_info_text)
            device3P2pInfoLayout.evContent.setHint(R.string.hint_p2p_info)
            device3P2pInfoLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            // 设备4
            device4ProductIdLayout.tvTip.setText(R.string.product_id_text)
            device4ProductIdLayout.evContent.setHint(R.string.hint_product_id)
            device4ProductIdLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device4DeviceNameLayout.tvTip.setText(R.string.device_name_text)
            device4DeviceNameLayout.evContent.setHint(R.string.hint_device_name)
            device4DeviceNameLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device4P2pInfoLayout.tvTip.setText(R.string.p2p_info_text)
            device4P2pInfoLayout.evContent.setHint(R.string.hint_p2p_info)
            device4P2pInfoLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            // 设备1跟随设备配置
            device1AppKeyLayout.tvTip.setText("AppKey")
            device1AppKeyLayout.evContent.setHint("请输入AppKey")
            device1AppKeyLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device1AppSecretLayout.tvTip.setText("AppSecret")
            device1AppSecretLayout.evContent.setHint("请输入AppSecret")
            device1AppSecretLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            // 设备2跟随设备配置
            device2AppKeyLayout.tvTip.setText("AppKey")
            device2AppKeyLayout.evContent.setHint("请输入AppKey")
            device2AppKeyLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device2AppSecretLayout.tvTip.setText("AppSecret")
            device2AppSecretLayout.evContent.setHint("请输入AppSecret")
            device2AppSecretLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            // 设备3跟随设备配置
            device3AppKeyLayout.tvTip.setText("AppKey")
            device3AppKeyLayout.evContent.setHint("请输入AppKey")
            device3AppKeyLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device3AppSecretLayout.tvTip.setText("AppSecret")
            device3AppSecretLayout.evContent.setHint("请输入AppSecret")
            device3AppSecretLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            // 设备4跟随设备配置
            device4AppKeyLayout.tvTip.setText("AppKey")
            device4AppKeyLayout.evContent.setHint("请输入AppKey")
            device4AppKeyLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
            
            device4AppSecretLayout.tvTip.setText("AppSecret")
            device4AppSecretLayout.evContent.setHint("请输入AppSecret")
            device4AppSecretLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT
        }
    }

    override fun setListener() {
        with(binding) {
            vTitle.ivBack.setOnClickListener { finish() }
            btnLogin.setOnClickListener(loginClickedListener)
            
            // 粘贴按钮点击事件
            btnPasteDevice1.setOnClickListener { pasteDeviceInfo(1) }
            btnPasteDevice2.setOnClickListener { pasteDeviceInfo(2) }
            btnPasteDevice3.setOnClickListener { pasteDeviceInfo(3) }
            btnPasteDevice4.setOnClickListener { pasteDeviceInfo(4) }
            
            // 设备1跟随设备配置开关
            switchDevice1FollowConfig.setOnCheckedChangeListener { _, isChecked ->
                layoutDevice1AppConfig.visibility = if (isChecked) View.VISIBLE else View.GONE
            }
            
            // 设备2跟随设备配置开关
            switchDevice2FollowConfig.setOnCheckedChangeListener { _, isChecked ->
                layoutDevice2AppConfig.visibility = if (isChecked) View.VISIBLE else View.GONE
            }
            
            // 设备3跟随设备配置开关
            switchDevice3FollowConfig.setOnCheckedChangeListener { _, isChecked ->
                layoutDevice3AppConfig.visibility = if (isChecked) View.VISIBLE else View.GONE
            }
            
            // 设备4跟随设备配置开关
            switchDevice4FollowConfig.setOnCheckedChangeListener { _, isChecked ->
                layoutDevice4AppConfig.visibility = if (isChecked) View.VISIBLE else View.GONE
            }
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
                intent.putExtra("device1_followConfig", switchDevice1FollowConfig.isChecked)
                if (switchDevice1FollowConfig.isChecked) {
                    intent.putExtra("device1_appKey", device1AppKeyLayout.evContent.text.toString())
                    intent.putExtra("device1_appSecret", device1AppSecretLayout.evContent.text.toString())
                }
                
                // 传递设备2信息
                intent.putExtra("device2_productId", device2ProductIdLayout.evContent.text.toString())
                intent.putExtra("device2_deviceName", device2DeviceNameLayout.evContent.text.toString())
                intent.putExtra("device2_p2pInfo", device2P2pInfoLayout.evContent.text.toString())
                intent.putExtra("device2_followConfig", switchDevice2FollowConfig.isChecked)
                if (switchDevice2FollowConfig.isChecked) {
                    intent.putExtra("device2_appKey", device2AppKeyLayout.evContent.text.toString())
                    intent.putExtra("device2_appSecret", device2AppSecretLayout.evContent.text.toString())
                }
                
                // 传递设备3信息
                intent.putExtra("device3_productId", device3ProductIdLayout.evContent.text.toString())
                intent.putExtra("device3_deviceName", device3DeviceNameLayout.evContent.text.toString())
                intent.putExtra("device3_p2pInfo", device3P2pInfoLayout.evContent.text.toString())
                intent.putExtra("device3_followConfig", switchDevice3FollowConfig.isChecked)
                if (switchDevice3FollowConfig.isChecked) {
                    intent.putExtra("device3_appKey", device3AppKeyLayout.evContent.text.toString())
                    intent.putExtra("device3_appSecret", device3AppSecretLayout.evContent.text.toString())
                }
                
                // 传递设备4信息
                intent.putExtra("device4_productId", device4ProductIdLayout.evContent.text.toString())
                intent.putExtra("device4_deviceName", device4DeviceNameLayout.evContent.text.toString())
                intent.putExtra("device4_p2pInfo", device4P2pInfoLayout.evContent.text.toString())
                intent.putExtra("device4_followConfig", switchDevice4FollowConfig.isChecked)
                if (switchDevice4FollowConfig.isChecked) {
                    intent.putExtra("device4_appKey", device4AppKeyLayout.evContent.text.toString())
                    intent.putExtra("device4_appSecret", device4AppSecretLayout.evContent.text.toString())
                }
                
                startActivity(intent)
            }
        }
    }
    
    /**
     * 粘贴设备信息
     * 格式: 每行一个字段，按顺序为：
     * productId
     * deviceName
     * p2pInfo
     * appKey (可选)
     * appSecret (可选)
     */
    private fun pasteDeviceInfo(deviceIndex: Int) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        
        if (clipData != null && clipData.itemCount > 0) {
            val text = clipData.getItemAt(0).text.toString()
            val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            
            if (lines.size >= 3) {
                val productId = lines[0]
                val deviceName = lines[1]
                val p2pInfo = lines[2]
                val appKey = if (lines.size >= 4) lines[3] else ""
                val appSecret = if (lines.size >= 5) lines[4] else ""
                
                with(binding) {
                    when (deviceIndex) {
                        1 -> {
                            device1ProductIdLayout.evContent.setText(productId)
                            device1DeviceNameLayout.evContent.setText(deviceName)
                            device1P2pInfoLayout.evContent.setText(p2pInfo)
                            // 如果有appKey和appSecret，自动打开跟随配置开关
                            if (appKey.isNotEmpty() || appSecret.isNotEmpty()) {
                                switchDevice1FollowConfig.isChecked = true
                                if (appKey.isNotEmpty()) {
                                    device1AppKeyLayout.evContent.setText(appKey)
                                }
                                if (appSecret.isNotEmpty()) {
                                    device1AppSecretLayout.evContent.setText(appSecret)
                                }
                            }
                        }
                        2 -> {
                            device2ProductIdLayout.evContent.setText(productId)
                            device2DeviceNameLayout.evContent.setText(deviceName)
                            device2P2pInfoLayout.evContent.setText(p2pInfo)
                            if (appKey.isNotEmpty() || appSecret.isNotEmpty()) {
                                switchDevice2FollowConfig.isChecked = true
                                if (appKey.isNotEmpty()) {
                                    device2AppKeyLayout.evContent.setText(appKey)
                                }
                                if (appSecret.isNotEmpty()) {
                                    device2AppSecretLayout.evContent.setText(appSecret)
                                }
                            }
                        }
                        3 -> {
                            device3ProductIdLayout.evContent.setText(productId)
                            device3DeviceNameLayout.evContent.setText(deviceName)
                            device3P2pInfoLayout.evContent.setText(p2pInfo)
                            if (appKey.isNotEmpty() || appSecret.isNotEmpty()) {
                                switchDevice3FollowConfig.isChecked = true
                                if (appKey.isNotEmpty()) {
                                    device3AppKeyLayout.evContent.setText(appKey)
                                }
                                if (appSecret.isNotEmpty()) {
                                    device3AppSecretLayout.evContent.setText(appSecret)
                                }
                            }
                        }
                        4 -> {
                            device4ProductIdLayout.evContent.setText(productId)
                            device4DeviceNameLayout.evContent.setText(deviceName)
                            device4P2pInfoLayout.evContent.setText(p2pInfo)
                            if (appKey.isNotEmpty() || appSecret.isNotEmpty()) {
                                switchDevice4FollowConfig.isChecked = true
                                if (appKey.isNotEmpty()) {
                                    device4AppKeyLayout.evContent.setText(appKey)
                                }
                                if (appSecret.isNotEmpty()) {
                                    device4AppSecretLayout.evContent.setText(appSecret)
                                }
                            }
                        }
                    }
                }
                val message = if (appKey.isNotEmpty() || appSecret.isNotEmpty()) {
                    "设备$deviceIndex 信息已粘贴（包含配置）"
                } else {
                    "设备$deviceIndex 信息已粘贴"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "粘贴板格式错误，请每行输入一个字段：\n第一行：产品ID\n第二行：设备名称\n第三行：P2P信息\n第四行：AppKey（可选）\n第五行：AppSecret（可选）", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "粘贴板为空", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
}