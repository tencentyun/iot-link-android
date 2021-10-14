package com.tencent.iot.explorer.link.core.link.entity

class BleDevSignResult: BleDevBindCondition() {

    companion object {
        fun data2BleDevSignResult(data: ByteArray) : BleDevSignResult{
            var bleDevSignResult = BleDevSignResult()
            bleDevSignResult.data = data
            return bleDevSignResult
        }
    }
}