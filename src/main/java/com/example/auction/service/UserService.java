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
