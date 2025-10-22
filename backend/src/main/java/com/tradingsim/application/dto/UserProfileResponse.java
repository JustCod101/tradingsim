package com.tradingsim.application.dto;

import com.tradingsim.domain.model.user.User;
import com.tradingsim.domain.model.user.UserStatus;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 用户资料响应DTO
 * 
 * @author TradingSim Team
 */
public class UserProfileResponse {
    
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String status;
    private String roles;
    private boolean emailVerified;
    private String phone;
    private LocalDate birthDate;
    private String timezone;
    private String language;
    private Instant lastLoginAt;
    private Instant createdAt;
    
    // 构造函数
    public UserProfileResponse() {}
    
    public UserProfileResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.avatarUrl = user.getAvatarUrl();
        this.status = user.getStatus() != null ? user.getStatus().getCode() : null;
        this.roles = user.getRoles();
        this.emailVerified = user.isEmailVerified();
        this.phone = user.getPhone();
        this.birthDate = user.getBirthDate();
        this.timezone = user.getTimezone();
        this.language = user.getLanguage();
        this.lastLoginAt = user.getLastLoginAt();
        this.createdAt = user.getCreatedAt();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
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
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getRoles() {
        return roles;
    }
    
    public void setRoles(String roles) {
        this.roles = roles;
    }
    
    public boolean isEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
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
    
    public Instant getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}