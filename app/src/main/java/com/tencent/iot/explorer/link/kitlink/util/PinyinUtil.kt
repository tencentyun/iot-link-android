package com.tencent.iot.explorer.link.kitlink.util

import net.sourceforge.pinyin4j.PinyinHelper

object PinyinUtil {

    /**
     * 获取汉字首字母
     */
    fun getFirstLetter(word: Char) : String? {
        val letterArray = PinyinHelper.toHanyuPinyinStringArray(word)
        return if (letterArray != null) {
            letterArray[0][0].toString()
        } else {
            null
        }
    }
}