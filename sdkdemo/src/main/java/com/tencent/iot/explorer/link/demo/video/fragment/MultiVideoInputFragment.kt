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
import androidx.core.view.isVisible
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.fragment.BaseFragment
import com.tencent.iot.explorer.link.demo.databinding.FragmentMultiVideoInputBinding
import com.tencent.iot.explorer.link.demo.video.preview.MultiVideoTestActivity

/**
 * 多设备直连参数输入
 */
class MultiVideoInputFragment : BaseFragment<FragmentMultiVideoInputBinding>() {

    private var protocol1 = "auto"
    private var protocol2 = "auto"
    private var protocol3 = "auto"
    private var protocol4 = "auto"

    /** 当前选择的设备数量（2/3/4），0 表示尚未选择，决定显示几张设备卡片 */
    private var deviceCount = 0

    /** 请求宿主 Activity 弹出设备数量选择 */
    var onRequestSelectCount: (() -> Unit)? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMultiVideoInputBinding =
        FragmentMultiVideoInputBinding.inflate(inflater, container, false)

    override fun startHere(view: View) {
        initView()
        setListener()
    }

    private fun initView() {
        with(binding) {
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

            // 统一配置
            appKeyLayout.tvTip.setText("AppKey")
            appKeyLayout.evContent.setHint("请输入AppKey")
            appKeyLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT

            appSecretLayout.tvTip.setText("AppSecret")
            appSecretLayout.evContent.setHint("请输入AppSecret")
            appSecretLayout.evContent.inputType = InputType.TYPE_CLASS_TEXT

            updateDeviceCountUi()
        }
    }

    private fun setListener() {
        with(binding) {
            btnLogin.setOnClickListener(loginClickedListener)

            // 粘贴按钮点击事件
            btnPasteDevice1.setOnClickListener { pasteDeviceInfo(1) }
            btnPasteDevice2.setOnClickListener { pasteDeviceInfo(2) }
            btnPasteDevice3.setOnClickListener { pasteDeviceInfo(3) }
            btnPasteDevice4.setOnClickListener { pasteDeviceInfo(4) }

            // 跟随设备配置开关：联动统一配置与各设备协议显隐
            switchDevice1FollowConfig.setOnCheckedChangeListener { _, _ -> updateFollowConfigUi() }
            switchDevice2FollowConfig.setOnCheckedChangeListener { _, _ -> updateFollowConfigUi() }
            switchDevice3FollowConfig.setOnCheckedChangeListener { _, _ -> updateFollowConfigUi() }
            switchDevice4FollowConfig.setOnCheckedChangeListener { _, _ -> updateFollowConfigUi() }

            // 各设备传输协议
            rgProtocolDevice1.setOnCheckedChangeListener { group, checkedId ->
                protocol1 = group.findViewById<RadioButton>(checkedId).tag.toString()
            }
            rgProtocolDevice2.setOnCheckedChangeListener { group, checkedId ->
                protocol2 = group.findViewById<RadioButton>(checkedId).tag.toString()
            }
            rgProtocolDevice3.setOnCheckedChangeListener { group, checkedId ->
                protocol3 = group.findViewById<RadioButton>(checkedId).tag.toString()
            }
            rgProtocolDevice4.setOnCheckedChangeListener { group, checkedId ->
                protocol4 = group.findViewById<RadioButton>(checkedId).tag.toString()
            }
        }
    }

    /** 该设备是否在已选择的设备数量范围内（未选择时都不显示） */
    private fun isDeviceSelected(index: Int): Boolean = deviceCount > 0 && index <= deviceCount

    /** 设备数量联动：只显示已选择数量的设备卡片 */
    private fun updateDeviceCountUi() {
        with(binding) {
            deviceCard1.isVisible = isDeviceSelected(1)
            deviceCard2.isVisible = isDeviceSelected(2)
            deviceCard3.isVisible = isDeviceSelected(3)
            deviceCard4.isVisible = isDeviceSelected(4)
        }
        updateFollowConfigUi()
    }

