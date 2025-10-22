package com.tradingsim.domain.exception;

import com.tradingsim.domain.model.SessionStatus;

/**
 * 无效会话状态异常
 * 
 * @author TradingSim Team
 */
public class InvalidSessionStateException extends DomainException {
    
    public InvalidSessionStateException(String sessionId, SessionStatus currentStatus, String operation) {
        super("INVALID_SESSION_STATE", 
              String.format("Cannot perform operation '%s' on session %s with status %s", 
                          operation, sessionId, currentStatus));
    }
    
    public InvalidSessionStateException(String sessionId, SessionStatus currentStatus, 
                                      SessionStatus expectedStatus, String operation) {
        super("INVALID_SESSION_STATE", 
              String.format("Cannot perform operation '%s' on session %s. Current status: %s, Expected: %s", 
                          operation, sessionId, currentStatus, expectedStatus));
    }
}