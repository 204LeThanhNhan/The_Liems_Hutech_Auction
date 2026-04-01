package com.example.auction.controller;

import com.example.auction.dto.NotificationDTO;
import com.example.auction.dto.UserActionRequest;
import com.example.auction.dto.UserManagementDTO;
import com.example.auction.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-management")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserManagementController {
    
    private final UserManagementService userManagementService;
    
    @GetMapping("/users")
    public ResponseEntity<List<UserManagementDTO>> getAllUsers() {
        return ResponseEntity.ok(userManagementService.getAllUsers());
    }
    
    @PostMapping("/action")
    public ResponseEntity<?> performAction(@RequestBody UserActionRequest request) {
        try {
            userManagementService.performUserAction(request);
            return ResponseEntity.ok(Map.of("message", "Action performed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @GetMapping("/notifications/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(userManagementService.getUserNotifications(userId));
    }
    
    @GetMapping("/notifications/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(userManagementService.getUnreadNotificationCount(userId));
    }
    
    @PostMapping("/notifications/{notificationId}/mark-read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        try {
            userManagementService.markNotificationAsRead(notificationId);
            return ResponseEntity.ok(Map.of("message", "Marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @PostMapping("/notifications/{userId}/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@PathVariable Long userId) {
        try {
            userManagementService.markAllNotificationsAsRead(userId);
            return ResponseEntity.ok(Map.of("message", "All marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
