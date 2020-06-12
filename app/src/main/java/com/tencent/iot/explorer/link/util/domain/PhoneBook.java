package com.tencent.iot.explorer.link.util.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 手机通讯录
 * Created by THINK on 2017/6/17.
 */

public class PhoneBook  implements Serializable {

    private List<Data> data;

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public static class Data implements Serializable {
        public String name;
        public String phone;


        public Data() {
        }

        public Data(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }
    }
}
