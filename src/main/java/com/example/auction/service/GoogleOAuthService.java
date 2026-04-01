package com.example.auction.service;

import com.example.auction.dto.AuthResponse;
import com.example.auction.entity.User;
import com.example.auction.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${google.oauth.client-id}")
    private String clientId;
    
    public AuthResponse authenticateWithGoogle(String idToken) {
        try {
            // Verify Google ID token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), 
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId))
                    .build();
            
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            
            if (googleIdToken == null) {
                throw new RuntimeException("Invalid Google ID token");
            }
            
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            
            // Check if user exists
            User user = userRepository.findByEmail(email).orElse(null);
            
            if (user == null) {
                // Create new user
                user = new User();
                user.setUsername(email); // Use email as username
                user.setEmail(email);
                user.setDisplayName(name);
                user.setAvatarUrl(pictureUrl);
                user.setRole("USER");
                // Set random password (user won't use it, they login with Google)
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                
                user = userRepository.save(user);
            } else {
                // Update avatar if changed
                if (pictureUrl != null && !pictureUrl.equals(user.getAvatarUrl())) {
                    user.setAvatarUrl(pictureUrl);
                    userRepository.save(user);
                }
            }
            
            // Return auth response
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
            
        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }
}
