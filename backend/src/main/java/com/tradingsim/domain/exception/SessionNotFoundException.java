package com.tradingsim.domain.exception;

/**
 * 会话未找到异常
 * 
 * @author TradingSim Team
 */
public class SessionNotFoundException extends DomainException {
    
    public SessionNotFoundException(String sessionId) {
        super("SESSION_NOT_FOUND", "Game session not found: " + sessionId);
    }
}