package com.tradingsim.application.dto.user;

import com.tradingsim.domain.model.user.User;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户资料响应DTO
 */
public class UserProfileResponse {
    private String userId;
    private String username;
    private String email;
    private String avatarUrl;
    private String displayName;
    private String bio;
    private String fullName;
    private String phone;
    private LocalDate birthDate;
    private String timezone;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private boolean isActive;
    
    // 构造函数
    public UserProfileResponse() {}
    
    public UserProfileResponse(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.avatarUrl = user.getAvatarUrl();
        this.displayName = user.getFullName();
        this.fullName = user.getFullName();
        this.phone = user.getPhone();
        this.birthDate = user.getBirthDate();
        this.timezone = user.getTimezone();
        this.language = user.getLanguage();
        this.createdAt = user.getCreatedAt() != null ? LocalDateTime.ofInstant(user.getCreatedAt(), java.time.ZoneOffset.UTC) : null;
        this.lastLoginAt = user.getLastLoginAt() != null ? LocalDateTime.ofInstant(user.getLastLoginAt(), java.time.ZoneOffset.UTC) : null;
        this.isActive = user.getStatus() != null && user.getStatus().toString().equals("ACTIVE");
    }
    
    public UserProfileResponse(String userId, String username, String email, 
                              String avatarUrl, String displayName, String bio,
                              LocalDateTime createdAt, LocalDateTime lastLoginAt, 
                              boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.displayName = displayName;
        this.bio = bio;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.isActive = isActive;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public LocalDate getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
}