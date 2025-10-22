package com.tradingsim.domain.exception;

/**
 * 数据不足异常
 * 
 * @author TradingSim Team
 */
public class InsufficientDataException extends DomainException {
    
    public InsufficientDataException(String stockCode, String timeframe, int required, int available) {
        super("INSUFFICIENT_DATA", 
              String.format("Insufficient data for %s (%s): required %d, available %d", 
                          stockCode, timeframe, required, available));
    }
    
    public InsufficientDataException(String stockCode, String message) {
        super("INSUFFICIENT_DATA", 
              String.format("Insufficient data for %s: %s", stockCode, message));
    }
}