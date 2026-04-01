package com.example.auction.controller;

import com.example.auction.dto.ForgotPasswordRequest;
import com.example.auction.dto.ResetPasswordRequest;
import com.example.auction.service.PasswordResetService;
import com.example.auction.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordResetController {
    
    private final PasswordResetService passwordResetService;
    private final UserService userService;
    
    @GetMapping("/hint-email")
    public ResponseEntity<?> getEmailHint(@RequestParam String identifier) {
        try {
            String maskedEmail = userService.getMaskedEmailByIdentifier(identifier);
            
            Map<String, String> response = new HashMap<>();
            response.put("maskedEmail", maskedEmail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.createPasswordResetToken(request.getEmail());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Vui lòng kiểm tra email của bạn để hoàn tất cài đặt lại mật khẩu");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateToken(token);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đặt lại mật khẩu thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
