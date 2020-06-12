package com.tencent.iot.explorer.link.core.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder

abstract class FooterHolder :BaseHolder<Any>{
    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)
}