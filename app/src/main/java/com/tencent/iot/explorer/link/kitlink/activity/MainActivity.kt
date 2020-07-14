package com.tencent.iot.explorer.link.kitlink.activity

import android.text.TextUtils
import android.view.View
import androidx.fragment.app.Fragment
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.entity.FamilyEntity
import com.tencent.iot.explorer.link.kitlink.fragment.HomeFragment
import com.tencent.iot.explorer.link.kitlink.fragment.MeFragment
import com.tencent.iot.explorer.link.kitlink.popup.FamilyListPopup
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.StatusBarUtil
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.android.tpush.XGIOperateCallback
import com.tencent.android.tpush.XGPushManager
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.util.T
import com.tencent.iot.explorer.link.customview.home.BottomItemEntity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess

/**
 * main页面
 */
class MainActivity : PActivity(), MyCallback {

    private val fragments = arrayListOf<Fragment>()

    private var familyPopup: FamilyListPopup? = null

    override fun getContentView(): Int {
        return R.layout.activity_main
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun onResume() {
        super.onResume()
        login(this)
        /* val sign =
             "Action=AppCreateCellphoneUser&AppKey=ahPxdKWywfNTGrejd&CountryCode=86&Nonce=71087795&Password=My!P@ssword&PhoneNumber=13900000000&RequestId=8b8d499bbba1ac28b6da21b4&Timestamp=1546315200&VerificationCode=123456"
         val key = "NcbHqkdiUyITTCGbKnQH"
         L.e(SignatureUtil.signature(sign, key))*/
    }

    override fun initView() {
        openXGPush()
        home_bottom_view.addMenu(
            BottomItemEntity(
                getString(R.string.main_tab_1),
                R.color.main_tab_normal, R.color.main_tab_hover,
                R.mipmap.main_tab_1_normal, R.mipmap.main_tab_1_hover
            )
        )
       /* .addMenu(
            BottomItemEntity(
                getString(R.string.main_tab_2),
                R.color.main_tab_normal, R.color.main_tab_hover,
                R.mipmap.main_tab_2_normal, R.mipmap.main_tab_2_hover
            )
        )*/
        .addMenu(
            BottomItemEntity(
                getString(R.string.main_tab_3),
                R.color.main_tab_normal, R.color.main_tab_hover,
                R.mipmap.main_tab_3_normal, R.mipmap.main_tab_3_hover
            )
        ).showMenu()
        fragments.clear()
        fragments.add(HomeFragment())
//        fragments.add(SceneFragment())
        fragments.add(MeFragment())
        this.supportFragmentManager.beginTransaction()
            .add(R.id.main_container, fragments[0])
            .show(fragments[0])
            .commit()
    }

    override fun setListener() {
        home_bottom_view.setOnItemClickListener { _, position, previewPosition ->
            showFragment(position, previewPosition)
        }
        (fragments[0] as? HomeFragment)?.run {
            popupListener = object : HomeFragment.PopupListener {
                override fun onPopupListener(familyList: List<FamilyEntity>) {
                    this@MainActivity.showFamilyPopup(familyList)
                }
            }
        }
    }

    private fun openXGPush() {
        XGPushManager.registerPush(applicationContext, object : XGIOperateCallback {
            override fun onSuccess(data: Any?, p1: Int) {
                L.e("注册成功，设备token为：$data")
                data?.let {
                    App.data.xg_token = it.toString()
                    bindXG()
                }
            }

            override fun onFail(data: Any?, errCode: Int, msg: String?) {
                L.e("注册失败，错误码：$errCode ,错误信息：$msg")
            }
        })
    }

    /**
     * 绑定信鸽推送
     */
    private fun bindXG() {
        if (TextUtils.isEmpty(App.data.xg_token)) return
        HttpRequest.instance.bindXG(App.data.xg_token, this)
    }

    /**
     * 解绑信鸽推送
     */
    private fun unbindXG() {
        if (TextUtils.isEmpty(App.data.xg_token)) return
        HttpRequest.instance.unbindXG(App.data.xg_token, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
    }

    private fun showFragment(position: Int, previewPosition: Int) {
        StatusBarUtil.setStatusBarDarkTheme(this, true)
        /*if (position == 2) {
            //设置白色状态栏
            StatusBarUtil.setStatusBarDarkTheme(this, false)
        } else {
            //设置黑色状态栏
            StatusBarUtil.setStatusBarDarkTheme(this, true)
        }*/
        val transaction = this.supportFragmentManager.beginTransaction()
        if (fragments[position].isAdded) {
            transaction.show(fragments[position]).hide(fragments[previewPosition]).commit()
        } else {
            transaction.add(R.id.main_container, fragments[position])
                .show(fragments[position]).hide(fragments[previewPosition])
                .commit()
        }
    }

    private fun showFamilyPopup(familyList: List<FamilyEntity>) {
        if (familyPopup == null) {
            familyPopup = FamilyListPopup(this)
            familyPopup?.setList(familyList)
        }
        familyPopup?.setBg(main_bg)
        familyPopup?.show(main)
        familyPopup?.onItemClickListener = object : FamilyListPopup.OnItemClickListener {
            override fun onItemClick(popupWindow: FamilyListPopup, position: Int) {
                popupWindow.dismiss()
                (fragments[0] as? HomeFragment)?.run {
                    tabFamily(position)
                }
            }
        }
        familyPopup?.setOnClickManagerListener(View.OnClickListener {
            jumpActivity(FamilyListActivity::class.java)
            familyPopup?.dismiss()
        })
    }


    override fun onDestroy() {
        unbindXG()
        super.onDestroy()
    }

    private var timestamp = 0L

    /**
     * 连续按两个关闭app
     */
    override fun onBackPressed() {
        val t = System.currentTimeMillis()
        if (t - timestamp < 1000) {
            exitApp()
            App.data.clear()
            exitProcess(0)
        } else {
            timestamp = t
            T.show("再按一下退出应用")
        }
//        super.onBackPressed()
    }

}
