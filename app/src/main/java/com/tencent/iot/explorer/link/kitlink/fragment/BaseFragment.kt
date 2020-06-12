package com.tencent.iot.explorer.link.kitlink.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.tencent.iot.explorer.link.kitlink.App
import com.tencent.iot.explorer.link.kitlink.activity.MainActivity
import com.tencent.iot.explorer.link.kitlink.util.DataHolder
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.util.L
import com.tencent.iot.explorer.link.util.T

abstract class BaseFragment : Fragment() {

    /**
     * 数据共享角色
     */
    private val role by lazy {
        DataHolder.instance.register(this.activity!!)
    }

    /**
     * 存放数据
     */
    fun put(key: String, any: Any) {
        role.put(key, any)
    }

    /**
     * 移除数据
     */
    fun remove(key: String) {
        role.remove(key)
    }

    /**
     * 获得数据
     */
    fun <T> get(key: String): T? {
        return role.get(key)
    }

    /**
     * 放弃某个字段的管理权
     */
    fun giveUp(key: String) {
        role.giveUp(key)
    }

    /**
     * 获取某个字段的管理权
     */
    fun pickUp(key: String) {
        role.pickUp(key)
    }

    abstract fun getPresenter(): IPresenter?

    @LayoutRes
    abstract fun getContentView(): Int

    abstract fun startHere(view: View)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (getContentView() != 0) {
            return inflater.inflate(getContentView(), container, false)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun backToMain() {
        var stop = false
        while (!stop) {
            if (App.data.activityList.last is MainActivity) {
                stop = true
            } else {
                App.data.activityList.last.finish()
                App.data.activityList.removeLast()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.let {
            startHere(it)
        }
    }

    fun show(text: String?) {
        if (TextUtils.isEmpty(text)) return
        activity!!.runOnUiThread {
            T.show(text)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        getPresenter()?.onDestroy()
        //清除管理权限内的DataHolder中存放的数据
        DataHolder.instance.unregister(this)
    }

    open fun jumpActivity(clazz: Class<*>) {
        startActivity(Intent(this.activity, clazz))
    }

    fun dp2px(dp: Int): Int {
        return (context!!.resources.displayMetrics.density * dp + 0.5).toInt()
    }

}