package com.tenext.auth.entity

import com.alibaba.fastjson.JSONObject

class BoolDefine : ProductDefine() {


    var mapping: JSONObject? = null

    override fun getText(value: String): String {
        return mapping?.getString(value) ?: ""
    }

    fun parseList(): ArrayList<Mapping> {
        val list = arrayListOf<Mapping>()
        mapping?.keys?.forEachIndexed { _, k ->
            list.add(Mapping(mapping!!.getString(k), k))
        }
        return list
    }
}