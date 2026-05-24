package com.yezishuo.transfer.enums;

public enum DirectionEnum {
    OUT("out", "调出"),
    IN("in", "调入");

    private String code;
    private String desc;

    DirectionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}