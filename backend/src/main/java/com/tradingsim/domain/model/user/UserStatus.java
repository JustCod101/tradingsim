package com.tradingsim.domain.model.user;

/**
 * 用户状态枚举
 * 
 * @author TradingSim Team
 */
public enum UserStatus {
    /**
     * 活跃状态
     */
    ACTIVE("ACTIVE", "活跃"),
    
    /**
     * 非活跃状态
     */
    INACTIVE("INACTIVE", "非活跃"),
    
    /**
     * 暂停状态
     */
    SUSPENDED("SUSPENDED", "暂停"),
    
    /**
     * 已删除状态
     */
    DELETED("DELETED", "已删除");
    
    private final String code;
    private final String description;
    
    UserStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static UserStatus fromCode(String code) {
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown user status code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}