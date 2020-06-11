package com.tencent.iot.explorer.link.kitlink.util

class ArrayString {

    private var array = ArrayList<String>()

    constructor()

    constructor(string: String) {
        array.add(string)
    }

    fun addValue(value: String) {
        array.add(value)
    }

    fun isNotEmpty(): Boolean {
        return array.size > 0
    }

    fun clear() {
        array.clear()
    }

    override fun toString(): String {
        val sb = StringBuilder("[\"")
        array.forEachIndexed { index, s ->
            sb.append(s).append("\"")
            if (index < array.size - 1)
                sb.append(",\"")
        }
        sb.append("]")
        return sb.toString()
    }
}