    /**
     * 跟随设备配置联动（仅统计已选择的设备）：
     * 1. 任一已选设备开启“使用统一配置”时才显示统一配置卡片；
     * 2. 单个设备开启后隐藏该设备的传输协议（协议由设备侧决定）。
     */
    private fun updateFollowConfigUi() {
        with(binding) {
            protocolLayoutDevice1.isVisible = !switchDevice1FollowConfig.isChecked
            protocolLayoutDevice2.isVisible = !switchDevice2FollowConfig.isChecked
            protocolLayoutDevice3.isVisible =
                isDeviceSelected(3) && !switchDevice3FollowConfig.isChecked
            protocolLayoutDevice4.isVisible =
                isDeviceSelected(4) && !switchDevice4FollowConfig.isChecked

            unifiedConfigCard.isVisible = isFollowingConfig()
        }
    }

    private fun isFollowingConfig(): Boolean = with(binding) {
        switchDevice1FollowConfig.isChecked || switchDevice2FollowConfig.isChecked ||
                (isDeviceSelected(3) && switchDevice3FollowConfig.isChecked) ||
                (isDeviceSelected(4) && switchDevice4FollowConfig.isChecked)
    }

    private var loginClickedListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            // 未选择设备数量时先弹出选择
            if (deviceCount <= 0) {
                onRequestSelectCount?.invoke()
                return
            }
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

                // 获取统一的配置信息
                val appKey = appKeyLayout.evContent.text.toString()
                val appSecret = appSecretLayout.evContent.text.toString()

                // 有设备使用统一配置时，AppKey / AppSecret 必填
                if (isFollowingConfig()) {
                    if (appKey.isEmpty()) {
                        show("请填写统一配置的 AppKey")
                        return
                    }
                    if (appSecret.isEmpty()) {
                        show("请填写统一配置的 AppSecret")
                        return
                    }
                }

                // 跳转到多设备测试页面
                val intent = Intent(requireContext(), MultiVideoTestActivity::class.java)

                // 设备1、2 始终参与
                intent.putDevice(
                    1,
                    device1ProductIdLayout.evContent.text.toString(),
                    device1DeviceNameLayout.evContent.text.toString(),
                    device1P2pInfoLayout.evContent.text.toString(),
                    switchDevice1FollowConfig.isChecked,
                    if (switchDevice1FollowConfig.isChecked) "auto" else protocol1,
                    appKey, appSecret
                )
                intent.putDevice(
                    2,
                    device2ProductIdLayout.evContent.text.toString(),
                    device2DeviceNameLayout.evContent.text.toString(),
                    device2P2pInfoLayout.evContent.text.toString(),
                    switchDevice2FollowConfig.isChecked,
                    if (switchDevice2FollowConfig.isChecked) "auto" else protocol2,
                    appKey, appSecret
                )

                // 设备3 仅在选中数量时才参与
                val selected3 = isDeviceSelected(3)
                intent.putDevice(
                    3,
                    if (selected3) device3ProductIdLayout.evContent.text.toString() else "",
                    if (selected3) device3DeviceNameLayout.evContent.text.toString() else "",
                    if (selected3) device3P2pInfoLayout.evContent.text.toString() else "",
                    selected3 && switchDevice3FollowConfig.isChecked,
                    if (switchDevice3FollowConfig.isChecked) "auto" else protocol3,
                    appKey, appSecret
                )

                // 设备4 仅在选中数量时才参与
                val selected4 = isDeviceSelected(4)
                intent.putDevice(
                    4,
                    if (selected4) device4ProductIdLayout.evContent.text.toString() else "",
                    if (selected4) device4DeviceNameLayout.evContent.text.toString() else "",
                    if (selected4) device4P2pInfoLayout.evContent.text.toString() else "",
                    selected4 && switchDevice4FollowConfig.isChecked,
                    if (switchDevice4FollowConfig.isChecked) "auto" else protocol4,
                    appKey, appSecret
                )

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
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip

