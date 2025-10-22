package com.tradingsim.application.dto.user;

import com.tradingsim.domain.model.user.UserPreferences;
import java.time.LocalDateTime;

/**
 * 用户偏好设置响应DTO
 */
public class UserPreferencesResponse {
    private String userId;
    private String preferredDifficulty;
    private String preferredTimeframe;
    private boolean notificationsEnabled;
    private boolean soundEnabled;
    private String theme;
    private String language;
    private LocalDateTime updatedAt;
    
    // 构造函数
    public UserPreferencesResponse() {}
    
    public UserPreferencesResponse(UserPreferences preferences) {
        this.userId = preferences.getUserId();
        this.preferredDifficulty = preferences.getPreferredDifficulty();
        this.preferredTimeframe = preferences.getPreferredTimeframe();
        this.notificationsEnabled = preferences.isNotificationsEnabled();
        this.soundEnabled = preferences.isSoundEnabled();
        this.theme = preferences.getTheme();
        this.language = "zh-CN"; // 默认语言
        this.updatedAt = preferences.getUpdatedAt() != null ? 
            LocalDateTime.ofInstant(preferences.getUpdatedAt(), java.time.ZoneOffset.UTC) : null;
    }
    
    public UserPreferencesResponse(String userId, String preferredDifficulty, 
                                  String preferredTimeframe, boolean notificationsEnabled,
                                  boolean soundEnabled, String theme, String language,
                                  LocalDateTime updatedAt) {
        this.userId = userId;
        this.preferredDifficulty = preferredDifficulty;
        this.preferredTimeframe = preferredTimeframe;
        this.notificationsEnabled = notificationsEnabled;
        this.soundEnabled = soundEnabled;
        this.theme = theme;
        this.language = language;
        this.updatedAt = updatedAt;
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
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}