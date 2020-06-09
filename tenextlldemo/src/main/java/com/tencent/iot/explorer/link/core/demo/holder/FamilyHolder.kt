package com.tenext.demo.holder

import android.content.Context
import android.view.ViewGroup
import com.tenext.auth.entity.Family
import com.tenext.demo.App
import com.tenext.demo.R
import kotlinx.android.synthetic.main.item_family.view.*

class FamilyHolder : BaseHolder<Family> {

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