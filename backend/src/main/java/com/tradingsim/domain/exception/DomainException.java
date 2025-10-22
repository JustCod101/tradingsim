package com.tradingsim.domain.exception;

/**
 * 领域异常基类
 * 
 * @author TradingSim Team
 */
public abstract class DomainException extends RuntimeException {
    
    private final String errorCode;
    
    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    protected DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "errorCode='" + errorCode + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}