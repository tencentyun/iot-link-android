package com.tencent.iot.explorer.link.demo.core.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import kotlinx.android.synthetic.main.item_family.view.*

class FamilyHolder : BaseHolder<FamilyEntity> {

    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        itemView.tv_family_name.text = data.FamilyName
        itemView.tv_family_name.setTextColor(
            if (App.data.getCurrentFamily().FamilyId == data.FamilyId) {
                getColor(R.color.blue_006EFF)
            } else {
                getColor(R.color.black_333333)
            }
        )
        itemView.tv_family_name.setOnClickListener {
            clickItem(this, it, position)
        }
    }
}