package com.tradingsim.application.dto;

import java.time.Instant;

/**
 * 认证响应DTO
 */
public class AuthResponse {
    
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // 过期时间（秒）
    private UserInfo user;
    
    // Constructors
    public AuthResponse() {}
    
    public AuthResponse(String token, String refreshToken, Long expiresIn, UserInfo user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public UserInfo getUser() {
        return user;
    }
    
    public void setUser(UserInfo user) {
        this.user = user;
    }
    
    /**
     * 用户信息内嵌类
     */
    public static class UserInfo {
        private String id;
        private String username;
        private String email;
        private String fullName;
        private String status;
        private String roles;
        private boolean emailVerified;
        private Instant createdAt;
        
        // Constructors
        public UserInfo() {}
        
        public UserInfo(String id, String username, String email, String fullName, 
                       String status, String roles, boolean emailVerified, Instant createdAt) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.status = status;
            this.roles = roles;
            this.emailVerified = emailVerified;
            this.createdAt = createdAt;
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
        
        public Instant getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }
    }
}