package com.tradingsim.domain.exception;

/**
 * 无效决策异常
 * 
 * @author TradingSim Team
 */
public class InvalidDecisionException extends DomainException {
    
    public InvalidDecisionException(String sessionId, String reason) {
        super("INVALID_DECISION", 
              String.format("Invalid decision for session %s: %s", sessionId, reason));
    }
    
    public InvalidDecisionException(String sessionId, int frameIndex, String reason) {
        super("INVALID_DECISION", 
              String.format("Invalid decision for session %s at frame %d: %s", sessionId, frameIndex, reason));
    }
}