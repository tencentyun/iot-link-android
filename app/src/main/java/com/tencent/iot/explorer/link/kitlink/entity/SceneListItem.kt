package com.tencent.iot.explorer.link.kitlink.entity

import com.alibaba.fastjson.JSONArray

class SceneListItem {
    var SceneId = ""
    var Status = 0
    var FilterType = ""
    var SceneName = ""
    var SceneIcon = ""
    var Actions: JSONArray? = null
    var Flag = 0
}