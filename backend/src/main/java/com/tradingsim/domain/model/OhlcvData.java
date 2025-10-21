package com.tradingsim.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * OHLCV 数据领域模型
 * 
 * 代表1分钟级别的市场数据，包含:
 * - 股票代码和时间戳
 * - OHLCV 价格和成交量数据
 * - 技术指标计算支持
 * 
 * DDD 值对象，不可变数据结构
 */
@Entity
@Table(name = "ohlcv_1m")
public class OhlcvData {
    
    @EmbeddedId
    private OhlcvId id;
    
    @Column(name = "open", nullable = false, precision = 10, scale = 4)
    private BigDecimal open;
    
    @Column(name = "high", nullable = false, precision = 10, scale = 4)
    private BigDecimal high;
    
    @Column(name = "low", nullable = false, precision = 10, scale = 4)
    private BigDecimal low;
    
    @Column(name = "close", nullable = false, precision = 10, scale = 4)
    private BigDecimal close;
    
    @Column(name = "volume", nullable = false)
    private Long volume;
    
    // 默认构造函数 (JPA 需要)
    protected OhlcvData() {}
    
    /**
     * 创建 OHLCV 数据
     * 
     * @param code 股票代码
     * @param timestamp 时间戳
     * @param open 开盘价
     * @param high 最高价
     * @param low 最低价
     * @param close 收盘价
     * @param volume 成交量
     */
    public OhlcvData(String code, Instant timestamp, BigDecimal open, BigDecimal high,
                    BigDecimal low, BigDecimal close, Long volume) {
        this.id = new OhlcvId(code, timestamp);
        this.open = Objects.requireNonNull(open, "open cannot be null");
        this.high = Objects.requireNonNull(high, "high cannot be null");
        this.low = Objects.requireNonNull(low, "low cannot be null");
        this.close = Objects.requireNonNull(close, "close cannot be null");
        this.volume = Objects.requireNonNull(volume, "volume cannot be null");
        
        // 业务规则验证
        validatePrices();
    }
    
    /**
     * 验证价格数据的合理性
     */
    private void validatePrices() {
        if (open.compareTo(BigDecimal.ZERO) <= 0 ||
            high.compareTo(BigDecimal.ZERO) <= 0 ||
            low.compareTo(BigDecimal.ZERO) <= 0 ||
            close.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("All prices must be positive");
        }
        
        if (high.compareTo(low) < 0) {
            throw new IllegalArgumentException("High price cannot be less than low price");
        }
        
        if (high.compareTo(open) < 0 || high.compareTo(close) < 0) {
            throw new IllegalArgumentException("High price must be >= open and close prices");
        }
        
        if (low.compareTo(open) > 0 || low.compareTo(close) > 0) {
            throw new IllegalArgumentException("Low price must be <= open and close prices");
        }
        
        if (volume < 0) {
            throw new IllegalArgumentException("Volume cannot be negative");
        }
    }
    
    /**
     * 计算价格变化
     */
    public BigDecimal getPriceChange() {
        return close.subtract(open);
    }
    
    /**
     * 计算价格变化百分比
     */
    public BigDecimal getPriceChangePercent() {
        if (open.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getPriceChange().divide(open, 6, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * 计算价格范围 (高低价差)
     */
    public BigDecimal getPriceRange() {
        return high.subtract(low);
    }
    
    /**
     * 计算价格范围百分比
     */
    public BigDecimal getPriceRangePercent() {
        if (open.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getPriceRange().divide(open, 6, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * 计算典型价格 (HLC/3)
     */
    public BigDecimal getTypicalPrice() {
        return high.add(low).add(close)
                .divide(BigDecimal.valueOf(3), 4, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * 计算加权收盘价 (HLCC/4)
     */
    public BigDecimal getWeightedClose() {
        return high.add(low).add(close).add(close)
                .divide(BigDecimal.valueOf(4), 4, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * 检查是否为上涨K线
     */
    public boolean isBullish() {
        return close.compareTo(open) > 0;
    }
    
    /**
     * 检查是否为下跌K线
     */
    public boolean isBearish() {
        return close.compareTo(open) < 0;
    }
    
    /**
     * 检查是否为十字星 (开盘价等于收盘价)
     */
    public boolean isDoji() {
        return close.compareTo(open) == 0;
    }
    
    /**
     * 计算实体大小 (开盘价和收盘价之差的绝对值)
     */
    public BigDecimal getBodySize() {
        return close.subtract(open).abs();
    }
    
    /**
     * 计算上影线长度
     */
    public BigDecimal getUpperShadow() {
        BigDecimal bodyTop = open.max(close);
        return high.subtract(bodyTop);
    }
    
    /**
     * 计算下影线长度
     */
    public BigDecimal getLowerShadow() {
        BigDecimal bodyBottom = open.min(close);
        return bodyBottom.subtract(low);
    }
    
    /**
     * 检查是否为高成交量 (相对于给定阈值)
     */
    public boolean isHighVolume(Long volumeThreshold) {
        return volume > volumeThreshold;
    }
    
    /**
     * 转换为 JSON 格式的字符串 (用于 WebSocket 传输)
     */
    public String toJsonString() {
        return String.format(
            "{\"timestamp\":\"%s\",\"open\":%.4f,\"high\":%.4f,\"low\":%.4f,\"close\":%.4f,\"volume\":%d}",
            id.getTimestamp().toString(),
            open.doubleValue(),
            high.doubleValue(),
            low.doubleValue(),
            close.doubleValue(),
            volume
        );
    }
    
    // Getters
    public OhlcvId getId() { return id; }
    public String getCode() { return id.getCode(); }
    public Instant getTimestamp() { return id.getTimestamp(); }
    public BigDecimal getOpen() { return open; }
    public BigDecimal getHigh() { return high; }
    public BigDecimal getLow() { return low; }
    public BigDecimal getClose() { return close; }
    public Long getVolume() { return volume; }
    
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
                "code='" + getCode() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                '}';
    }
}

/**
 * OHLCV 复合主键
 */
@Embeddable
class OhlcvId {
    
    @Column(name = "code", length = 16, nullable = false)
    private String code;
    
    @Column(name = "ts", nullable = false)
    private Instant timestamp;
    
    // 默认构造函数 (JPA 需要)
    protected OhlcvId() {}
    
    public OhlcvId(String code, Instant timestamp) {
        this.code = Objects.requireNonNull(code, "code cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp cannot be null");
        
        if (code.trim().isEmpty()) {
            throw new IllegalArgumentException("code cannot be empty");
        }
    }
    
    public String getCode() { return code; }
    public Instant getTimestamp() { return timestamp; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OhlcvId ohlcvId = (OhlcvId) o;
        return Objects.equals(code, ohlcvId.code) &&
               Objects.equals(timestamp, ohlcvId.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(code, timestamp);
    }
    
    @Override
    public String toString() {
        return "OhlcvId{" +
                "code='" + code + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}