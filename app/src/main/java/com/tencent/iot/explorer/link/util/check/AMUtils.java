package com.tencent.iot.explorer.link.util.check;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *验证手机号码及邮箱是否正确
 */
public class AMUtils {

    /**
     * 手机号正则表达式
     **/
    private final static String MOBILE_PHONE_PATTERN = "^((13[0-9])|(15[0-9])|(18[0-9])|(14[0-9])|(17[0-9]))\\d{8}$";
    /**
     * 固定电话
     */
    private static final String REG_PATTERN = "(?:(\\(\\+?86\\))(0[0-9]{2,3}\\-?)?([2-9][0-9]{6,7})+(\\-[0-9]{1,4})?)|" +
            "(?:(86-?)?(0[0-9]{2,3}\\-?)?([2-9][0-9]{6,7})+(\\-[0-9]{1,4})?)";

    /**
     * 通过正则验证是否是合法手机号码
     *
     * @param phoneNumber
     * @return：true:合法,false：不合法
     */
    public static boolean isMobile(String phoneNumber) {
        Pattern p = Pattern.compile(MOBILE_PHONE_PATTERN);
        Matcher m = p.matcher(phoneNumber);
        return m.matches();
    }

    /**
     * 验证邮箱
     */
    public static boolean isEmail(String email) {
        return email.contains("@");
    }

    /**
     * 验证固定电话
     */
    public static boolean isPhone(String phone) {
        return Pattern.matches(REG_PATTERN, phone);
    }


}
