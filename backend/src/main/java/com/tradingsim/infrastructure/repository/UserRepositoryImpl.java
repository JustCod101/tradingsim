package com.tradingsim.infrastructure.repository;

import com.tradingsim.domain.model.user.User;
import com.tradingsim.domain.model.user.UserPreferences;
import com.tradingsim.domain.model.user.UserStats;
import com.tradingsim.domain.model.user.UserStatus;
import com.tradingsim.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问实现类
 * 
 * @author TradingSim Team
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    // User RowMapper
    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getString("id"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setFullName(rs.getString("full_name"));
            user.setAvatarUrl(rs.getString("avatar_url"));
            
            String statusCode = rs.getString("status");
            if (statusCode != null) {
                user.setStatus(UserStatus.fromCode(statusCode));
            }
            
            user.setRoles(rs.getString("roles"));
            user.setEmailVerified(rs.getBoolean("email_verified"));
            user.setPhone(rs.getString("phone"));
            
            java.sql.Date birthDate = rs.getDate("birth_date");
            if (birthDate != null) {
                user.setBirthDate(birthDate.toLocalDate());
            }
            
            user.setTimezone(rs.getString("timezone"));
            user.setLanguage(rs.getString("language"));
            
            java.sql.Timestamp lastLoginAt = rs.getTimestamp("last_login_at");
            if (lastLoginAt != null) {
                user.setLastLoginAt(lastLoginAt.toInstant());
            }
            
            user.setLastLoginIp(rs.getString("last_login_ip"));
            user.setCreatedAt(rs.getTimestamp("created_at").toInstant());
            user.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
            
            return user;
        }
    };
    
    // UserPreferences RowMapper
    private final RowMapper<UserPreferences> preferencesRowMapper = new RowMapper<UserPreferences>() {
        @Override
        public UserPreferences mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserPreferences preferences = new UserPreferences();
            preferences.setUserId(rs.getString("user_id"));
            preferences.setPreferredDifficulty(rs.getString("preferred_difficulty"));
            preferences.setPreferredTimeframe(rs.getString("preferred_timeframe"));
            
            String stocksStr = rs.getString("preferred_stocks");
            if (stocksStr != null && !stocksStr.isEmpty()) {
                preferences.setPreferredStocks(Arrays.asList(stocksStr.split(",")));
            }
            
            preferences.setNotificationsEnabled(rs.getBoolean("notifications_enabled"));
            preferences.setSoundEnabled(rs.getBoolean("sound_enabled"));
            preferences.setTheme(rs.getString("theme"));
            preferences.setAutoSaveEnabled(rs.getBoolean("auto_save_enabled"));
            preferences.setShowTutorials(rs.getBoolean("show_tutorials"));
            preferences.setRiskTolerance(rs.getString("risk_tolerance"));
            preferences.setTradingStyle(rs.getString("trading_style"));
            preferences.setCreatedAt(rs.getTimestamp("created_at").toInstant());
            preferences.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
            
            return preferences;
        }
    };
    
    // UserStats RowMapper
    private final RowMapper<UserStats> statsRowMapper = new RowMapper<UserStats>() {
        @Override
        public UserStats mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserStats stats = new UserStats();
            stats.setUserId(rs.getString("user_id"));
            stats.setTotalSessions(rs.getInt("total_sessions"));
            stats.setCompletedSessions(rs.getInt("completed_sessions"));
            // completion_rate is calculated automatically by getCompletionRate() method
            stats.setTotalScore(rs.getBigDecimal("total_score"));
            stats.setBestScore(rs.getBigDecimal("best_score"));
            stats.setAverageScore(rs.getBigDecimal("average_score"));
            stats.setTotalPnl(rs.getBigDecimal("total_pnl"));
            stats.setBestPnl(rs.getBigDecimal("best_pnl"));
            stats.setWorstPnl(rs.getBigDecimal("worst_pnl"));
            stats.setWinRate(rs.getBigDecimal("win_rate"));
            stats.setTotalTrades(rs.getInt("total_trades"));
            stats.setWinningTrades(rs.getInt("winning_trades"));
            stats.setLosingTrades(rs.getInt("losing_trades"));
            stats.setAvgResponseTimeMs(rs.getLong("avg_response_time_ms"));
            stats.setMaxConsecutiveWins(rs.getInt("max_consecutive_wins"));
            stats.setMaxConsecutiveLosses(rs.getInt("max_consecutive_losses"));
            stats.setTotalPlayTimeMinutes(rs.getLong("total_play_time_minutes"));
            
            Integer rankPosition = rs.getObject("rank_position", Integer.class);
            stats.setRankPosition(rankPosition);
            
            stats.setRankPercentile(rs.getBigDecimal("rank_percentile"));
            stats.setCreatedAt(rs.getTimestamp("created_at").toInstant());
            stats.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
            
            return stats;
        }
    };
    
    @Override
    public Optional<User> findById(String id) {
        try {
            String sql = "SELECT * FROM users WHERE id = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        try {
            String sql = "SELECT * FROM users WHERE username = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        try {
            String sql = "SELECT * FROM users WHERE email = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, userRowMapper);
    }
    
    @Override
    public User save(User user) {
        if (user.getId() == null) {
            // Insert new user
            user.setId(java.util.UUID.randomUUID().toString());
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            
            String sql = """
                INSERT INTO users (id, username, email, password_hash, full_name, avatar_url, 
                                 status, roles, email_verified, phone, birth_date, timezone, 
                                 language, last_login_at, last_login_ip, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            
            jdbcTemplate.update(sql,
                user.getId(), user.getUsername(), user.getEmail(), user.getPasswordHash(),
                user.getFullName(), user.getAvatarUrl(), 
                user.getStatus() != null ? user.getStatus().getCode() : null,
                user.getRoles(), user.isEmailVerified(), user.getPhone(),
                user.getBirthDate(), user.getTimezone(), user.getLanguage(),
                user.getLastLoginAt(), user.getLastLoginIp(),
                user.getCreatedAt(), user.getUpdatedAt());
        } else {
            // Update existing user
            user.setUpdatedAt(Instant.now());
            
            String sql = """
                UPDATE users SET username = ?, email = ?, password_hash = ?, full_name = ?, 
                               avatar_url = ?, status = ?, roles = ?, email_verified = ?, 
                               phone = ?, birth_date = ?, timezone = ?, language = ?, 
                               last_login_at = ?, last_login_ip = ?, updated_at = ?
                WHERE id = ?
                """;
            
            jdbcTemplate.update(sql,
                user.getUsername(), user.getEmail(), user.getPasswordHash(),
                user.getFullName(), user.getAvatarUrl(),
                user.getStatus() != null ? user.getStatus().getCode() : null,
                user.getRoles(), user.isEmailVerified(), user.getPhone(),
                user.getBirthDate(), user.getTimezone(), user.getLanguage(),
                user.getLastLoginAt(), user.getLastLoginIp(),
                user.getUpdatedAt(), user.getId());
        }
        return user;
    }
    
    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }
    
    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
    
    @Override
    public Optional<UserPreferences> findPreferencesByUserId(String userId) {
        try {
            String sql = "SELECT * FROM user_preferences WHERE user_id = ?";
            UserPreferences preferences = jdbcTemplate.queryForObject(sql, preferencesRowMapper, userId);
            return Optional.ofNullable(preferences);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public UserPreferences savePreferences(UserPreferences preferences) {
        Optional<UserPreferences> existing = findPreferencesByUserId(preferences.getUserId());
        
        if (existing.isEmpty()) {
            // Insert new preferences
            preferences.setCreatedAt(Instant.now());
            preferences.setUpdatedAt(Instant.now());
            
            String sql = """
                INSERT INTO user_preferences (user_id, preferred_difficulty, preferred_timeframe, 
                                            preferred_stocks, notifications_enabled, sound_enabled, 
                                            theme, auto_save_enabled, show_tutorials, risk_tolerance, 
                                            trading_style, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            
            String stocksStr = preferences.getPreferredStocks() != null ? 
                String.join(",", preferences.getPreferredStocks()) : null;
            
            jdbcTemplate.update(sql,
                preferences.getUserId(), preferences.getPreferredDifficulty(),
                preferences.getPreferredTimeframe(), stocksStr,
                preferences.isNotificationsEnabled(), preferences.isSoundEnabled(),
                preferences.getTheme(), preferences.isAutoSaveEnabled(),
                preferences.isShowTutorials(), preferences.getRiskTolerance(),
                preferences.getTradingStyle(), preferences.getCreatedAt(),
                preferences.getUpdatedAt());
        } else {
            // Update existing preferences
            preferences.setUpdatedAt(Instant.now());
            
            String sql = """
                UPDATE user_preferences SET preferred_difficulty = ?, preferred_timeframe = ?, 
                                          preferred_stocks = ?, notifications_enabled = ?, 
                                          sound_enabled = ?, theme = ?, auto_save_enabled = ?, 
                                          show_tutorials = ?, risk_tolerance = ?, trading_style = ?, 
                                          updated_at = ?
                WHERE user_id = ?
                """;
            
            String stocksStr = preferences.getPreferredStocks() != null ? 
                String.join(",", preferences.getPreferredStocks()) : null;
            
            jdbcTemplate.update(sql,
                preferences.getPreferredDifficulty(), preferences.getPreferredTimeframe(),
                stocksStr, preferences.isNotificationsEnabled(),
                preferences.isSoundEnabled(), preferences.getTheme(),
                preferences.isAutoSaveEnabled(), preferences.isShowTutorials(),
                preferences.getRiskTolerance(), preferences.getTradingStyle(),
                preferences.getUpdatedAt(), preferences.getUserId());
        }
        return preferences;
    }
    
    @Override
    public void deletePreferencesByUserId(String userId) {
        String sql = "DELETE FROM user_preferences WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
    
    @Override
    public Optional<UserStats> findStatsByUserId(String userId) {
        try {
            String sql = "SELECT * FROM user_stats WHERE user_id = ?";
            UserStats stats = jdbcTemplate.queryForObject(sql, statsRowMapper, userId);
            return Optional.ofNullable(stats);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public UserStats saveStats(UserStats stats) {
        Optional<UserStats> existing = findStatsByUserId(stats.getUserId());
        
        if (existing.isEmpty()) {
            // Insert new stats
            stats.setCreatedAt(Instant.now());
            stats.setUpdatedAt(Instant.now());
            
            String sql = """
                INSERT INTO user_stats (user_id, total_sessions, completed_sessions,
                                      total_score, best_score, average_score, total_pnl, best_pnl,
                                      worst_pnl, win_rate, total_trades, winning_trades, losing_trades,
                                      avg_response_time_ms, max_consecutive_wins, max_consecutive_losses,
                                      total_play_time_minutes, rank_position, rank_percentile,
                                      created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            
            jdbcTemplate.update(sql,
                stats.getUserId(), stats.getTotalSessions(), stats.getCompletedSessions(),
                stats.getTotalScore(), stats.getBestScore(),
                stats.getAverageScore(), stats.getTotalPnl(), stats.getBestPnl(),
                stats.getWorstPnl(), stats.getWinRate(), stats.getTotalTrades(),
                stats.getWinningTrades(), stats.getLosingTrades(), stats.getAvgResponseTimeMs(),
                stats.getMaxConsecutiveWins(), stats.getMaxConsecutiveLosses(),
                stats.getTotalPlayTimeMinutes(), stats.getRankPosition(),
                stats.getRankPercentile(), stats.getCreatedAt(), stats.getUpdatedAt());
        } else {
            // Update existing stats
            stats.setUpdatedAt(Instant.now());
            
            String sql = """
                UPDATE user_stats SET total_sessions = ?, completed_sessions = ?,
                                    total_score = ?, best_score = ?, average_score = ?, total_pnl = ?,
                                    best_pnl = ?, worst_pnl = ?, win_rate = ?, total_trades = ?,
                                    winning_trades = ?, losing_trades = ?, avg_response_time_ms = ?,
                                    max_consecutive_wins = ?, max_consecutive_losses = ?,
                                    total_play_time_minutes = ?, rank_position = ?, rank_percentile = ?,
                                    updated_at = ?
                WHERE user_id = ?
                """;
            
            jdbcTemplate.update(sql,
                stats.getTotalSessions(), stats.getCompletedSessions(),
                stats.getTotalScore(), stats.getBestScore(), stats.getAverageScore(),
                stats.getTotalPnl(), stats.getBestPnl(), stats.getWorstPnl(),
                stats.getWinRate(), stats.getTotalTrades(), stats.getWinningTrades(),
                stats.getLosingTrades(), stats.getAvgResponseTimeMs(),
                stats.getMaxConsecutiveWins(), stats.getMaxConsecutiveLosses(),
                stats.getTotalPlayTimeMinutes(), stats.getRankPosition(),
                stats.getRankPercentile(), stats.getUpdatedAt(), stats.getUserId());
        }
        return stats;
    }
    
    @Override
    public void deleteStatsByUserId(String userId) {
        String sql = "DELETE FROM user_stats WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
    
    @Override
    public List<UserStats> findTopUsersByScore(int limit) {
        String sql = "SELECT * FROM user_stats ORDER BY total_score DESC LIMIT ?";
        return jdbcTemplate.query(sql, statsRowMapper, limit);
    }
    
    @Override
    public List<UserStats> findTopUsersByPnl(int limit) {
        String sql = "SELECT * FROM user_stats ORDER BY total_pnl DESC LIMIT ?";
        return jdbcTemplate.query(sql, statsRowMapper, limit);
    }
    
    @Override
    public Long getUserRankByScore(String userId) {
        String sql = """
            SELECT rank_position FROM (
                SELECT user_id, ROW_NUMBER() OVER (ORDER BY total_score DESC) as rank_position
                FROM user_stats
            ) ranked WHERE user_id = ?
            """;
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    @Override
    public Long getUserRankByPnl(String userId) {
        String sql = """
            SELECT rank_position FROM (
                SELECT user_id, ROW_NUMBER() OVER (ORDER BY total_pnl DESC) as rank_position
                FROM user_stats
            ) ranked WHERE user_id = ?
            """;
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    @Override
    public List<User> findByUsernameContaining(String username) {
        String sql = "SELECT * FROM users WHERE username ILIKE ? ORDER BY username";
        return jdbcTemplate.query(sql, userRowMapper, "%" + username + "%");
    }
    
    @Override
    public List<User> findByStatus(String status) {
        String sql = "SELECT * FROM users WHERE status = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, userRowMapper, status);
    }
    
    @Override
    public List<User> findActiveUsers() {
        String sql = "SELECT * FROM users WHERE status = 'ACTIVE' ORDER BY last_login_at DESC";
        return jdbcTemplate.query(sql, userRowMapper);
    }
    
    @Override
    public List<User> saveAll(List<User> users) {
        for (User user : users) {
            save(user);
        }
        return users;
    }
    
    @Override
    public void deleteAll(List<String> userIds) {
        if (userIds != null && !userIds.isEmpty()) {
            String sql = "DELETE FROM users WHERE id = ANY(?)";
            jdbcTemplate.update(sql, userIds.toArray(new String[0]));
        }
    }
}