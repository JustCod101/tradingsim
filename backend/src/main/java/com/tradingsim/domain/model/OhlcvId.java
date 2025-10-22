package com.tradingsim.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import java.util.Objects;

/**
 * OHLCV数据的复合主键
 * 
 * @author TradingSim Team
 */
@Embeddable
public class OhlcvId {
    
    @Column(name = "code", nullable = false, length = 20)
    private String code;
    
    @Column(name = "ts", nullable = false)
    private Instant timestamp;
    
    public OhlcvId() {}
    
    public OhlcvId(String code, Instant timestamp) {
        this.code = code;
        this.timestamp = timestamp;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
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