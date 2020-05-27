package com.kitlink.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import com.kitlink.R
import com.kitlink.entity.RecommDeviceEntity
import com.kitlink.response.BaseResponse
import com.kitlink.response.RecommDeviceListResponse
import com.kitlink.util.HttpRequest
import com.kitlink.util.JsonManager
import com.kitlink.util.MyCallback
import com.kitlink.util.RequestCode
import com.mvp.IPresenter
import com.squareup.picasso.Picasso
import com.util.L


class DeviceFragment : BaseFragment, MyCallback{

    var mContext : Context
    constructor(c: Context) {
        mContext = c
    }

    var devicesGridView : GridView? = null

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_devices
    }

    override fun startHere(view: View) {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val categoryKey = arguments!!.getString("CategoryKey", "CategoryKey")
        val view = inflater.inflate(getContentView(), container, false)
        if (getContentView() != 0) {
            devicesGridView = view.findViewById(R.id.gv_devices)
            HttpRequest.instance.getRecommList(categoryKey, this)
        }
        return view
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.get_recommend_device_list -> {
                if (response.isSuccess()) {
                    response.parse(RecommDeviceListResponse::class.java)?.run {
                        L.e("推荐设备分类列表：${JsonManager.toJson(CategoryList)}")
                        devicesGridView!!.adapter = GridAdapter(mContext, CategoryList)
                    }
                }
            }
        }
    }

    class GridAdapter : BaseAdapter {
        var recommDeviceList : List<RecommDeviceEntity>? = null
        var context : Context? = null
        var inflater : LayoutInflater? = null

        constructor(contxt : Context, list : List<RecommDeviceEntity>) {
            context = contxt
            recommDeviceList = list
            inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder : ViewHolder
            var retView : View
            if (convertView == null) {
                viewHolder = ViewHolder()
                retView = LayoutInflater.from(parent?.context). inflate(R.layout.device_item, parent, false)
                viewHolder.image = retView.findViewById(R.id.iv_device_icon)
                viewHolder.text = retView.findViewById(R.id.tv_device_name)
                retView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
                retView = convertView
            }

            viewHolder.text.text = recommDeviceList?.get(position)?.CategoryName
            val url = recommDeviceList?.get(position)?.IconUrl
            Picasso.with(context).load(url).into(viewHolder.image)
            return retView
        }

        override fun getItem(position: Int): Any? {
            return recommDeviceList?.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return recommDeviceList?.size?: 0
        }

        inner class ViewHolder {
            lateinit var image: ImageView
            lateinit var text: TextView
        }
    }

}