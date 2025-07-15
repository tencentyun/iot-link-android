package com.tencent.iot.explorer.link.demo.core.adapter

import androidx.viewbinding.ViewBinding
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder

abstract class ListHolder<T : Any, VB: ViewBinding>(binding: VB) : BaseHolder<T, VB>(binding) {
}