package com.tradingsim.interfaces.rest;

import com.tradingsim.app.service.GameSessionService;
import com.tradingsim.domain.model.GameDecision;
import com.tradingsim.domain.model.GameSession;
import com.tradingsim.interfaces.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 游戏REST控制器
 * 
 * 提供游戏会话管理的REST API端点
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/game")
@Tag(name = "Game", description = "游戏会话管理API")
@Validated
public class GameController {
    
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    
    private final GameSessionService gameSessionService;
    
    @Autowired
    public GameController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }
    
    @PostMapping("/sessions")
    @Operation(summary = "创建游戏会话", description = "为指定用户和数据段创建新的游戏会话")
    @ApiResponse(responseCode = "201", description = "会话创建成功")
    @ApiResponse(responseCode = "400", description = "请求参数无效")
    public ResponseEntity<SessionResponse> createSession(
            @Valid @RequestBody CreateSessionRequest request) {
        
        logger.info("Creating session for user {} with segment {}", 
                   request.getUserId(), request.getSegmentId());
        
        try {
            GameSession session = gameSessionService.createSession(
                request.getUserId(), request.getSegmentId(), request.getClientId());
            
            SessionResponse response = SessionResponse.fromDomain(session);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for session creation: {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating session", e);
            throw new InternalServerErrorException("Failed to create session");
        }
    }
    
    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "获取游戏会话", description = "根据会话ID获取游戏会话详情")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "会话不存在")
    public ResponseEntity<SessionResponse> getSession(
            @Parameter(description = "会话ID") @PathVariable @NotBlank String sessionId) {
        
        logger.debug("Getting session {}", sessionId);
        
        Optional<GameSession> session = gameSessionService.getSession(sessionId);
        if (session.isEmpty()) {
            throw new NotFoundException("Session not found: " + sessionId);
        }
        
        SessionResponse response = SessionResponse.fromDomain(session.get());
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/sessions/{sessionId}")
    @Operation(summary = "更新游戏会话", description = "更新游戏会话状态")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @ApiResponse(responseCode = "404", description = "会话不存在")
    @ApiResponse(responseCode = "400", description = "状态转换无效")
    public ResponseEntity<SessionResponse> updateSession(
            @Parameter(description = "会话ID") @PathVariable @NotBlank String sessionId,
            @Valid @RequestBody UpdateSessionRequest request) {
        
        logger.info("Updating session {} to status {}", sessionId, request.getAction());
        
        try {
            GameSession session = performSessionAction(sessionId, request.getAction());
            SessionResponse response = SessionResponse.fromDomain(session);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid session update: {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("Invalid session state transition: {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }
    
    @PostMapping("/sessions/{sessionId}/decisions")
    @Operation(summary = "提交交易决策", description = "为游戏会话提交交易决策")
    @ApiResponse(responseCode = "200", description = "决策提交成功")
    @ApiResponse(responseCode = "404", description = "会话不存在")
    @ApiResponse(responseCode = "400", description = "决策无效")
    public ResponseEntity<DecisionResponse> submitDecision(
            @Parameter(description = "会话ID") @PathVariable @NotBlank String sessionId,
            @Valid @RequestBody SubmitDecisionRequest request) {
        
        logger.debug("Submitting decision for session {}: {} at frame {}", 
                    sessionId, request.getDecisionType(), request.getFrameIndex());
        
        try {
            GameDecision decision = gameSessionService.submitDecision(
                sessionId, request.getFrameIndex(), request.getDecisionType(),
                request.getPrice(), request.getQuantity(), request.getClientId());
            
            DecisionResponse response = DecisionResponse.fromDomain(decision);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid decision submission: {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("Invalid session state for decision: {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }
    
    @GetMapping("/sessions/user/{userId}")
    @Operation(summary = "获取用户会话", description = "获取指定用户的所有游戏会话")
    @ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<List<SessionResponse>> getUserSessions(
            @Parameter(description = "用户ID") @PathVariable @NotBlank String userId,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        
        logger.debug("Getting sessions for user {} with limit {}", userId, limit);
        
        List<GameSession> sessions = gameSessionService.getUserSessions(userId, limit);
        List<SessionResponse> responses = sessions.stream()
            .map(SessionResponse::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/sessions/segment/{segmentId}")
    @Operation(summary = "获取数据段会话", description = "获取指定数据段的所有游戏会话")
    @ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<List<SessionResponse>> getSegmentSessions(
            @Parameter(description = "数据段ID") @PathVariable @NotBlank String segmentId,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        
        logger.debug("Getting sessions for segment {} with limit {}", segmentId, limit);
        
        List<GameSession> sessions = gameSessionService.getSegmentSessions(segmentId, limit);
        List<SessionResponse> responses = sessions.stream()
            .map(SessionResponse::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/leaderboard")
    @Operation(summary = "获取全局排行榜", description = "获取全局游戏排行榜")
    @ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<List<LeaderboardEntry>> getGlobalLeaderboard(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        logger.debug("Getting global leaderboard with limit {}", limit);
        
        List<GameSession> topSessions = gameSessionService.getGlobalLeaderboard(limit);
        List<LeaderboardEntry> entries = topSessions.stream()
            .map(LeaderboardEntry::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(entries);
    }
    
    @GetMapping("/leaderboard/user/{userId}")
    @Operation(summary = "获取用户排行榜", description = "获取指定用户的排行榜")
    @ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<List<LeaderboardEntry>> getUserLeaderboard(
            @Parameter(description = "用户ID") @PathVariable @NotBlank String userId,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        logger.debug("Getting user leaderboard for {} with limit {}", userId, limit);
        
        List<GameSession> topSessions = gameSessionService.getUserLeaderboard(userId, limit);
        List<LeaderboardEntry> entries = topSessions.stream()
            .map(LeaderboardEntry::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(entries);
    }
    
    @GetMapping("/leaderboard/segment/{segmentId}")
    @Operation(summary = "获取数据段排行榜", description = "获取指定数据段的排行榜")
    @ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<List<LeaderboardEntry>> getSegmentLeaderboard(
            @Parameter(description = "数据段ID") @PathVariable @NotBlank String segmentId,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        logger.debug("Getting segment leaderboard for {} with limit {}", segmentId, limit);
        
        List<GameSession> topSessions = gameSessionService.getSegmentLeaderboard(segmentId, limit);
        List<LeaderboardEntry> entries = topSessions.stream()
            .map(LeaderboardEntry::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(entries);
    }
    
    @PostMapping("/sessions/{sessionId}/replay")
    @Operation(summary = "重放游戏会话", description = "重放已完成的游戏会话")
    @ApiResponse(responseCode = "200", description = "重放开始成功")
    @ApiResponse(responseCode = "404", description = "会话不存在")
    @ApiResponse(responseCode = "400", description = "会话状态不允许重放")
    public ResponseEntity<ReplayResponse> replaySession(
            @Parameter(description = "会话ID") @PathVariable @NotBlank String sessionId,
            @Valid @RequestBody ReplayRequest request) {
        
        logger.info("Starting replay for session {} with speed {}", sessionId, request.getSpeed());
        
        try {
            // TODO: 实现重放功能
            ReplayResponse response = new ReplayResponse();
            response.setSessionId(sessionId);
            response.setReplayId("replay_" + System.currentTimeMillis());
            response.setSpeed(request.getSpeed());
            response.setStatus("STARTED");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid replay request: {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }
    
    /**
     * 执行会话操作
     */
    private GameSession performSessionAction(String sessionId, String action) {
        switch (action.toUpperCase()) {
            case "START":
                return gameSessionService.startSession(sessionId);
            case "PAUSE":
                return gameSessionService.pauseSession(sessionId);
            case "RESUME":
                return gameSessionService.resumeSession(sessionId);
            case "COMPLETE":
                return gameSessionService.completeSession(sessionId);
            case "CANCEL":
                return gameSessionService.cancelSession(sessionId);
            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }
    }
}