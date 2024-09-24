package com.tencent.iot.explorer.link.kitlink.activity

import androidx.core.os.bundleOf
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.fragment.DeviceCategoryFragment
import com.tencent.iot.explorer.link.mvp.IPresenter

class DeviceCategoryActivity : PActivity() {
    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.activity_device_category
    }

    override fun initView() {
        val fragment = DeviceCategoryFragment()
        fragment.arguments =
            bundleOf("formSourceType" to DeviceCategoryFragment.FormSourceType.FROM_DEVICE_CATEGORY_ACTIVITY)
        supportFragmentManager.beginTransaction()
            .add(R.id.device_category_fragment_container, fragment)
            .commitAllowingStateLoss()
    }

    override fun setListener() {
    }
}
