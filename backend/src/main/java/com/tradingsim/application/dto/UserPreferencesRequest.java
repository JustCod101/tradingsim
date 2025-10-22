package com.tradingsim.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/**
 * 用户偏好设置请求DTO
 * 
 * @author TradingSim Team
 */
public class UserPreferencesRequest {
    
    @NotNull(message = "难度级别不能为空")
    @Pattern(regexp = "^(BEGINNER|INTERMEDIATE|ADVANCED|EXPERT)$", message = "难度级别必须是 BEGINNER, INTERMEDIATE, ADVANCED 或 EXPERT")
    private String difficulty;
    
    @NotNull(message = "时间范围不能为空")
    @Pattern(regexp = "^(1m|5m|15m|30m|1h|4h|1d)$", message = "时间范围必须是 1m, 5m, 15m, 30m, 1h, 4h 或 1d")
    private String timeframe;
    
    private List<String> watchlist;
    
    private boolean notificationsEnabled;
    private boolean soundEnabled;
    
    @Pattern(regexp = "^(LIGHT|DARK|AUTO)$", message = "主题必须是 LIGHT, DARK 或 AUTO")
    private String theme;
    
    private boolean autoSaveEnabled;
    private boolean showTutorials;
    
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH)$", message = "风险承受能力必须是 LOW, MEDIUM 或 HIGH")
    private String riskTolerance;
    
    @Pattern(regexp = "^(CONSERVATIVE|MODERATE|AGGRESSIVE|SCALPING)$", message = "交易风格必须是 CONSERVATIVE, MODERATE, AGGRESSIVE 或 SCALPING")
    private String tradingStyle;
    
    // 构造函数
    public UserPreferencesRequest() {}
    
    // Getters and Setters
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getTimeframe() {
        return timeframe;
    }
    
    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }
    
    public List<String> getWatchlist() {
        return watchlist;
    }
    
    public void setWatchlist(List<String> watchlist) {
        this.watchlist = watchlist;
    }
    
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String theme) {
        this.theme = theme;
    }
    
    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }
    
    public void setAutoSaveEnabled(boolean autoSaveEnabled) {
        this.autoSaveEnabled = autoSaveEnabled;
    }
    
    public boolean isShowTutorials() {
        return showTutorials;
    }
    
    public void setShowTutorials(boolean showTutorials) {
        this.showTutorials = showTutorials;
    }
    
    public String getRiskTolerance() {
        return riskTolerance;
    }
    
    public void setRiskTolerance(String riskTolerance) {
        this.riskTolerance = riskTolerance;
    }
    
    public String getTradingStyle() {
        return tradingStyle;
    }
    
    public void setTradingStyle(String tradingStyle) {
        this.tradingStyle = tradingStyle;
    }
}