package com.tencent.iot.explorer.link.customview.progress.bean;

public class StepBean
{

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StepBean() {
        this("");
    }

    public StepBean(String name) {
        this.name = name;
    }
}
