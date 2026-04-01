package com.example.auction.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private Long notificationId;
    private String type;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private AdminInfoDTO admin;
    
    public NotificationDTO() {}
    
    public NotificationDTO(Long notificationId, String type, String message, Boolean isRead, 
                          LocalDateTime createdAt, AdminInfoDTO admin) {
        this.notificationId = notificationId;
        this.type = type;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.admin = admin;
    }
    
    // Getters and Setters
    public Long getNotificationId() {
        return notificationId;
    }
    
    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public AdminInfoDTO getAdmin() {
        return admin;
    }
    
    public void setAdmin(AdminInfoDTO admin) {
        this.admin = admin;
    }
    
    public static class AdminInfoDTO {
        private Long userId;
        private String username;
        private String displayName;
        private String avatarUrl;
        
        public AdminInfoDTO() {}
        
        public AdminInfoDTO(Long userId, String username, String displayName, String avatarUrl) {
            this.userId = userId;
            this.username = username;
            this.displayName = displayName;
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
        
        public String getAvatarUrl() {
            return avatarUrl;
        }
        
        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}
