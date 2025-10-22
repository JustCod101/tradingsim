package com.tradingsim.application.service;

import com.tradingsim.application.dto.*;
import com.tradingsim.application.dto.user.UserRanking;
import com.tradingsim.application.dto.user.UserProfileResponse;
import com.tradingsim.application.dto.user.UserPreferencesRequest;
import com.tradingsim.application.dto.user.UserPreferencesResponse;
import com.tradingsim.application.dto.user.UserStatsResponse;
import com.tradingsim.domain.model.user.User;
import com.tradingsim.domain.model.user.UserPreferences;
import com.tradingsim.domain.model.user.UserStats;
import com.tradingsim.domain.model.user.UserStatus;
import com.tradingsim.domain.repository.UserRepository;
import com.tradingsim.infrastructure.security.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户服务类
 * 处理用户认证、注册等业务逻辑
 */
@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        logger.info("User login attempt: {}", request.getUsernameOrEmail());
        
        // 查找用户
        String sql = "SELECT id, username, email, password_hash, full_name, status, roles, email_verified, created_at " +
                    "FROM users WHERE (username = ? OR email = ?) AND status = 'ACTIVE'";
        
        List<Map<String, Object>> users = jdbcTemplate.queryForList(sql, 
                request.getUsernameOrEmail(), request.getUsernameOrEmail());
        
        if (users.isEmpty()) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        Map<String, Object> user = users.get(0);
        String passwordHash = (String) user.get("password_hash");
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), passwordHash)) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 生成Token
        String userId = (String) user.get("id");
        String username = (String) user.get("username");
        String roles = (String) user.get("roles");
        
        String accessToken = jwtTokenService.generateAccessToken(userId, username, roles);
        String refreshToken = jwtTokenService.generateRefreshToken(userId);
        
        // 保存会话信息
        saveUserSession(userId, accessToken, refreshToken, request.isRememberMe());
        
        // 构建响应
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                userId,
                username,
                (String) user.get("email"),
                (String) user.get("full_name"),
                (String) user.get("status"),
                roles,
                (Boolean) user.get("email_verified"),
                ((java.sql.Timestamp) user.get("created_at")).toInstant()
        );
        
        AuthResponse response = new AuthResponse(
                accessToken,
                refreshToken,
                jwtTokenService.getAccessTokenExpirationInSeconds(),
                userInfo
        );
        
        logger.info("User login successful: {}", username);
        return response;
    }
    
    /**
     * 用户注册
     */
    public AuthResponse register(RegisterRequest request) {
        logger.info("User registration attempt: {}", request.getUsername());
        
        // 验证密码匹配
        if (!request.isPasswordMatching()) {
            throw new RuntimeException("密码和确认密码不匹配");
        }
        
        // 检查用户名是否已存在
        String checkUsernameSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer usernameCount = jdbcTemplate.queryForObject(checkUsernameSql, Integer.class, request.getUsername());
        if (usernameCount != null && usernameCount > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        String checkEmailSql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer emailCount = jdbcTemplate.queryForObject(checkEmailSql, Integer.class, request.getEmail());
        if (emailCount != null && emailCount > 0) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 创建用户
        String userId = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String passwordHash = passwordEncoder.encode(request.getPassword());
        
        String insertUserSql = "INSERT INTO users (id, username, email, password_hash, full_name, status, roles, email_verified, created_at, updated_at) " +
                              "VALUES (?, ?, ?, ?, ?, 'ACTIVE', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        
        jdbcTemplate.update(insertUserSql, 
                userId, 
                request.getUsername(), 
                request.getEmail(), 
                passwordHash, 
                request.getFullName());
        
        // 创建用户统计记录
        String insertStatsSql = "INSERT INTO user_stats (user_id, total_sessions, completed_sessions, total_score, best_score, total_pnl, win_rate, avg_response_time_ms, created_at, updated_at) " +
                               "VALUES (?, 0, 0, 0.00, 0.00, 0.00, 0.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        
        jdbcTemplate.update(insertStatsSql, userId);
        
        // 生成Token
        String accessToken = jwtTokenService.generateAccessToken(userId, request.getUsername(), "USER");
        String refreshToken = jwtTokenService.generateRefreshToken(userId);
        
        // 保存会话信息
        saveUserSession(userId, accessToken, refreshToken, false);
        
        // 构建响应
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                userId,
                request.getUsername(),
                request.getEmail(),
                request.getFullName(),
                "ACTIVE",
                "USER",
                true,
                Instant.now()
        );
        
        AuthResponse response = new AuthResponse(
                accessToken,
                refreshToken,
                jwtTokenService.getAccessTokenExpirationInSeconds(),
                userInfo
        );
        
        logger.info("User registration successful: {}", request.getUsername());
        return response;
    }
    
    /**
     * 刷新Token
     */
    public AuthResponse refreshToken(String refreshToken) {
        logger.info("Token refresh attempt");
        
        // 验证刷新Token
        if (!jwtTokenService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("无效的刷新Token");
        }
        
        String userId = jwtTokenService.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new RuntimeException("无效的刷新Token");
        }
        
        // 检查会话是否存在
        String checkSessionSql = "SELECT COUNT(*) FROM user_sessions WHERE user_id = ? AND refresh_token_hash = ? AND expires_at > CURRENT_TIMESTAMP";
        String refreshTokenHash = passwordEncoder.encode(refreshToken);
        Integer sessionCount = jdbcTemplate.queryForObject(checkSessionSql, Integer.class, userId, refreshTokenHash);
        
        if (sessionCount == null || sessionCount == 0) {
            throw new RuntimeException("会话已过期");
        }
        
        // 获取用户信息
        String getUserSql = "SELECT username, email, full_name, status, roles, email_verified, created_at " +
                           "FROM users WHERE id = ? AND status = 'ACTIVE'";
        
        List<Map<String, Object>> users = jdbcTemplate.queryForList(getUserSql, userId);
        if (users.isEmpty()) {
            throw new RuntimeException("用户不存在或已被禁用");
        }
        
        Map<String, Object> user = users.get(0);
        String username = (String) user.get("username");
        String roles = (String) user.get("roles");
        
        // 生成新的Token
        String newAccessToken = jwtTokenService.generateAccessToken(userId, username, roles);
        String newRefreshToken = jwtTokenService.generateRefreshToken(userId);
        
        // 更新会话信息
        updateUserSession(userId, refreshToken, newAccessToken, newRefreshToken);
        
        // 构建响应
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                userId,
                username,
                (String) user.get("email"),
                (String) user.get("full_name"),
                (String) user.get("status"),
                roles,
                (Boolean) user.get("email_verified"),
                ((java.sql.Timestamp) user.get("created_at")).toInstant()
        );
        
        AuthResponse response = new AuthResponse(
                newAccessToken,
                newRefreshToken,
                jwtTokenService.getAccessTokenExpirationInSeconds(),
                userInfo
        );
        
        logger.info("Token refresh successful for user: {}", username);
        return response;
    }
    
    /**
     * 用户登出
     */
    public void logout(String userId, String accessToken) {
        logger.info("User logout: {}", userId);
        
        // 删除用户会话
        String deleteSessionSql = "DELETE FROM user_sessions WHERE user_id = ? AND access_token_hash = ?";
        String tokenHash = passwordEncoder.encode(accessToken);
        jdbcTemplate.update(deleteSessionSql, userId, tokenHash);
        
        logger.info("User logout successful: {}", userId);
    }
    
    /**
     * 保存用户会话
     */
    private void saveUserSession(String userId, String accessToken, String refreshToken, boolean rememberMe) {
        String tokenHash = passwordEncoder.encode(accessToken);
        String refreshTokenHash = passwordEncoder.encode(refreshToken);
        
        // 计算过期时间
        long expirationSeconds = rememberMe ? 
                jwtTokenService.getRefreshTokenExpirationInSeconds() : 
                jwtTokenService.getAccessTokenExpirationInSeconds();
        
        String sessionId = UUID.randomUUID().toString();
        String sql = "INSERT INTO user_sessions (id, user_id, access_token_hash, refresh_token_hash, expires_at, created_at) " +
                    "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP + INTERVAL '" + expirationSeconds + " seconds', CURRENT_TIMESTAMP)";
        
        jdbcTemplate.update(sql, sessionId, userId, tokenHash, refreshTokenHash);
    }
    
    /**
     * 更新用户会话
     */
    private void updateUserSession(String userId, String oldRefreshToken, String newAccessToken, String newRefreshToken) {
        String oldRefreshTokenHash = passwordEncoder.encode(oldRefreshToken);
        String newTokenHash = passwordEncoder.encode(newAccessToken);
        String newRefreshTokenHash = passwordEncoder.encode(newRefreshToken);
        
        String sql = "UPDATE user_sessions SET access_token_hash = ?, refresh_token_hash = ?, " +
                    "expires_at = CURRENT_TIMESTAMP + INTERVAL '" + jwtTokenService.getRefreshTokenExpirationInSeconds() + " seconds' " +
                    "WHERE user_id = ? AND refresh_token_hash = ?";
        
        jdbcTemplate.update(sql, newTokenHash, newRefreshTokenHash, userId, oldRefreshTokenHash);
    }
    
    /**
     * 根据用户ID获取用户信息
     */
    public AuthResponse.UserInfo getUserById(String userId) {
        String sql = "SELECT username, email, full_name, status, roles, email_verified, created_at " +
                    "FROM users WHERE id = ? AND status = 'ACTIVE'";
        
        List<Map<String, Object>> users = jdbcTemplate.queryForList(sql, userId);
        if (users.isEmpty()) {
            return null;
        }
        
        Map<String, Object> user = users.get(0);
        return new AuthResponse.UserInfo(
                userId,
                (String) user.get("username"),
                (String) user.get("email"),
                (String) user.get("full_name"),
                (String) user.get("status"),
                (String) user.get("roles"),
                (Boolean) user.get("email_verified"),
                ((java.sql.Timestamp) user.get("created_at")).toInstant()
        );
    }
    
    // ==================== 用户管理方法 ====================
    
    /**
     * 获取用户资料
     */
    public UserProfileResponse getUserProfile(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        return new UserProfileResponse(userOpt.get());
    }
    
    /**
     * 更新用户资料
     */
    public UserProfileResponse updateUserProfile(String userId, UserProfileResponse request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setBirthDate(request.getBirthDate());
        user.setTimezone(request.getTimezone());
        user.setLanguage(request.getLanguage());
        
        User savedUser = userRepository.save(user);
        return new UserProfileResponse(savedUser);
    }
    
    /**
     * 获取用户偏好设置
     */
    public UserPreferencesResponse getUserPreferences(String userId) {
        Optional<UserPreferences> preferencesOpt = userRepository.findPreferencesByUserId(userId);
        if (preferencesOpt.isEmpty()) {
            // 如果没有偏好设置，创建默认设置
            UserPreferences defaultPreferences = new UserPreferences(userId);
            userRepository.savePreferences(defaultPreferences);
            return new UserPreferencesResponse(defaultPreferences);
        }
        return new UserPreferencesResponse(preferencesOpt.get());
    }
    
    /**
     * 更新用户偏好设置
     */
    public UserPreferencesResponse updateUserPreferences(String userId, UserPreferencesRequest request) {
        Optional<UserPreferences> preferencesOpt = userRepository.findPreferencesByUserId(userId);
        
        UserPreferences preferences;
        if (preferencesOpt.isEmpty()) {
            preferences = new UserPreferences(userId);
        } else {
            preferences = preferencesOpt.get();
        }
        
        preferences.updatePreferences(
            request.getPreferredDifficulty(),
            request.getPreferredTimeframe(),
            request.getWatchlist(),
            request.isNotificationsEnabled(),
            request.isSoundEnabled(),
            request.getTheme(),
            request.isAutoSaveEnabled(),
            request.isShowTutorials(),
            request.getRiskTolerance(),
            request.getTradingStyle()
        );
        
        UserPreferences savedPreferences = userRepository.savePreferences(preferences);
        return new UserPreferencesResponse(savedPreferences);
    }
    
    /**
     * 获取用户统计信息
     */
    public UserStatsResponse getUserStats(String userId) {
        Optional<UserStats> statsOpt = userRepository.findStatsByUserId(userId);
        if (statsOpt.isEmpty()) {
            // 如果没有统计信息，创建默认统计
            UserStats defaultStats = new UserStats(userId);
            userRepository.saveStats(defaultStats);
            return new UserStatsResponse(defaultStats);
        }
        return new UserStatsResponse(statsOpt.get());
    }
    
    /**
     * 获取用户排名
     */
    public List<UserRanking> getUserRankings(int limit) {
        List<UserStats> topUsers = userRepository.findTopUsersByScore(limit);
        return topUsers.stream()
                .map(stats -> {
                    Optional<User> userOpt = userRepository.findById(stats.getUserId());
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        return new UserRanking(
                                user.getId(),
                                user.getUsername(),
                                user.getAvatarUrl(),
                                stats.getTotalScore(),
                                stats.getTotalPnl(),
                                stats.getWinRate(),
                                stats.getRankPosition()
                        );
                    }
                    return null;
                })
                .filter(ranking -> ranking != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 搜索用户
     */
    public List<UserProfileResponse> searchUsers(String query) {
        List<User> users = userRepository.findByUsernameContaining(query);
        return users.stream()
                .map(UserProfileResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取活跃用户列表
     */
    public List<UserProfileResponse> getActiveUsers() {
        List<User> users = userRepository.findActiveUsers();
        return users.stream()
                .map(UserProfileResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新用户状态
     */
    public void updateUserStatus(String userId, String status) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setStatus(UserStatus.fromCode(status));
        userRepository.save(user);
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(String userId) {
        // 删除用户相关数据
        userRepository.deletePreferencesByUserId(userId);
        userRepository.deleteStatsByUserId(userId);
        userRepository.deleteById(userId);
    }
    
    /**
     * 验证用户名是否可用
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    /**
     * 验证邮箱是否可用
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}