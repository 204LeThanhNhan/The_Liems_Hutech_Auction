package com.example.auction.service;

import com.example.auction.dto.NotificationDTO;
import com.example.auction.dto.UserActionRequest;
import com.example.auction.dto.UserManagementDTO;
import com.example.auction.entity.User;
import com.example.auction.entity.UserNotification;
import com.example.auction.repository.UserNotificationRepository;
import com.example.auction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    
    private final UserRepository userRepository;
    private final UserNotificationRepository notificationRepository;
    
    public List<UserManagementDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserManagementDTO(
                        user.getUserId(),
                        user.getUsername(),
                        user.getDisplayName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getStatus() != null ? user.getStatus() : "ACTIVE",
                        user.getAvatarUrl()
                ))
                .collect(Collectors.toList());
    }
    
    public void performUserAction(UserActionRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User admin = userRepository.findById(request.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Only admin can perform this action");
        }
        
        // Update user status
        String action = request.getAction().toUpperCase();
        switch (action) {
            case "BAN":
                user.setStatus("BANNED");
                break;
            case "WARN":
                user.setStatus("WARNED");
                break;
            case "UNBAN":
                user.setStatus("ACTIVE");
                break;
            default:
                throw new RuntimeException("Invalid action");
        }
        
        userRepository.save(user);
        
        // Create notification
        UserNotification notification = new UserNotification();
        notification.setUser(user);
        notification.setAdmin(admin);
        notification.setType(action);
        notification.setMessage(request.getReason());
        notification.setIsRead(false);
        
        notificationRepository.save(notification);
    }
    
    public List<NotificationDTO> getUserNotifications(Long userId) {
        List<UserNotification> notifications = notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        
        return notifications.stream()
                .map(notif -> {
                    NotificationDTO.AdminInfoDTO adminInfo = null;
                    if (notif.getAdmin() != null) {
                        adminInfo = new NotificationDTO.AdminInfoDTO(
                                notif.getAdmin().getUserId(),
                                notif.getAdmin().getUsername(),
                                notif.getAdmin().getDisplayName(),
                                notif.getAdmin().getAvatarUrl()
                        );
                    }
                    
                    return new NotificationDTO(
                            notif.getNotificationId(),
                            notif.getType(),
                            notif.getMessage(),
                            notif.getIsRead(),
                            notif.getCreatedAt(),
                            adminInfo
                    );
                })
                .collect(Collectors.toList());
    }
    
    public Long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
    
    public void markNotificationAsRead(Long notificationId) {
        UserNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
    
    public void markAllNotificationsAsRead(Long userId) {
        List<UserNotification> notifications = notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }
}
