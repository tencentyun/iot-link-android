package com.tenext.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tenext.demo.holder.BaseHolder

abstract class ListHolder<T : Any> : BaseHolder<T> {
    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)
}