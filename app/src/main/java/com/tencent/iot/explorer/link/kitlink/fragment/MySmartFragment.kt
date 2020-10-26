package com.tencent.iot.explorer.link.kitlink.fragment

import android.util.Log
import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.fragment_my_smart.*

/**
 * 我的智能
 */
class MySmartFragment() : BaseFragment(), View.OnClickListener {

    private var addDialog: ListOptionsDialog? = null
    var options = ArrayList<String>()

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_my_smart
    }

    override fun startHere(view: View) {
        options.add(getString(R.string.smart_option_1))
        options.add(getString(R.string.smart_option_2))
        addDialog = ListOptionsDialog(context, options)
        addDialog?.setOnDismisListener(onItemClickedListener)

        tv_add_now_btn.setOnClickListener(this)
    }

    private var onItemClickedListener = ListOptionsDialog.OnDismisListener {
        Log.e("XXX", "pos " + it)
    }

    override fun onResume() {
        super.onResume()
        initView()
    }

    private fun initView() {

    }

    override fun onClick(v: View?) {
        when(v) {
            tv_add_now_btn -> {
                addDialog?.show()
            }
        }
    }


}