        if (clipData != null && clipData.itemCount > 0) {
            val lines = clipData.getItemAt(0)?.text.toString().split("\n")
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
                            // 如果有appKey和appSecret，填充到统一配置区域并打开开关
                            if (appKey.isNotEmpty() || appSecret.isNotEmpty()) {
                                if (appKey.isNotEmpty()) {
                                    appKeyLayout.evContent.setText(appKey)
                                }
                                if (appSecret.isNotEmpty()) {
                                    appSecretLayout.evContent.setText(appSecret)
                                }
                                switchDevice1FollowConfig.isChecked = true
                            }
                        }
                        2 -> {
                            device2ProductIdLayout.evContent.setText(productId)
                            device2DeviceNameLayout.evContent.setText(deviceName)
                            device2P2pInfoLayout.evContent.setText(p2pInfo)
                            if (appKey.isNotEmpty() || appSecret.isNotEmpty()) {
                                if (appKey.isNotEmpty()) {
                                    appKeyLayout.evContent.setText(appKey)
                                }
                                if (appSecret.isNotEmpty()) {
                                    appSecretLayout.evContent.setText(appSecret)
                                }
                                switchDevice2FollowConfig.isChecked = true
                            }
                        }
                        3 -> {
                            device3ProductIdLayout.evContent.setText(productId)
                            device3DeviceNameLayout.evContent.setText(deviceName)
                            device3P2pInfoLayout.evContent.setText(p2pInfo)
                            if (appKey.isNotEmpty() || appSecret.isNotEmpty()) {
                                if (appKey.isNotEmpty()) {
                                    appKeyLayout.evContent.setText(appKey)
                                }
                                if (appSecret.isNotEmpty()) {
                                    appSecretLayout.evContent.setText(appSecret)
                                }
                                switchDevice3FollowConfig.isChecked = true
                            }
                        }
                        4 -> {
                            device4ProductIdLayout.evContent.setText(productId)
                            device4DeviceNameLayout.evContent.setText(deviceName)
                            device4P2pInfoLayout.evContent.setText(p2pInfo)
                            if (appKey.isNotEmpty() || appSecret.isNotEmpty()) {
                                if (appKey.isNotEmpty()) {
                                    appKeyLayout.evContent.setText(appKey)
                                }
                                if (appSecret.isNotEmpty()) {
                                    appSecretLayout.evContent.setText(appSecret)
                                }
                                switchDevice4FollowConfig.isChecked = true
                            }
                        }
                    }
                }
                val message = if (appKey.isNotEmpty() || appSecret.isNotEmpty()) {
                    "设备$deviceIndex 信息已粘贴（包含配置）"
                } else {
                    "设备$deviceIndex 信息已粘贴"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "粘贴板格式错误，请每行输入一个字段：\n第一行：产品ID\n第二行：设备名称\n第三行：P2P信息\n第四行：AppKey（可选）\n第五行：AppSecret（可选）", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(requireContext(), "粘贴板为空", Toast.LENGTH_SHORT).show()
        }
    }

    private fun show(text: String?) {
        if (text.isNullOrEmpty()) return
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    /** 由宿主在切换前设置设备数量 */
    fun setDeviceCount(count: Int) {
        deviceCount = count
        if (view != null) updateDeviceCountUi()
    }

    /** 按设备序号写入参数，跟随配置时才附带 AppKey / AppSecret */
    private fun Intent.putDevice(
        index: Int,
        productId: String,
        deviceName: String,
        p2pInfo: String,
        follow: Boolean,
        protocol: String,
        appKey: String,
        appSecret: String
    ) {
        putExtra("device${index}_productId", productId)
        putExtra("device${index}_deviceName", deviceName)
        putExtra("device${index}_p2pInfo", p2pInfo)
        putExtra("device${index}_followConfig", follow)
        putExtra("device${index}_protocol", protocol)
        if (follow) {
            putExtra("device${index}_appKey", appKey)
            putExtra("device${index}_appSecret", appSecret)
        }
    }
}
