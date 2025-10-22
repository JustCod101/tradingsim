package com.tradingsim.api.controller;

import com.tradingsim.application.dto.AuthResponse;
import com.tradingsim.application.dto.LoginRequest;
import com.tradingsim.application.dto.RegisterRequest;
import com.tradingsim.application.service.UserService;
import com.tradingsim.infrastructure.security.JwtTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、注册、登出等认证相关的REST API
 * 
 * @author TradingSim Team
 */

@Tag(name = "认证管理", description = "用户认证相关API")
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "通过用户名/邮箱和密码进行登录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "用户名或密码错误")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            logger.info("Login request for: {}", request.getUsernameOrEmail());
            
            AuthResponse response = userService.login(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Login failed for: {}", request.getUsernameOrEmail(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("data", null);
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            logger.info("Registration request for: {}", request.getUsername());
            
            AuthResponse response = userService.register(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "注册成功");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Registration failed for: {}", request.getUsername(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("data", null);
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                throw new RuntimeException("刷新Token不能为空");
            }
            
            logger.info("Token refresh request");
            
            AuthResponse response = userService.refreshToken(refreshToken);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Token刷新成功");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Token refresh failed", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("data", null);
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("无效的认证头");
            }
            
            String token = authHeader.substring(7);
            
            // 验证Token
            if (!jwtTokenService.validateAccessToken(token)) {
                throw new RuntimeException("无效的Token");
            }
            
            String userId = jwtTokenService.getUserIdFromToken(token);
            if (userId == null) {
                throw new RuntimeException("无效的Token");
            }
            
            logger.info("Logout request for user: {}", userId);
            
            userService.logout(userId, token);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "登出成功");
            result.put("data", null);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Logout failed", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("data", null);
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("无效的认证头");
            }
            
            String token = authHeader.substring(7);
            
            // 验证Token
            if (!jwtTokenService.validateAccessToken(token)) {
                throw new RuntimeException("无效的Token");
            }
            
            String userId = jwtTokenService.getUserIdFromToken(token);
            if (userId == null) {
                throw new RuntimeException("无效的Token");
            }
            
            AuthResponse.UserInfo userInfo = userService.getUserById(userId);
            if (userInfo == null) {
                throw new RuntimeException("用户不存在");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "获取用户信息成功");
            result.put("data", userInfo);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Get current user failed", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("data", null);
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 验证Token
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (token == null || token.trim().isEmpty()) {
                throw new RuntimeException("Token不能为空");
            }
            
            boolean isValid = jwtTokenService.validateAccessToken(token);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Token验证完成");
            result.put("data", Map.of("valid", isValid));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Token validation failed", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("data", null);
            
            return ResponseEntity.badRequest().body(error);
        }
    }
}