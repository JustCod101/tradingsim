package com.tradingsim.application.dto.user;

import jakarta.validation.constraints.NotNull;

/**
 * 用户偏好设置请求DTO
 */
public class UserPreferencesRequest {
    @NotNull
    private String preferredDifficulty;
    
    @NotNull
    private String preferredTimeframe;
    
    private boolean notificationsEnabled;
    private boolean soundEnabled;
    private String theme;
    private String language;
    
    // 构造函数
    public UserPreferencesRequest() {}
    
    public UserPreferencesRequest(String preferredDifficulty, String preferredTimeframe,
                                 boolean notificationsEnabled, boolean soundEnabled,
                                 String theme, String language) {
        this.preferredDifficulty = preferredDifficulty;
        this.preferredTimeframe = preferredTimeframe;
        this.notificationsEnabled = notificationsEnabled;
        this.soundEnabled = soundEnabled;
        this.theme = theme;
        this.language = language;
    }
    
    // Getters and Setters
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
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    // UserService中需要的额外方法
    public String getWatchlist() {
        return null; // 暂时返回null，可以根据需要添加字段
    }

    public boolean isAutoSaveEnabled() {
        return true; // 默认启用自动保存
    }

    public boolean isShowTutorials() {
        return true; // 默认显示教程
    }

    public String getRiskTolerance() {
        return "MEDIUM"; // 默认中等风险承受能力
    }

    public String getTradingStyle() {
        return "BALANCED"; // 默认平衡交易风格
    }
}