package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.tencent.iot.explorer.link.BuildConfig
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.http.HttpCallBack
import com.tencent.iot.explorer.link.core.auth.http.HttpUtil
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.KeyBoardUtils
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.kitlink.adapter.HistoryPostionsAdapter
import com.tencent.iot.explorer.link.kitlink.adapter.PostionsAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.Address
import com.tencent.iot.explorer.link.kitlink.entity.LocationResp
import com.tencent.iot.explorer.link.kitlink.entity.Postion
import com.tencent.iot.explorer.link.kitlink.entity.PostionsResp
import kotlinx.android.synthetic.main.activity_family_address.*
import kotlinx.android.synthetic.main.activity_select_point.*
import kotlinx.android.synthetic.main.activity_select_point.ev_search
import kotlinx.android.synthetic.main.activity_select_point.smart_refreshLayout
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.android.synthetic.main.menu_back_layout.tv_title
import java.util.*
import kotlin.collections.ArrayList

class SelectPointActivity : BaseActivity() {

    private var address: MutableList<Address> = ArrayList()
    private var adapter: HistoryPostionsAdapter? = null
    private var postions: MutableList<Postion> = ArrayList()
    private var postionAdapter: PostionsAdapter? = null
    private var pageIndex = 1
    private var PAGE_SIZE = 20

    override fun getContentView(): Int {
        return R.layout.activity_select_point
    }

    override fun initView() {
        tv_title.text = getString(R.string.map_select_postion)
        var linearLayoutManager = LinearLayoutManager(this@SelectPointActivity)
        var addressStr = Utils.getStringValueFromXml(this@SelectPointActivity, CommonField.STORED_ADDRESS, CommonField.STORED_ADDRESS)
        if (!TextUtils.isEmpty(addressStr)) {
            try {
                address = JSONArray.parseArray(addressStr, Address::class.java)
                Collections.reverse(address)
            } catch (e : JSONException) {
                e.printStackTrace()
            }
        }
        adapter = HistoryPostionsAdapter(address)
        lv_history.layoutManager = linearLayoutManager
        lv_history.adapter = adapter

        var postionLinearLayoutManager = LinearLayoutManager(this@SelectPointActivity)
        postionAdapter = PostionsAdapter(postions)
        postionAdapter?.selectPos = -1  // 没有默认选中项
        lv_postion.layoutManager = postionLinearLayoutManager
        lv_postion.adapter = postionAdapter

        refreshHistory()

        smart_refreshLayout.setEnableRefresh(false)
        smart_refreshLayout.setEnableLoadMore(true)
        smart_refreshLayout.setRefreshFooter(ClassicsFooter(this@SelectPointActivity))
        smart_refreshLayout.visibility = View.GONE
    }

    private fun refreshHistory() {
        if (address != null && address.size > 0) {
            lv_history.visibility = View.VISIBLE
            no_history_tip_layout.visibility = View.GONE
        } else {
            lv_history.visibility = View.GONE
            no_history_tip_layout.visibility = View.VISIBLE
        }
    }

    private fun requestMorePostion(region: String, keyword: String) {
        var url = "https://apis.map.qq.com/ws/place/v1/suggestion/?region=${region}&keyword=${keyword}" +
                "&key=${BuildConfig.TencentMapSDKValue}&page_size=${PAGE_SIZE}&page_index=${pageIndex}"
        HttpUtil.get(url, object : HttpCallBack {
            override fun onSuccess(response: String) {
                L.e("地址解析", "response=$response")
                var postionsResp = JSONObject.parseObject(response, PostionsResp::class.java)
                if (postionsResp != null && postionsResp.status == 0) {

                    if (postionsResp.count <= 0) {
                        postions.clear()
                    }

                    // 查询到数据，且获取到的数据量少于总数据量
                    if (postionsResp.count > 0 && postionsResp.data.size > 0 &&
                            (postions.size + postionsResp.data.size) < postionsResp.count) {
                        pageIndex++
                        postions.addAll(postionsResp.data)
                    }

                    postionAdapter?.notifyDataSetChanged()
                }

                if (postions == null || postions.size <= 0) {
                    tv_no_data_tip.visibility = View.VISIBLE
                } else {
                    tv_no_data_tip.visibility = View.GONE
                }

                if (smart_refreshLayout.isLoading) {
                    smart_refreshLayout.finishLoadMore()
                }
            }

            override fun onError(error: String) {
                if (smart_refreshLayout.isLoading) {
                    smart_refreshLayout.finishLoadMore()
                }
            }
        })
    }

    private fun startRequestPage() {
        postions.clear()
        pageIndex = 1
        tip_layout.visibility = View.GONE
        smart_refreshLayout.visibility = View.VISIBLE
        requestMorePostion("", ev_search.text.toString())
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        iv_clear.setOnClickListener { ev_search.setText("") }
        ev_search.setOnEditorActionListener(object: TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                startRequestPage()
                KeyBoardUtils.hideKeyBoard(this@SelectPointActivity, tv_search)
                return false
            }
        })
        ev_search.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s == null) return
                if (s!!.length > 0) {
                    iv_clear.visibility = View.VISIBLE
                    startRequestPage()
                } else {
                    iv_clear.visibility = View.GONE
                    tip_layout.visibility = View.VISIBLE
                    smart_refreshLayout.visibility = View.GONE
                }
            }
        })
        tv_search.setOnClickListener {
            if (TextUtils.isEmpty(ev_search.text.toString())) {
                return@setOnClickListener
            }
            startRequestPage()
            KeyBoardUtils.hideKeyBoard(this@SelectPointActivity, tv_search)
        }
        adapter?.setOnItemClicked(adapterListener)
        smart_refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                requestMorePostion("", ev_search.text.toString())
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                refreshLayout.finishRefresh()
            }
        })
        postionAdapter?.setOnItemClicked(object : PostionsAdapter.OnItemClicked {
            override fun onItemClicked(pos: Int) {
                var curretAddress = Address()
                curretAddress.id = postions.get(pos).id
                curretAddress.address = postions.get(pos).address
                curretAddress.name = postions.get(pos).title
                curretAddress.latitude = postions.get(pos).location!!.lat
                curretAddress.longitude = postions.get(pos).location!!.lng

                var it = address.iterator()
                while (it.hasNext()) {
                    var item = it.next()
                    if (item.id == curretAddress.id) {
                        it.remove()
                        break
                    }
                }

                address.add(curretAddress)
                var jsonStr = JSON.toJSONString(address)
                Utils.setXmlStringValue(this@SelectPointActivity, CommonField.STORED_ADDRESS, CommonField.STORED_ADDRESS, jsonStr)
                val data = Intent()
                data.putExtra(CommonField.EXTRA_INFO, JSON.toJSONString(curretAddress))
                setResult(RESULT_OK, data)
                finish()
            }
        })
    }

    private var adapterListener = object : HistoryPostionsAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int) {

            val data = Intent()
            data.putExtra(CommonField.EXTRA_INFO, JSON.toJSONString(address.get(pos)))
            setResult(RESULT_OK, data)
            finish()
        }

        override fun onClearAllHistory() {
            Utils.clearXmlStringValue(this@SelectPointActivity, CommonField.STORED_ADDRESS, CommonField.STORED_ADDRESS)
            address.clear()
            refreshHistory()
        }
    }
}