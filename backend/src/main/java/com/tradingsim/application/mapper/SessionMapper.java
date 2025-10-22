package com.tradingsim.application.mapper;

import com.tradingsim.application.dto.SessionResponse;
import com.tradingsim.domain.model.GameSession;
import org.springframework.stereotype.Component;

/**
 * 会话映射器
 * 
 * @author TradingSim Team
 */
@Component
public class SessionMapper {
    
    /**
     * 将领域模型转换为响应DTO
     */
    public SessionResponse toResponse(GameSession session) {
        if (session == null) {
            return null;
        }
        
        SessionResponse response = new SessionResponse();
        response.setSessionId(session.getId());
        response.setStockCode(session.getStockCode());
        response.setTimeframe(session.getTimeframe());
        response.setStartTime(session.getStartTime());
        response.setEndTime(session.getEndTime());
        response.setStatus(session.getStatus());
        response.setCurrentFrameIndex(session.getCurrentFrameIndex());
        response.setTotalFrames(session.getTotalFrames());
        response.setInitialBalance(session.getInitialBalance());
        response.setCurrentBalance(session.getCurrentBalance());
        response.setTotalPnl(session.getTotalPnl());
        response.setMaxDrawdown(session.getMaxDrawdown());
        response.setWinRate(session.getWinRate());
        response.setTotalTrades(session.getTotalTrades());
        response.setProfitableTrades(session.getWinningTrades());
        response.setLosingTrades(session.getLosingTrades());
        response.setScore(session.getScore().intValue());
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        
        return response;
    }
}