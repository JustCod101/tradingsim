package com.tradingsim.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * 创建游戏会话请求DTO
 * 
 * @author TradingSim Team
 */
public class CreateSessionRequest {
    
    @NotBlank(message = "股票代码不能为空")
    private String stockCode;
    
    @NotBlank(message = "时间框架不能为空")
    private String timeframe;
    
    @NotNull(message = "初始余额不能为空")
    @Positive(message = "初始余额必须大于0")
    private BigDecimal initialBalance;
    
    public CreateSessionRequest() {}
    
    public CreateSessionRequest(String stockCode, String timeframe, BigDecimal initialBalance) {
        this.stockCode = stockCode;
        this.timeframe = timeframe;
        this.initialBalance = initialBalance;
    }
    
    public String getStockCode() {
        return stockCode;
    }
    
    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }
    
    public String getTimeframe() {
        return timeframe;
    }
    
    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }
    
    public BigDecimal getInitialBalance() {
        return initialBalance;
    }
    
    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
    
    @Override
    public String toString() {
        return "CreateSessionRequest{" +
                "stockCode='" + stockCode + '\'' +
                ", timeframe='" + timeframe + '\'' +
                ", initialBalance=" + initialBalance +
                '}';
    }
}