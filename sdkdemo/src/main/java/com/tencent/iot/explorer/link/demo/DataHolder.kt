package com.tencent.iot.explorer.link.demo

/**
 * 数据执有者
 */
class DataHolder private constructor() {

    companion object {

        val instance = Data.holder

    }

    object Data {
        val holder = DataHolder()
    }

    /**
     * 数据列表
     */
    private val dataMap = HashMap<String, Any>()

    private val roleList by lazy {
        hashSetOf<DataRole>()
    }

    /**
     * 注册管理者
     */
    fun register(any: Any): DataRole {
        contains(any)?.run {
            return this
        }
        val role = DataRole(this, any)
        roleList.add(role)
        return role
    }

    /**
     * 反注册管理者
     */
    fun unregister(any: Any) {
        contains(any)?.run {
            this.clear()
            roleList.remove(this)
        }
    }

    private fun contains(any: Any): DataRole? {
        run out@{
            roleList.forEach {
                if (it.owner == any) {
                    return it
                }
            }
        }
        return null
    }

    /**
     * 存放数据
     */
    fun put(key: String, any: Any) {
        dataMap[key] = any
        dataMap.remove(key)
    }

    /**
     * 获得数据
     */
    fun <T> get(key: String): T? {
        (dataMap[key] as? T)?.run {
            return this
        }
        return null
    }

    /**
     * 移除key
     */
    fun remove(key: String) {
        dataMap.remove(key)
    }

    /**
     * 清除数据
     */
    fun clear() {
        dataMap.clear()
    }

    class DataRole(holder: DataHolder, any: Any) {

        val owner = any

        private val holder = holder

        private val keyList by lazy {
            hashSetOf<String>()
        }

        /**
         * 存放数据
         */
        fun put(key: String, any: Any) {
            if (!keyList.contains(key))
                keyList.add(key)
            holder.dataMap[key] = any
        }

        /**
         * 更新数据，不存在的key无法更新
         */
        fun update(key: String, any: Any) {
            if (holder.dataMap.containsKey(key)) {
                holder.dataMap[key] = any
            }
        }

        /**
         * 获得数据
         */
        fun <T> get(key: String): T? {
            (holder.dataMap[key] as? T)?.run {
                return this
            }
            return null
        }

        /**
         * 移除key
         */
        fun remove(key: String) {
            keyList.remove(key)
            holder.dataMap.remove(key)
        }

        /**
         * 清除数据
         */
        fun clear() {
            keyList.forEach {
                holder.dataMap.remove(it)
            }
            keyList.clear()
        }

        /**
         * 放弃某个字段的管理权
         */
        fun giveUp(key: String) {
            if (keyList.contains(key))
                keyList.remove(key)
        }

        /**
         * 获取某个字段的管理权
         */
        fun pickUp(key: String) {
            keyList.add(key)
        }

    }

}
