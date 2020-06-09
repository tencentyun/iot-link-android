package com.tencent.iot.explorer.link.core.auth.message.upload

open class ParamMap : HashMap<String, Any?>() {

    /**
     * 转化为String
     */
    override fun toString(): String {
        val sb = StringBuilder("{")
        this.keys.forEachIndexed { index, key ->
            sb.append("\"$key").append("\":")
            when (this[key]) {
                is String -> {
                    sb.append("\"${this[key]}\"")
                }
                else -> {
                    sb.append("${this[key]}")
                }
            }
            if (index < this.keys.size - 1) sb.append(",")
        }
        sb.append("}")
        return sb.toString()
    }

}