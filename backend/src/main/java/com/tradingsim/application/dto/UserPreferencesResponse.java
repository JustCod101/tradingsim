package com.tradingsim.application.dto;

import com.tradingsim.domain.model.user.UserPreferences;

import java.util.List;

/**
 * 用户偏好设置响应DTO
 * 
 * @author TradingSim Team
 */
public class UserPreferencesResponse {
    
    private String userId;
    private String difficulty;
    private String timeframe;
    private List<String> watchlist;
    private boolean notificationsEnabled;
    private boolean soundEnabled;
    private String theme;
    private boolean autoSaveEnabled;
    private boolean showTutorials;
    private String riskTolerance;
    private String tradingStyle;
    
    // 构造函数
    public UserPreferencesResponse() {}
    
    public UserPreferencesResponse(UserPreferences preferences) {
        this.userId = preferences.getUserId();
        this.difficulty = preferences.getPreferredDifficulty();
        this.timeframe = preferences.getPreferredTimeframe();
        this.watchlist = preferences.getPreferredStocks();
        this.notificationsEnabled = preferences.isNotificationsEnabled();
        this.soundEnabled = preferences.isSoundEnabled();
        this.theme = preferences.getTheme();
        this.autoSaveEnabled = preferences.isAutoSaveEnabled();
        this.showTutorials = preferences.isShowTutorials();
        this.riskTolerance = preferences.getRiskTolerance();
        this.tradingStyle = preferences.getTradingStyle();
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
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