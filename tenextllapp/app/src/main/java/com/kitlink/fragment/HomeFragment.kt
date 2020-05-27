package com.kitlink.fragment

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kitlink.App
import com.kitlink.R
import com.kitlink.activity.AddDeviceActivity
import com.kitlink.activity.ControlPanelActivity
import com.kitlink.activity.DeviceCategoryActivity
import com.kitlink.entity.DeviceEntity
import com.kitlink.entity.FamilyEntity
import com.kitlink.holder.*
import com.mvp.IPresenter
import com.mvp.presenter.HomeFragmentPresenter
import com.mvp.view.HomeFragmentView
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.util.L
import com.view.recyclerview.CRecyclerDivider
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.head_home.*

/**
 * 设备界面
 */
class HomeFragment : BaseFragment(), HomeFragmentView, CRecyclerView.RecyclerItemView {

    private lateinit var presenter: HomeFragmentPresenter

    private lateinit var header1: HomeHeadViewHolder1
    private lateinit var header2: HomeHeadViewHolder2
    private lateinit var header3: HomeHeadViewHolder3
    var popupListener: PopupListener? = null

    private var mScrollY = 0

    override fun getContentView(): Int {
        return R.layout.fragment_home
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun startHere(view: View) {
        presenter = HomeFragmentPresenter(this)
        initView()
        setListener()
        //请求数据
//        requestData()
    }

    override fun onResume() {
        super.onResume()
        if (App.data.refresh) {//更新数据
            requestData()
        } else {//更新界面
            showData()
        }
    }

    /**
     * fragment可见状态(首次创建时不调用)
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        when (hidden) {
            true -> {//隐藏

            }
            else -> {//显示

            }
        }
    }

    /**
     * 请求数据
     */
    private fun requestData() {
        when (App.data.getRefreshLevel()) {
            0 -> presenter.refreshFamilyList()
            1 -> presenter.refreshRoomList()
            2 -> presenter.refreshDeviceList()
        }
        //级别降为设备刷新
        App.data.resetRefreshLevel()
    }

    /**
     * 更新界面
     */
    private fun showData() {
        presenter.model!!.run {
            showFamily()
            showRoomList()
            showDeviceList(App.data.deviceList.size, roomId, deviceListEnd, shareDeviceListEnd)
        }
    }

    private fun initView() {
        crv_home_fragment.layoutManager = LinearLayoutManager(context)
        val myDivider = CRecyclerDivider(dp2px(16), dp2px(16), dp2px(16))
        myDivider.linearItemOffsetsListener = object : CRecyclerDivider.LinearItemOffsetsListener {
            override fun setItemOffsets(position: Int, viewType: Int): Boolean {
                //头部时自定义
                return (viewType >= CRecyclerView.HEAD_VIEW_TYPE)
            }

            override fun itemOffsets(position: Int, viewType: Int): Rect {
                return Rect(0, 0, 0, 0)
            }
        }
        crv_home_fragment.addItemDecoration(myDivider)
        crv_home_fragment.setList(App.data.deviceList)
        crv_home_fragment.addRecyclerItemView(this)
        header1 = HomeHeadViewHolder1(
            LayoutInflater.from(context).inflate(
                R.layout.head_home1,
                crv_home_fragment, false
            )
        )
        header2 = HomeHeadViewHolder2(
            LayoutInflater.from(context).inflate(
                R.layout.head_home2,
                crv_home_fragment, false
            )
        )
        header3 = HomeHeadViewHolder3(context!!, this, crv_home_fragment, R.layout.head_home3)
        header3.setRoomList(App.data.roomList)
        crv_home_fragment.addHeader(header2)
        addHomeHead()
    }

    /**
     * 显示滑动头部
     */
    private fun addHomeHead() {
        crv_head_home_room.layoutManager =
            LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        crv_head_home_room.setList(App.data.roomList)
        crv_head_home_room.addRecyclerItemView(object : CRecyclerView.RecyclerItemView {
            override fun doAction(
                viewHolder: CRecyclerView.CViewHolder<*>,
                clickView: View,
                position: Int
            ) {
                crv_head_home_room.addSingleSelect(position)
                presenter.tabRoom(position)
                showRoomList()
            }

            override fun getViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): CRecyclerView.CViewHolder<*> {
                return HomeRoomViewHolder(context!!, parent, R.layout.item_home_room)
            }

            override fun getViewType(position: Int): Int {
                return 0
            }
        })
    }

