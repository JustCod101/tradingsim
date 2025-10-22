package com.tradingsim.domain.model.user;

import java.time.Instant;
import java.util.List;

/**
 * 用户偏好设置实体类
 * 
 * @author TradingSim Team
 */
public class UserPreferences {
    
    private String userId;
    private String preferredDifficulty;
    private String preferredTimeframe;
    private List<String> preferredStocks;
    private boolean notificationsEnabled;
    private boolean soundEnabled;
    private String theme;
    private boolean autoSaveEnabled;
    private boolean showTutorials;
    private String riskTolerance;
    private String tradingStyle;
    private Instant createdAt;
    private Instant updatedAt;
    
    // 构造函数
    public UserPreferences() {}
    
    public UserPreferences(String userId) {
        this.userId = userId;
        this.preferredDifficulty = "normal";
        this.preferredTimeframe = "1m";
        this.notificationsEnabled = true;
        this.soundEnabled = true;
        this.theme = "light";
        this.autoSaveEnabled = true;
        this.showTutorials = true;
        this.riskTolerance = "medium";
        this.tradingStyle = "balanced";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getPreferredDifficulty() {
        return preferredDifficulty;
    }
    
    public void setPreferredDifficulty(String preferredDifficulty) {
        this.preferredDifficulty = preferredDifficulty;
    }
    
    public String getPreferredTimeframe() {
        return preferredTimeframe;
    }
    
    public void setPreferredTimeframe(String preferredTimeframe) {
        this.preferredTimeframe = preferredTimeframe;
    }
    
    public List<String> getPreferredStocks() {
        return preferredStocks;
    }
    
    public void setPreferredStocks(List<String> preferredStocks) {
        this.preferredStocks = preferredStocks;
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
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // 业务方法
    public void updatePreferences(String preferredDifficulty, String preferredTimeframe, 
                                 String preferredStocks, boolean notificationsEnabled, 
                                 boolean soundEnabled, String theme, boolean autoSaveEnabled, 
                                 boolean showTutorials, String riskTolerance, String tradingStyle) {
        this.preferredDifficulty = preferredDifficulty;
        this.preferredTimeframe = preferredTimeframe;
        this.preferredStocks = List.of(preferredStocks);
        this.notificationsEnabled = notificationsEnabled;
        this.soundEnabled = soundEnabled;
        this.theme = theme;
        this.autoSaveEnabled = autoSaveEnabled;
        this.showTutorials = showTutorials;
        this.riskTolerance = riskTolerance;
        this.tradingStyle = tradingStyle;
        this.updatedAt = Instant.now();
    }
    
    @Override
    public String toString() {
        return "UserPreferences{" +
                "userId='" + userId + '\'' +
                ", preferredDifficulty='" + preferredDifficulty + '\'' +
                ", preferredTimeframe='" + preferredTimeframe + '\'' +
                ", notificationsEnabled=" + notificationsEnabled +
                ", soundEnabled=" + soundEnabled +
                ", theme='" + theme + '\'' +
                ", riskTolerance='" + riskTolerance + '\'' +
                ", tradingStyle='" + tradingStyle + '\'' +
                '}';
    }
}