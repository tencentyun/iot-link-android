package com.tencent.iot.explorer.link.kitlink.util

//import net.sourceforge.pinyin4jj.PinyinHelper
import com.github.promeg.pinyinhelper.Pinyin

object PinyinUtil {

    /**
     * 获取汉字首字母
     */
//    fun getFirstLetter(word: Char) : String? {
//        val letterArray = PinyinHelper.toHanyuPinyinStringArray(word)
//        return if (letterArray != null) {
//            letterArray[0][0].toString()
//        } else {
//            null
//        }
//    }

    fun getFirstLetter(word: Char): String? {
        return if (Pinyin.isChinese(word)) {
            Pinyin.toPinyin(word)[0].toString() // TinyPinyin 返回大写字母
        } else {
            null
        }
    }
}