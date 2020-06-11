package com.tencent.iot.explorer.link.auth.message.upload

class ArrayString {

    private var array = ArrayList<String>()

    constructor()

    constructor(string: String) {
        array.add(string)
    }

    fun addValue(value: String) {
        if (!contains(value))
            array.add(value)
    }

    fun contains(value: String): Boolean {
        array.forEach {
            if (value == it) {
                return true
            }
        }
        return false
    }

    fun getValue(position: Int): String {
        return array[position]
    }

    fun size(): Int {
        return array.size
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

    fun toJson(): String {
        val sb = StringBuilder("[\"")
        array.forEachIndexed { index, s ->
            sb.append(s).append("\"")
            if (index < array.size - 1)
                sb.append(",\"")
        }
        sb.append("]")
        return sb.toString()
    }

    fun remove(value: String) {
        array.remove(value)
    }
}