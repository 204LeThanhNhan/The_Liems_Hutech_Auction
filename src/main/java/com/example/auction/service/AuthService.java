package com.example.auction.service;

import com.example.auction.dto.AuthResponse;
import com.example.auction.dto.LoginRequest;
import com.example.auction.dto.RegisterRequest;
import com.example.auction.entity.User;
import com.example.auction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    public AuthResponse register(RegisterRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username này đã tồn tại");
        }
        
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email này đã tồn tại");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername());
        user.setRole("USER");
        
        User savedUser = userRepository.save(user);
        
        // welcome email
        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getDisplayName());
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
        
        return new AuthResponse(
            savedUser.getUserId(),
            savedUser.getUsername(),
            savedUser.getDisplayName(),
            savedUser.getEmail(),
            savedUser.getRole(),
            savedUser.getStatus() != null ? savedUser.getStatus() : "ACTIVE",
            savedUser.getAvatarUrl(),
            "Đăng ký thành công"
        );
    }
    
    public AuthResponse login(LoginRequest request) {
        User user;
        
        // Check if input contains @ (email format)
        if (request.getUsername().contains("@gmail.com")) {
            user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng"));
        } else {
            user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng");
        }
        
        return new AuthResponse(
            user.getUserId(),
            user.getUsername(),
            user.getDisplayName(),
            user.getEmail(),
            user.getRole(),
            user.getStatus() != null ? user.getStatus() : "ACTIVE",
            user.getAvatarUrl(),
            "Đăng nhập thành công"
        );
    }
}
