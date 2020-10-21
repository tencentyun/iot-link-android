package com.tencent.iot.explorer.link.core.auth.entity


/**
 * 家庭实体
 */
class FamilyEntity {
    var FamilyId = ""
    var FamilyName = ""
    var Role = 0   // 1:自己是管理员  0：普通成员
    var CreateTime = 0L
    var UpdateTime = 0L
}