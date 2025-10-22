package com.tradingsim.application.service.impl;

import com.tradingsim.application.dto.CreateSessionRequest;
import com.tradingsim.application.dto.SessionResponse;
import com.tradingsim.application.dto.SubmitDecisionRequest;
import com.tradingsim.application.mapper.SessionMapper;
import com.tradingsim.application.service.GameSessionApplicationService;
import com.tradingsim.domain.model.GameSession;
import com.tradingsim.domain.model.SessionStatus;
import com.tradingsim.domain.service.GameSessionDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 游戏会话应用服务实现
 * 
 * @author TradingSim Team
 */
@Service
@Transactional
public class GameSessionApplicationServiceImpl implements GameSessionApplicationService {
    
    private final GameSessionDomainService domainService;
    private final SessionMapper sessionMapper;
    
    @Autowired
    public GameSessionApplicationServiceImpl(GameSessionDomainService domainService, 
                                           SessionMapper sessionMapper) {
        this.domainService = domainService;
        this.sessionMapper = sessionMapper;
    }
    
    @Override
    public SessionResponse createSession(CreateSessionRequest request) {
        GameSession session = domainService.createSession(
            request.getStockCode(), 
            request.getTimeframe(), 
            request.getInitialBalance()
        );
        return sessionMapper.toResponse(session);
    }
    
    @Override
    public SessionResponse startSession(String sessionId) {
        GameSession session = domainService.startSession(sessionId);
        return sessionMapper.toResponse(session);
    }
    
    @Override
    public SessionResponse pauseSession(String sessionId) {
        GameSession session = domainService.pauseSession(sessionId);
        return sessionMapper.toResponse(session);
    }
    
    @Override
    public SessionResponse resumeSession(String sessionId) {
        GameSession session = domainService.resumeSession(sessionId);
        return sessionMapper.toResponse(session);
    }
    
    @Override
    public SessionResponse completeSession(String sessionId) {
        GameSession session = domainService.completeSession(sessionId);
        return sessionMapper.toResponse(session);
    }
    
    @Override
    public SessionResponse cancelSession(String sessionId) {
        GameSession session = domainService.cancelSession(sessionId);
        return sessionMapper.toResponse(session);
    }
    
    @Override
    public SessionResponse submitDecision(SubmitDecisionRequest request) {
        GameSession session = domainService.submitDecision(
            request.getSessionId(),
            request.getFrameIndex(),
            request.getDecisionType(),
            request.getPrice(),
            request.getQuantity(),
            request.getResponseTimeMs()
        );
        return sessionMapper.toResponse(session);
    }
    
    @Override
    public SessionResponse nextFrame(String sessionId) {
        GameSession session = domainService.nextFrame(sessionId);
        return sessionMapper.toResponse(session);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<SessionResponse> getSessionById(String sessionId) {
        return domainService.getSessionById(sessionId)
                .map(sessionMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsByStatus(SessionStatus status) {
        return domainService.getSessionsByStatus(status)
                .stream()
                .map(sessionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsByStockCode(String stockCode) {
        return domainService.getSessionsByStockCode(stockCode)
                .stream()
                .map(sessionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getActiveSessions() {
        return domainService.getActiveSessions()
                .stream()
                .map(sessionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getLeaderboard(int limit) {
        return domainService.getLeaderboard(limit)
                .stream()
                .map(sessionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteSession(String sessionId) {
        domainService.deleteSession(sessionId);
    }
    
    @Override
    public int cleanupExpiredSessions() {
        return domainService.cleanupExpiredSessions();
    }
}