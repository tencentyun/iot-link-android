package com.tencent.iot.explorer.link.demo.video

class AccessInfo: Comparable<AccessInfo> {
    var accessId = ""
    var accessToken = ""
    var productId = ""

    override fun compareTo(other: AccessInfo): Int {
        if (this.accessId.compareTo(other.accessId) == 0
            && this.accessToken.compareTo(other.accessToken) == 0
            && this.productId.compareTo(other.productId) == 0) {
            return 0
        } else {
            return this.accessId.compareTo(other.accessId)
        }
    }

    override fun equals(other: Any?): Boolean {
        other as AccessInfo

        other?.let {
            this?.let {
                if (this.accessId == other.accessId
                    && this.accessToken == other.accessToken
                    && this.productId == other.productId) {
                    return true
                }
            }
        }

        return false
    }
}