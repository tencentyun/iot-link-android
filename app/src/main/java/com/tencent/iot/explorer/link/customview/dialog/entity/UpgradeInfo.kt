package com.tencent.iot.explorer.link.customview.dialog.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.kitlink.util.picture.utils.FileUtils
import java.text.SimpleDateFormat
import java.util.*

class UpgradeInfo(title: String, log: String, version: String) {

    var title = ""
    var log = ""
    var version = ""
    var packageSzie = ""
    var publishTime = ""
    var url = ""
    var upgradeType = 2  // 2:静默更新不提示 1:强制升级 0:用户确认

    init {
        this.title = title
        this.log = log
        this.version = version
    }

    constructor(): this("", "", "")

    companion object {
        private val KEY_REP_VER = "VersionInfo"
        private val KEY_TITLE = "Title"
        private val KEY_APP_VERSION = "AppVersion"
        private val KEY_URL = "DownloadURL"
        private val KEY_SIZE = "PackageSize"
        private val KEY_LOG = "Content"
        private val KEY_RELEASE_TIME = "ReleaseTime"
        private val KEY_CHANNEL = "Channel"
        private val KEY_UPGRADE_TYPE = "UpgradeType"

        fun convertJson2UpgradeInfo(jsonSrc: JSONObject): UpgradeInfo?{
            var ret = UpgradeInfo()

            if (jsonSrc == null) return null

            var json = jsonSrc

            if (json.containsKey(KEY_REP_VER)) {
                json = json.getJSONObject(KEY_REP_VER)
            }

            if (json.containsKey(KEY_TITLE)) {
                ret.title = json.getString(KEY_TITLE)
            }

            if (json.containsKey(KEY_APP_VERSION)) {
                ret.version = json.getString(KEY_APP_VERSION)
            }

            if (json.containsKey(KEY_URL)) {
                ret.url = json.getString(KEY_URL)
            }

            if (json.containsKey(KEY_SIZE)) {
                var size = json.getLong(KEY_SIZE)
                ret.packageSzie = FileUtils.FormetFileSize(size)
            }

            if (json.containsKey(KEY_LOG)) {
                ret.log = json.getString(KEY_LOG)
            }

            if (json.containsKey(KEY_RELEASE_TIME)) {
                var time = json.getLong(KEY_RELEASE_TIME)
                time = time * 1000L
                val date = Date(time)
                val format = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                ret.publishTime = format.format(date)
            }

            if (json.containsKey(KEY_UPGRADE_TYPE)) {
                ret.upgradeType = json.getIntValue(KEY_UPGRADE_TYPE)
            }

            return ret
        }

        fun convertJson2UpgradeInfo(jsonStr: String): UpgradeInfo?{
            var json = JSON.parse(jsonStr) as JSONObject
            return convertJson2UpgradeInfo(json)
        }
    }
}