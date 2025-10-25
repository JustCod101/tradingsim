package com.tradingsim.api.controller;

import com.tradingsim.application.dto.GameSessionResponse;
import com.tradingsim.application.dto.GameDecisionRequest;
import com.tradingsim.application.dto.GameDecisionResponse;
import com.tradingsim.application.service.GameApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 游戏REST API控制器
 * 
 * @author TradingSim Team
 */
@Tag(name = "游戏管理", description = "交易模拟游戏相关API")
@RestController
@RequestMapping("/game")
public class GameController {
    
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    
    @Autowired
    private GameApplicationService gameApplicationService;
    
    /**
     * 创建新的游戏会话
     */
    @Operation(summary = "创建游戏会话", description = "创建新的交易模拟游戏会话")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "游戏会话创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    @PostMapping("/sessions")
    public ResponseEntity<GameSessionResponse> createSession(
            @Parameter(description = "股票代码", example = "AAPL") @RequestParam @NotBlank String stockCode,
            @Parameter(description = "游戏难度", example = "EASY") @RequestParam @NotBlank String difficulty) {
        logger.info("创建游戏会话请求: stockCode={}, difficulty={}", stockCode, difficulty);
        
        try {
            GameSessionResponse response = gameApplicationService.createSession(stockCode, difficulty);
            logger.info("游戏会话创建成功: sessionId={}", response.getSessionId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("创建游戏会话失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取游戏会话详情
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<GameSessionResponse> getSession(
            @PathVariable @NotBlank String sessionId) {
        logger.info("获取游戏会话: sessionId={}", sessionId);
        
        try {
            GameSessionResponse response = gameApplicationService.getSessionById(sessionId);
            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("获取游戏会话失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 提交决策
     */
    @PostMapping("/sessions/{sessionId}/decisions")
    public ResponseEntity<GameDecisionResponse> submitDecision(
            @PathVariable @NotBlank String sessionId,
            @Valid @RequestBody GameDecisionRequest request) {
        logger.info("提交决策: sessionId={}, decision={}", sessionId, request);
        
        try {
            GameDecisionResponse response = gameApplicationService.submitDecision(sessionId, request);
            logger.info(response.toString());
            logger.info("决策提交成功: sessionId={}", sessionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("提交决策失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取会话决策历史
     */
    @GetMapping("/sessions/{sessionId}/decisions")
    public ResponseEntity<List<GameDecisionResponse>> getSessionDecisions(
            @PathVariable @NotBlank String sessionId) {
        logger.info("获取会话决策历史: sessionId={}", sessionId);
        
        try {
            List<GameDecisionResponse> decisions = gameApplicationService.getSessionDecisions(sessionId);
            return ResponseEntity.ok(decisions);
        } catch (Exception e) {
            logger.error("获取会话决策历史失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 启动游戏会话
     */
    @PostMapping("/sessions/{sessionId}/start")
    public ResponseEntity<GameSessionResponse> startSession(
            @PathVariable @NotBlank String sessionId) {
        logger.info("启动游戏会话: sessionId={}", sessionId);
        
        try {
            GameSessionResponse response = gameApplicationService.startSession(sessionId);
            logger.info("游戏会话启动成功: sessionId={}", sessionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("启动游戏会话失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 结束游戏会话
     */
    @PostMapping("/sessions/{sessionId}/finish")
    public ResponseEntity<GameSessionResponse> finishSession(
            @PathVariable @NotBlank String sessionId) {
        logger.info("结束游戏会话: sessionId={}", sessionId);
        
        try {
            GameSessionResponse response = gameApplicationService.finishSession(sessionId);
            logger.info("游戏会话结束成功: sessionId={}", sessionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("结束游戏会话失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取排行榜
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<GameSessionResponse>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        logger.info("获取排行榜: limit={}", limit);
        
        try {
            List<GameSessionResponse> leaderboard = gameApplicationService.getLeaderboard(limit);
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            logger.error("获取排行榜失败: limit={}, error={}", limit, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取用户游戏历史
     */
    @GetMapping("/users/{userId}/history")
    public ResponseEntity<List<GameSessionResponse>> getUserGameHistory(
            @PathVariable @NotBlank String userId,
            @RequestParam(defaultValue = "20") int limit) {
        logger.info("获取用户游戏历史: userId={}, limit={}", userId, limit);
        
        try {
            List<GameSessionResponse> history = gameApplicationService.getUserGameHistory(userId, limit);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("获取用户游戏历史失败: userId={}, limit={}, error={}", 
                userId, limit, e.getMessage(), e);
            throw e;
        }
    }
}