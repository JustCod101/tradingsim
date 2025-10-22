package com.tradingsim.domain.model;

/**
 * 交易决策类型枚举
 * 
 * @author TradingSim Team
 */
public enum DecisionType {
    /**
     * 买入
     */
    BUY,
    
    /**
     * 卖出
     */
    SELL,
    
    /**
     * 跳过/不操作
     */
    SKIP
}