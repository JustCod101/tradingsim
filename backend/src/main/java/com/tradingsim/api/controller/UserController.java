package com.tradingsim.api.controller;

import com.tradingsim.application.dto.GameSessionResponse;
import com.tradingsim.application.dto.user.*;
import com.tradingsim.application.service.GameApplicationService;
import com.tradingsim.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户管理控制器
 * 处理用户相关的REST API
 * 
 * @author TradingSim Team
 */
@Tag(name = "用户管理", description = "用户信息和偏好设置相关API")
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private GameApplicationService gameApplicationService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取用户游戏历史
     */
    @Operation(summary = "获取用户游戏历史", description = "获取指定用户的游戏历史记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取游戏历史"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @GetMapping("/{userId}/game-history")
    public ResponseEntity<List<GameSessionResponse>> getUserGameHistory(
            @Parameter(description = "用户ID") @PathVariable String userId,
            @Parameter(description = "返回记录数量限制", example = "10") @RequestParam(defaultValue = "10") int limit) {
        
        List<GameSessionResponse> history = gameApplicationService.getUserGameHistory(userId, limit);
        return ResponseEntity.ok(history);
    }
    
    /**
     * 获取用户统计信息
     */
    @Operation(summary = "获取用户统计信息", description = "获取指定用户的详细统计信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取用户统计信息"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @GetMapping("/{userId}/stats")
    public ResponseEntity<UserStatsResponse> getUserStats(@PathVariable String userId) {
        UserStatsResponse stats = userService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取用户排名
     */
    @Operation(summary = "获取用户排名", description = "获取指定用户在排行榜中的排名信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取用户排名"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @GetMapping("/{userId}/ranking")
    public ResponseEntity<UserRanking> getUserRanking(@PathVariable String userId) {
        // 获取用户统计信息来获取排名
        UserStatsResponse stats = userService.getUserStats(userId);
        
        UserRanking ranking = new UserRanking();
        ranking.setUserId(userId);
        ranking.setTotalScore(stats.getTotalScore());
        ranking.setTotalPnl(stats.getTotalPnl());
        ranking.setWinRate(stats.getWinRate());
        ranking.setRankPosition(stats.getRankPosition());
        
        return ResponseEntity.ok(ranking);
    }
    
    /**
     * 获取用户偏好设置
     */
    @Operation(summary = "获取用户偏好设置", description = "获取指定用户的偏好设置")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取用户偏好设置"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @GetMapping("/{userId}/preferences")
    public ResponseEntity<UserPreferencesResponse> getUserPreferences(@PathVariable String userId) {
        UserPreferencesResponse preferences = userService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }
    
    /**
     * 更新用户偏好设置
     */
    @Operation(summary = "更新用户偏好设置", description = "更新指定用户的偏好设置")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功更新用户偏好设置"),
        @ApiResponse(responseCode = "400", description = "请求参数无效"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @PutMapping("/{userId}/preferences")
    public ResponseEntity<UserPreferencesResponse> updateUserPreferences(
            @PathVariable String userId,
            @Valid @RequestBody UserPreferencesRequest request) {
        
        UserPreferencesResponse preferences = userService.updateUserPreferences(userId, request);
        return ResponseEntity.ok(preferences);
    }
    
    /**
     * 获取用户资料
     */
    @Operation(summary = "获取用户资料", description = "获取指定用户的详细资料信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取用户资料"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String userId) {
        UserProfileResponse profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }
    
    /**
     * 更新用户资料
     */
    @Operation(summary = "更新用户资料", description = "更新指定用户的资料信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功更新用户资料"),
        @ApiResponse(responseCode = "400", description = "请求参数无效"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @PathVariable String userId,
            @Valid @RequestBody UserProfileResponse request) {
        
        UserProfileResponse profile = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(profile);
    }
    
    /**
     * 获取排行榜
     */
    @Operation(summary = "获取排行榜", description = "获取用户排行榜")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取排行榜")
    })
    @GetMapping("/rankings")
    public ResponseEntity<List<UserRanking>> getRankings(
            @Parameter(description = "返回记录数量限制", example = "10") 
            @RequestParam(defaultValue = "10") int limit) {
        
        List<UserRanking> rankings = userService.getUserRankings(limit);
        return ResponseEntity.ok(rankings);
    }
    
    /**
     * 搜索用户
     */
    @Operation(summary = "搜索用户", description = "根据用户名搜索用户")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功搜索用户")
    })
    @GetMapping("/search")
    public ResponseEntity<List<UserProfileResponse>> searchUsers(
            @Parameter(description = "搜索关键词") 
            @RequestParam String query) {
        
        List<UserProfileResponse> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 获取活跃用户列表
     */
    @Operation(summary = "获取活跃用户", description = "获取当前活跃的用户列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取活跃用户列表")
    })
    @GetMapping("/active")
    public ResponseEntity<List<UserProfileResponse>> getActiveUsers() {
        List<UserProfileResponse> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * 验证用户名是否可用
     */
    @Operation(summary = "验证用户名", description = "验证用户名是否可用")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "验证结果")
    })
    @GetMapping("/validate/username")
    public ResponseEntity<Boolean> validateUsername(
            @Parameter(description = "用户名") 
            @RequestParam String username) {
        
        boolean available = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(available);
    }
    
    /**
     * 验证邮箱是否可用
     */
    @Operation(summary = "验证邮箱", description = "验证邮箱是否可用")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "验证结果")
    })
    @GetMapping("/validate/email")
    public ResponseEntity<Boolean> validateEmail(
            @Parameter(description = "邮箱地址") 
            @RequestParam String email) {
        
        boolean available = userService.isEmailAvailable(email);
        return ResponseEntity.ok(available);
    }
}