package com.yezishuo.transfer.enums;

public enum UserRole {
    GUEST("游客", 0),
    NORMAL("普通账号", 1),
    SUPER("超级账号", 2);

    private String name;
    private int level;

    UserRole(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }
}