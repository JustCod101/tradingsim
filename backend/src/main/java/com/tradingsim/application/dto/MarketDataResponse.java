package com.tradingsim.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 市场数据响应DTO
 * 
 * @author TradingSim Team
 */
public class MarketDataResponse {
    
    private String stockCode;
    private Instant timestamp;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;
    private List<Integer> keyPoints;
    
    public MarketDataResponse() {}
    
    public MarketDataResponse(String stockCode, Instant timestamp, BigDecimal open, BigDecimal high, 
                             BigDecimal low, BigDecimal close, Long volume) {
        this.stockCode = stockCode;
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }
    
    public String getStockCode() {
        return stockCode;
    }
    
    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public BigDecimal getOpen() {
        return open;
    }
    
    public void setOpen(BigDecimal open) {
        this.open = open;
    }
    
    public BigDecimal getHigh() {
        return high;
    }
    
    public void setHigh(BigDecimal high) {
        this.high = high;
    }
    
    public BigDecimal getLow() {
        return low;
    }
    
    public void setLow(BigDecimal low) {
        this.low = low;
    }
    
    public BigDecimal getClose() {
        return close;
    }
    
    public void setClose(BigDecimal close) {
        this.close = close;
    }
    
    public Long getVolume() {
        return volume;
    }
    
    public void setVolume(Long volume) {
        this.volume = volume;
    }
    
    public List<Integer> getKeyPoints() {
        return keyPoints;
    }
    
    public void setKeyPoints(List<Integer> keyPoints) {
        this.keyPoints = keyPoints;
    }
    
    /**
     * 计算价格变化百分比
     */
    public BigDecimal getPriceChangePercentage() {
        if (open == null || close == null || open.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return close.subtract(open).divide(open, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * 判断是否为上涨K线
     */
    public boolean isBullish() {
        return close != null && open != null && close.compareTo(open) > 0;
    }
    
    /**
     * 判断是否为下跌K线
     */
    public boolean isBearish() {
        return close != null && open != null && close.compareTo(open) < 0;
    }
    
    @Override
    public String toString() {
        return "MarketDataResponse{" +
                "stockCode='" + stockCode + '\'' +
                ", timestamp=" + timestamp +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                ", keyPoints=" + keyPoints +
                '}';
    }
}