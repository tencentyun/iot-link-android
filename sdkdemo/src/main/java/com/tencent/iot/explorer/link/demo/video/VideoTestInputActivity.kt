package com.tencent.iot.explorer.link.demo.video

import android.view.View
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityVideoTestInputBinding
import com.tencent.iot.explorer.link.demo.video.fragment.MultiVideoInputFragment
import com.tencent.iot.explorer.link.demo.video.fragment.SingleVideoInputFragment

class VideoTestInputActivity : VideoBaseActivity<ActivityVideoTestInputBinding>() {

    private val singleFragment by lazy { SingleVideoInputFragment() }
    private val multiFragment by lazy { MultiVideoInputFragment() }

    private var isMulti = false

    override fun getViewBinding(): ActivityVideoTestInputBinding =
        ActivityVideoTestInputBinding.inflate(layoutInflater)

    override fun initView() {
        with(binding.vTitle) {
            ivBack.setOnClickListener { finish() }
            ivRightBtn.visibility = View.VISIBLE
            ivRightBtn.setImageResource(R.drawable.ic_switch_device)
            ivRightBtn.setOnClickListener { switchInputPage(!isMulti) }
        }
        switchInputPage(false, true)
    }

    override fun setListener() {}

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

        binding.vTitle.tvTitle.setText(R.string.iot_test_demo_name)
    }
}
