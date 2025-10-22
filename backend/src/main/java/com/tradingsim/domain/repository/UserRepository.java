package com.tradingsim.domain.repository;

import com.tradingsim.domain.model.user.User;
import com.tradingsim.domain.model.user.UserPreferences;
import com.tradingsim.domain.model.user.UserStats;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 * 
 * @author TradingSim Team
 */
public interface UserRepository {
    
    // 用户基本操作
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    User save(User user);
    void deleteById(String id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // 用户偏好设置操作
    Optional<UserPreferences> findPreferencesByUserId(String userId);
    UserPreferences savePreferences(UserPreferences preferences);
    void deletePreferencesByUserId(String userId);
    
    // 用户统计信息操作
    Optional<UserStats> findStatsByUserId(String userId);
    UserStats saveStats(UserStats stats);
    void deleteStatsByUserId(String userId);
    
    // 用户排名相关
    List<UserStats> findTopUsersByScore(int limit);
    List<UserStats> findTopUsersByPnl(int limit);
    Long getUserRankByScore(String userId);
    Long getUserRankByPnl(String userId);
    
    // 用户搜索和过滤
    List<User> findByUsernameContaining(String username);
    List<User> findByStatus(String status);
    List<User> findActiveUsers();
    
    // 批量操作
    List<User> saveAll(List<User> users);
    void deleteAll(List<String> userIds);
}