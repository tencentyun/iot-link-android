package com.tencent.iot.explorer.link.core.utils

import java.util.concurrent.CountDownLatch

class OneOffLock(limit: Int) {
    var limit = 1
    var lock = CountDownLatch(limit)
    @Volatile
    var checkRet = false

    init {
        this.limit = limit
        lock = CountDownLatch(this.limit)
        checkRet = false
    }

    fun await() {
        this.lock.await()
    }

    fun countDown() {
        this.lock.countDown()
    }

}