    private fun setListener() {
        header1.headListener = object : CRecyclerView.HeadListener {
            override fun doAction(
                holder: CRecyclerView.HeadViewHolder<*>,
                clickView: View,
                position: Int
            ) {
                when (position) {
                    0 -> jumpActivity(AddDeviceActivity::class.java)
                    1 -> {
                    }
                    2 -> {
                        popupListener?.onPopupListener(App.data.familyList)
                    }
                }
            }
        }
        header2.headListener = object : CRecyclerView.HeadListener {
            override fun doAction(
                holder: CRecyclerView.HeadViewHolder<*>,
                clickView: View,
                position: Int
            ) {
                when (position) {
//                    0 -> jumpActivity(AddDeviceActivity::class.java)
                    0 -> jumpActivity(DeviceCategoryActivity::class.java)
                    1 -> {
                    }
                    2 -> {
                        popupListener?.onPopupListener(App.data.familyList)
                    }
                }
            }
        }
        crv_home_fragment.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                mScrollY += dy
                if (mScrollY < 0) {
                    mScrollY = 0
                }
                isShowHeadHome()
            }
        })
        srl_home_fragment.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                refreshLayout.finishLoadMore()
                presenter.loadDeviceList()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                refreshLayout.finishRefresh()
                presenter.refreshFamilyList()
            }
        })
    }

    /**
     * 切换家庭
     */
    fun tabFamily(position: Int) {
        presenter.tabFamily(position)
    }

    fun tabRoom(position: Int) {
        presenter.tabRoom(position)
    }

    /**
     * 显示家庭名称
     */
    override fun showFamily() {
        header1.show()
        header2.show()
        L.e("显示家庭名称")
        crv_home_fragment.notifyDataChanged()
    }

    /**
     * 显示房间
     */
    override fun showRoomList() {
        crv_head_home_room.notifyDataChanged()
        header3.show()
    }

    private fun isShowHeadHome() {
        if (mScrollY >= dp2px(104)) {
            head_home.visibility = View.VISIBLE
        } else {
            head_home.visibility = View.GONE
        }
    }

    /**
     * 显示设备列表
     */
    override fun showDeviceList(
        deviceSize: Int,
        roomId: String,
        deviceListEnd: Boolean,
        shareDeviceListEnd: Boolean
    ) {
        if (deviceSize > 0) {
            crv_home_fragment.removeHeader(header2)
            crv_home_fragment.addHeader(header1)
            crv_home_fragment.addHeader(header3)
        } else if (roomId == "") {//默认房间时,没有设备，则家庭都没有设备
            crv_home_fragment.removeHeader(header1)
            crv_home_fragment.removeHeader(header3)
            crv_home_fragment.addHeader(header2)
        } else {

        }
        if (deviceSize <= 0 && head_home.visibility == View.VISIBLE) {
            mScrollY = 0
            isShowHeadHome()
        }
        crv_home_fragment.notifyDataChanged()
    }

    override fun showDeviceOnline() {
        crv_home_fragment.notifyDataChanged()
    }

    override fun getViewType(position: Int): Int {
        return when (presenter.getDeviceEntity(position).shareDevice) {
            true -> if (presenter.getDeviceEntity(position).DeviceId == "title") 2 else 1 //共享的设备
            false -> 0
        }
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return when (viewType) {
            2 -> object : CRecyclerView.CViewHolder<DeviceEntity>(
                context!!,
                parent,
                R.layout.item_share_device_title
            ) {
                override fun show(position: Int) {
                }
            }
            1 -> ShareDeviceViewHolder(context!!, parent, R.layout.item_share_device)
            else -> DeviceViewHolder(context!!, parent, R.layout.item_device)
        }
    }

    /**
     * 点击跳转
     */
    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        put("device", presenter.getDeviceEntity(position))
        jumpActivity(ControlPanelActivity::class.java)
    }

    interface PopupListener {
        fun onPopupListener(familyList: List<FamilyEntity>)
    }
}