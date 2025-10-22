package com.tradingsim.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * OHLCV数据领域模型
 * 
 * @author TradingSim Team
 */
@Entity
@Table(name = "ohlcv_1m")
public class OhlcvData {
    
    @EmbeddedId
    private OhlcvId id;
    
    @Column(name = "open", nullable = false, precision = 12, scale = 4)
    private BigDecimal openPrice;
    
    @Column(name = "high", nullable = false, precision = 12, scale = 4)
    private BigDecimal highPrice;
    
    @Column(name = "low", nullable = false, precision = 12, scale = 4)
    private BigDecimal lowPrice;
    
    @Column(name = "close", nullable = false, precision = 12, scale = 4)
    private BigDecimal closePrice;
    
    @Column(name = "volume", nullable = false)
    private Long volume;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    public OhlcvData() {
        this.createdAt = Instant.now();
    }
    
    public OhlcvData(OhlcvId id, BigDecimal openPrice, BigDecimal highPrice, 
                     BigDecimal lowPrice, BigDecimal closePrice, Long volume) {
        this.id = id;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.createdAt = Instant.now();
    }
    
    /**
     * 计算价格变化百分比
     */
    public BigDecimal getPriceChangePercent() {
        if (openPrice == null || openPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return closePrice.subtract(openPrice)
                .divide(openPrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * 判断是否为上涨K线
     */
    public boolean isBullish() {
        return closePrice.compareTo(openPrice) > 0;
    }
    
    /**
     * 判断是否为下跌K线
     */
    public boolean isBearish() {
        return closePrice.compareTo(openPrice) < 0;
    }
    
    /**
     * 获取实体大小（收盘价与开盘价的差值绝对值）
     */
    public BigDecimal getBodySize() {
        return closePrice.subtract(openPrice).abs();
    }
    
    /**
     * 获取上影线长度
     */
    public BigDecimal getUpperShadow() {
        BigDecimal maxOpenClose = openPrice.max(closePrice);
        return highPrice.subtract(maxOpenClose);
    }
    
    /**
     * 获取下影线长度
     */
    public BigDecimal getLowerShadow() {
        BigDecimal minOpenClose = openPrice.min(closePrice);
        return minOpenClose.subtract(lowPrice);
    }
    
    // Getters and Setters
    public OhlcvId getId() {
        return id;
    }
    
    public void setId(OhlcvId id) {
        this.id = id;
    }
    
    public BigDecimal getOpenPrice() {
        return openPrice;
    }
    
    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }
    
    public BigDecimal getHighPrice() {
        return highPrice;
    }
    
    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }
    
    public BigDecimal getLowPrice() {
        return lowPrice;
    }
    
    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }
    
    public BigDecimal getClosePrice() {
        return closePrice;
    }
    
    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }
    
    public Long getVolume() {
        return volume;
    }
    
    public void setVolume(Long volume) {
        this.volume = volume;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OhlcvData ohlcvData = (OhlcvData) o;
        return Objects.equals(id, ohlcvData.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "OhlcvData{" +
                "id=" + id +
                ", openPrice=" + openPrice +
                ", highPrice=" + highPrice +
                ", lowPrice=" + lowPrice +
                ", closePrice=" + closePrice +
                ", volume=" + volume +
                ", createdAt=" + createdAt +
                '}';
    }
}