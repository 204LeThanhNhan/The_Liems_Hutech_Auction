package com.example.auction.service;

import com.example.auction.dto.UpdateUserRequest;
import com.example.auction.dto.UserResponse;
import com.example.auction.entity.User;
import com.example.auction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public String getMaskedEmailByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        return maskEmail(user.getEmail());
    }
    
    public String getMaskedEmailByIdentifier(String identifier) {
        // Tìm theo username hoặc email
        User user = userRepository.findByUsername(identifier)
            .orElseGet(() -> userRepository.findByEmail(identifier)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng")));
        
        return maskEmail(user.getEmail());
    }
    
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "";
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return email;
        }
        
        // Show first and last character, mask the rest
        String masked = username.charAt(0) + "*".repeat(username.length() - 2) + username.charAt(username.length() - 1);
        return masked + "@" + domain;
    }
    
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return new UserResponse(
            user.getUserId(),
            user.getUsername(),
            user.getDisplayName(),
            user.getEmail(),
            user.getRole(),
            user.getAvatarUrl()
        );
    }
    
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        
        if (request.getEmail() != null) {
            // Check if email already exists for another user
            if (userRepository.existsByEmail(request.getEmail()) && 
                !user.getEmail().equals(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        User updatedUser = userRepository.save(user);
        
        return new UserResponse(
            updatedUser.getUserId(),
            updatedUser.getUsername(),
            updatedUser.getDisplayName(),
            updatedUser.getEmail(),
            updatedUser.getRole(),
            updatedUser.getAvatarUrl()
        );
    }
}
