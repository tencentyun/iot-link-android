package com.tenext.auth.entity

import com.tenext.auth.util.JsonManager

/**
 * 面板实体
 */
class PanelData {

    var ProductId = ""
    var Config = ""

    var panelConfig = PanelConfig()

    fun parse(): PanelConfig {
        JsonManager.parseJson(Config, PanelConfig::class.java)?.let {
            panelConfig = it
        }
        return panelConfig
    }

}





