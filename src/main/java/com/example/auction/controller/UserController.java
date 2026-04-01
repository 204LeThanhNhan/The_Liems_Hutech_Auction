package com.example.auction.controller;

import com.example.auction.dto.UpdateProfileRequest;
import com.example.auction.entity.User;
import com.example.auction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "https://hutech-auction.click"})
public class UserController {
    
    private final UserRepository userRepository;
    
    @GetMapping("/{userId}/status")
    public ResponseEntity<Map<String, String>> getUserStatus(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Map<String, String> response = new HashMap<>();
        response.put("status", user.getStatus() != null ? user.getStatus() : "ACTIVE");
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable Long userId, 
                                         @Valid @RequestBody UpdateProfileRequest request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Update user fields
            user.setDisplayName(request.getDisplayName());
            user.setEmail(request.getEmail());
            if (request.getAvatarUrl() != null) {
                user.setAvatarUrl(request.getAvatarUrl());
            }
            
            // Save to database
            User updatedUser = userRepository.save(user);
            
            // Return updated user data
            Map<String, Object> response = new HashMap<>();
            response.put("userId", updatedUser.getUserId());
            response.put("username", updatedUser.getUsername());
            response.put("displayName", updatedUser.getDisplayName());
            response.put("email", updatedUser.getEmail());
            response.put("avatarUrl", updatedUser.getAvatarUrl());
            response.put("role", updatedUser.getRole());
            response.put("status", updatedUser.getStatus());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Update profile failed: " + e.getMessage());
        }
    }
}
