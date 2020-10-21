package com.tencent.iot.explorer.link.kitlink.popup

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.kitlink.holder.PopupFamilyListHolder
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.popup_family.view.*

/**
 * 家庭列表弹框
 */
class FamilyListPopup(context: Context) : ParentPopupWindow(context),
    CRecyclerView.RecyclerItemView {

    var onItemClickListener: OnItemClickListener? = null

    override fun getLayoutId(): Int {
        return R.layout.popup_family
    }

    override fun getAnimation(): Int {
        return R.style.PopupWindowFamily
    }

    override fun initView() {
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    fun setList(list: List<FamilyEntity>) {
        contentView.crv_popup_family_list.setList(list)
        contentView.crv_popup_family_list.addRecyclerItemView(this)
    }

    fun setOnClickManagerListener(listener: View.OnClickListener) {
        contentView.tv_popup_family_list.setOnClickListener(listener)
    }

    override fun show(parentView: View) {
        super.show(parentView)
        super.showAtLocation(parentView, Gravity.TOP, 0, 0)
        contentView.crv_popup_family_list.notifyDataChanged()
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        contentView.crv_popup_family_list.addSingleSelect(position)
        contentView.crv_popup_family_list.notifyDataChanged()
        onItemClickListener?.onItemClick(this, position)
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return PopupFamilyListHolder(
            context,
            contentView.crv_popup_family_list,
            R.layout.item_popup_family_list
        )
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

    interface OnItemClickListener {
        fun onItemClick(popupWindow: FamilyListPopup, position: Int)
    }

}