package com.tencent.iot.explorer.link.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import com.tencent.iot.explorer.link.util.domain.PhoneBook;


/**
 * 读取手机联系人
 * 使用方法：
 * 1.new 一个本类对象
 * 2.可获取手机中的联系
 * 3.可获取sim卡中的联系人
 * 4.可获取所有联系人（手机 + sim卡）
 */
public class PhoneContactUtils {
    private Context context;

    /**
     * 获取库Phon表字段
     **/
    private static final String[] PHONES_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.
            CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Photo.PHOTO_ID,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID};

    /**
     * 联系人显示名称
     **/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;

    /**
     * 电话号码
     **/
    private static final int PHONES_NUMBER_INDEX = 1;

    /**
     * 联系人名称
     **/
    private ArrayList<String> mContactsName = new ArrayList<>();

    /**
     * 联系人手机号码
     **/
    private ArrayList<String> mContactsNumber = new ArrayList<>();

    /**
     * 姓名+手机号码的对象集合
     */
    private ArrayList<PhoneBook.Data> books = new ArrayList<>();

    /**
     * 返回手机所有的联系人：包含：SIM卡和手机通讯录
     *
     * @return
     */
    public List<String> getContacts() {
        return mContactsNumber;
    }

    /**
     * 返回手机所有的联系人对象：姓名+ 手机号码
     */
    public List<PhoneBook.Data> getPhoneBooks() {
        return books;
    }

    public PhoneContactUtils(Context context) {
        this.context = context;
        getPhoneContacts();
        getSIMContacts();
    }

    /**
     * 得到手机通讯录联系人信息
     **/
    private void getPhoneContacts() {

        ContentResolver resolver = context.getContentResolver();
        // 获取手机联系人
        Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONES_PROJECTION,
                null, null, null);
        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                //得到手机号码
                String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                //当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber))
                    continue;
                //去掉电话号码内的+86 86
                phoneNumber =  formatParentID(phoneNumber);

                //得到联系人名称
                String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
                if (TextUtils.isEmpty(contactName)) {
                    contactName = "";
                }
                mContactsName.add(contactName);
                mContactsNumber.add(phoneNumber);
                books.add(new PhoneBook.Data(contactName, phoneNumber));
            }
            phoneCursor.close();
        }
    }

    /**
     * 得到手机SIM卡联系人人信息
     **/
    private void getSIMContacts() {
        ContentResolver resolver = context.getContentResolver();
        // 获取Sims卡联系人
        Uri uri = Uri.parse("content://icc/adn");
        Cursor phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null, null);
        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {

                // 得到手机号码
                String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                // 当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber))
                    continue;
                // 得到联系人名称
                String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
                if (TextUtils.isEmpty(contactName)) {
                    contactName = "";
                }
                //Sim卡中没有联系人头像
                mContactsName.add(contactName);
                mContactsNumber.add(phoneNumber);
                books.add(new PhoneBook.Data(contactName, phoneNumber));
            }

            phoneCursor.close();
        }
    }
    /**
     *
     * @author zhangbin
     * @2016-1-31
     * @param phoneNum
     * @descript:去掉+86或86
     */
    public static String formatParentID(String phoneNum){
        phoneNum = phoneNum.replaceAll("\\s*", "");//替换大部分空白字符， 不限于空格
        if(phoneNum.startsWith("+86")){
            phoneNum = phoneNum.substring(3, phoneNum.length());
        }else if(phoneNum.startsWith("86")){
            phoneNum = phoneNum.substring(2, phoneNum.length());
        }
        return phoneNum;
    }
}
