package com.tradingsim.domain.model;

/**
 * 游戏会话状态枚举
 * 
 * @author TradingSim Team
 */
public enum SessionStatus {
    /**
     * 已创建，等待开始
     */
    CREATED,
    
    /**
     * 运行中
     */
    RUNNING,
    
    /**
     * 已暂停
     */
    PAUSED,
    
    /**
     * 已完成
     */
    COMPLETED,
    
    /**
     * 已取消
     */
    CANCELLED
}