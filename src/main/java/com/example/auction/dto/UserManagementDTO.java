package com.example.auction.dto;

public class UserManagementDTO {
    private Long userId;
    private String username;
    private String displayName;
    private String email;
    private String role;
    private String status;
    private String avatarUrl;
    
    public UserManagementDTO() {}
    
    public UserManagementDTO(Long userId, String username, String displayName, String email, String role, String status, String avatarUrl) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
        this.status = status;
        this.avatarUrl = avatarUrl;
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
