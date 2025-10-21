package com.tradingsim.interfaces.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 500 Internal Server Error 异常
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends RuntimeException {
    
    public InternalServerErrorException(String message) {
        super(message);
    }
    
    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}