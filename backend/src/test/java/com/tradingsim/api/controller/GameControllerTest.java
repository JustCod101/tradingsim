package com.tradingsim.api.controller;

import com.tradingsim.application.dto.GameSessionResponse;
import com.tradingsim.application.dto.GameDecisionRequest;
import com.tradingsim.application.dto.GameDecisionResponse;
import com.tradingsim.application.service.GameApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GameController单元测试
 * 
 * @author TradingSim Team
 */
@ExtendWith(MockitoExtension.class)
class GameControllerTest {
    
    @Mock
    private GameApplicationService gameApplicationService;
    
    @InjectMocks
    private GameController gameController;
    
    private GameSessionResponse mockSessionResponse;
    private GameDecisionResponse mockDecisionResponse;
    
    @BeforeEach
    void setUp() {
        mockSessionResponse = new GameSessionResponse();
        mockSessionResponse.setSessionId("test-session-1");
        mockSessionResponse.setStockCode("AAPL");
        mockSessionResponse.setStatus("RUNNING");
        mockSessionResponse.setCurrentBalance(BigDecimal.valueOf(10000));
        mockSessionResponse.setCreatedAt(Instant.now());
        
        mockDecisionResponse = new GameDecisionResponse();
        mockDecisionResponse.setDecisionId("decision-1");
        mockDecisionResponse.setSessionId("test-session-1");
        mockDecisionResponse.setDecisionType("BUY");
        mockDecisionResponse.setPrice(BigDecimal.valueOf(150.00));
        mockDecisionResponse.setQuantity(100);
    }
    
    @Test
    void testCreateSession() {
        // Given
        when(gameApplicationService.createSession(anyString(), anyString()))
            .thenReturn(mockSessionResponse);
        
        // When
        ResponseEntity<GameSessionResponse> response = gameController.createSession("AAPL", "EASY");
        
        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("test-session-1", response.getBody().getSessionId());
        verify(gameApplicationService).createSession("AAPL", "EASY");
    }
    
    @Test
    void testGetSession() {
        // Given
        when(gameApplicationService.getSessionById("test-session-1"))
            .thenReturn(mockSessionResponse);
        
        // When
        ResponseEntity<GameSessionResponse> response = gameController.getSession("test-session-1");
        
        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("test-session-1", response.getBody().getSessionId());
        verify(gameApplicationService).getSessionById("test-session-1");
    }
    
    @Test
    void testGetSessionNotFound() {
        // Given
        when(gameApplicationService.getSessionById("non-existent"))
            .thenReturn(null);
        
        // When
        ResponseEntity<GameSessionResponse> response = gameController.getSession("non-existent");
        
        // Then
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(gameApplicationService).getSessionById("non-existent");
    }
    
    @Test
    void testSubmitDecision() {
        // Given
        GameDecisionRequest request = new GameDecisionRequest();
        request.setDecisionType("BUY");
        request.setPrice(BigDecimal.valueOf(150.00));
        request.setQuantity(100);
        
        when(gameApplicationService.submitDecision(eq("test-session-1"), any(GameDecisionRequest.class)))
            .thenReturn(mockDecisionResponse);
        
        // When
        ResponseEntity<GameDecisionResponse> response = gameController.submitDecision("test-session-1", request);
        
        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("decision-1", response.getBody().getDecisionId());
        verify(gameApplicationService).submitDecision("test-session-1", request);
    }
    
    @Test
    void testGetSessionDecisions() {
        // Given
        List<GameDecisionResponse> decisions = Arrays.asList(mockDecisionResponse);
        when(gameApplicationService.getSessionDecisions("test-session-1"))
            .thenReturn(decisions);
        
        // When
        ResponseEntity<List<GameDecisionResponse>> response = gameController.getSessionDecisions("test-session-1");
        
        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(gameApplicationService).getSessionDecisions("test-session-1");
    }
    
    @Test
    void testFinishSession() {
        // Given
        mockSessionResponse.setStatus("COMPLETED");
        when(gameApplicationService.finishSession("test-session-1"))
            .thenReturn(mockSessionResponse);
        
        // When
        ResponseEntity<GameSessionResponse> response = gameController.finishSession("test-session-1");
        
        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("COMPLETED", response.getBody().getStatus());
        verify(gameApplicationService).finishSession("test-session-1");
    }
    
    @Test
    void testGetLeaderboard() {
        // Given
        List<GameSessionResponse> leaderboard = Arrays.asList(mockSessionResponse);
        when(gameApplicationService.getLeaderboard(10))
            .thenReturn(leaderboard);
        
        // When
        ResponseEntity<List<GameSessionResponse>> response = gameController.getLeaderboard(10);
        
        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(gameApplicationService).getLeaderboard(10);
    }
    
    @Test
    void testGetUserGameHistory() {
        // Given
        List<GameSessionResponse> history = Arrays.asList(mockSessionResponse);
        when(gameApplicationService.getUserGameHistory("user123", 20))
            .thenReturn(history);
        
        // When
        ResponseEntity<List<GameSessionResponse>> response = gameController.getUserGameHistory("user123", 20);
        
        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(gameApplicationService).getUserGameHistory("user123", 20);
    }
}