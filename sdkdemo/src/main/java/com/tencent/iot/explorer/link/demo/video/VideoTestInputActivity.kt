package com.tencent.iot.explorer.link.demo.video

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityVideoTestInputBinding
import com.tencent.iot.explorer.link.demo.databinding.DialogDeviceCountBinding
import com.tencent.iot.explorer.link.demo.video.fragment.MultiVideoInputFragment
import com.tencent.iot.explorer.link.demo.video.fragment.SingleVideoInputFragment

/**
 * 设备连接参数输入页（宿主）。
 * 默认展示“单设备直连”输入；点击标题栏右上角切换按钮时，先弹出设备数量选择，
 * 选定数量后再切换到“多设备直连”输入（均由 Fragment 实现）。
 */
class VideoTestInputActivity : VideoBaseActivity<ActivityVideoTestInputBinding>() {

    private val singleFragment by lazy { SingleVideoInputFragment() }
    private val multiFragment by lazy { MultiVideoInputFragment() }

    private var isMulti = false

    /** 已选择的设备数量，0 表示尚未选择 */
    private var deviceCount = 0

    private var countDialog: Dialog? = null

    override fun getViewBinding(): ActivityVideoTestInputBinding =
        ActivityVideoTestInputBinding.inflate(layoutInflater)

    override fun initView() {
        multiFragment.onRequestSelectCount = { showDeviceCountSheet() }
        with(binding.vTitle) {
            ivBack.setOnClickListener { finish() }
            ivRightBtn.visibility = View.VISIBLE
            ivRightBtn.setImageResource(R.drawable.ic_switch_device)
            ivRightBtn.setOnClickListener { onSwitchClicked() }
        }
        switchInputPage(false, true)
    }

    override fun setListener() {}

    /** 点击切换：切到多设备前先选择设备数量，选择完成后再切换 */
    private fun onSwitchClicked() {
        if (isMulti) {
            switchInputPage(false)
        } else {
            showDeviceCountSheet()
        }
    }

    /**
     * 底部向上弹出设备数量选择。
     * 强制选择：不可点击外部取消、不可返回键取消，必须选一个。
     */
    private fun showDeviceCountSheet() {
        if (countDialog?.isShowing == true) return

        val sheet = DialogDeviceCountBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(sheet.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        sheet.tvCount2.setOnClickListener { selectDeviceCount(2, dialog) }
        sheet.tvCount3.setOnClickListener { selectDeviceCount(3, dialog) }
        sheet.tvCount4.setOnClickListener { selectDeviceCount(4, dialog) }
        dialog.show()

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.45f)
            setGravity(Gravity.BOTTOM)
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setWindowAnimations(R.style.BottomSheetAnim)
        }
        countDialog = dialog
    }

    private fun selectDeviceCount(count: Int, dialog: Dialog) {
        deviceCount = count
        multiFragment.setDeviceCount(count)
        dialog.dismiss()
        // 选择设备数量后再切换到多设备页
        switchInputPage(true)
    }

    /**
     * 切换单/多设备输入页
     *
     * @param multi true 展示多设备页，false 展示单设备页
     * @param force 强制刷新一次（首次进入时使用）
     */
    private fun switchInputPage(multi: Boolean, force: Boolean = false) {
        if (multi == isMulti && !force) return
        isMulti = multi

        val transaction = supportFragmentManager.beginTransaction()
        if (multi) {
            if (!multiFragment.isAdded) transaction.add(R.id.fragment_container, multiFragment)
            if (singleFragment.isAdded) transaction.hide(singleFragment)
            transaction.show(multiFragment)
        } else {
            if (!singleFragment.isAdded) transaction.add(R.id.fragment_container, singleFragment)
            if (multiFragment.isAdded) transaction.hide(multiFragment)
            transaction.show(singleFragment)
        }
        transaction.commit()

        binding.vTitle.tvTitle.setText(
            if (multi) R.string.multi_device_connection else R.string.iot_test_demo_name
        )
    }

    override fun onDestroy() {
        countDialog?.dismiss()
        countDialog = null
        super.onDestroy()
    }
}
