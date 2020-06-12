package com.tencent.iot.explorer.link.util.string;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * 特殊数据处理工具
 * Created by THINK on 2017/5/2.
 */

public class DataUtils {

    /**
     * 根据字符串获取当前首字母
     *
     * @param name：需要截取的汉字
     * @param type：格式，type == 0 ：返回小写，type > 0 ：返回大写
     * @return 首写字母,如果传入的不是正常的汉字，则返回 ‘#’
     */
    public static String getLetter(String name, int type) {
        String DefaultLetter = "#";
        if (TextUtils.isEmpty(name)) {
            return DefaultLetter;
        }
        char char0 = name.toLowerCase().charAt(0);
        if (Character.isDigit(char0)) {
            return DefaultLetter;
        }
        ArrayList<HanziToPinyin.Token> l = HanziToPinyin.getInstance().get(name.substring(0, 1));

        if (l != null && l.size() > 0 && l.get(0).target.length() > 0) {
            HanziToPinyin.Token token = l.get(0);
            // toLowerCase()返回小写， toUpperCase()返回大写
            String letter;
            char a;
            char z;
            if (type == 0) {
                letter = token.target.substring(0, 1).toLowerCase();
                a = 'a';
                z = 'z';
            } else {
                letter = token.target.substring(0, 1).toUpperCase();
                a = 'A';
                z = 'Z';
            }
            char c = letter.charAt(0);
            // 这里的 'a' 和 'z' 要和letter的大小写保持一直。
            if (c < a || c > z) {
                return DefaultLetter;
            }
            return letter;
        }
        return DefaultLetter;
    }
}
