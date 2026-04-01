package com.example.auction.service;

import com.example.auction.entity.PasswordResetToken;
import com.example.auction.entity.User;
import com.example.auction.repository.PasswordResetTokenRepository;
import com.example.auction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public void createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email này"));
        
        // Delete old tokens for this user
        tokenRepository.deleteByUser(user);
        
        // Create new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setCreatedAt(LocalDateTime.now());
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // Token valid for 1 hour
        resetToken.setUsed(false);
        
        tokenRepository.save(resetToken);
        
        // Send email
        sendResetEmail(user, token);
    }
    
    private void sendResetEmail(User user, String token) {
        String resetUrl = "https://hutech-auction.click/reset-password?token=" + token;
        
        String subject = "Đặt lại mật khẩu - HUTECH AUCTION";
        String body = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #e74c3c; text-align: center;">🔐 Đặt lại mật khẩu</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                    <p>Vui lòng nhấn vào nút bên dưới để hoàn tất việc đặt lại mật khẩu:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" 
                           style="background-color: #e74c3c; 
                                  color: white; 
                                  padding: 15px 30px; 
                                  text-decoration: none; 
                                  border-radius: 5px; 
                                  display: inline-block;
                                  font-weight: bold;">
                            Đặt lại mật khẩu
                        </a>
                    </div>
                    <p style="color: #666; font-size: 14px;">
                        <strong>Lưu ý:</strong> Link này chỉ có hiệu lực trong vòng 1 giờ.
                    </p>
                    <p style="color: #666; font-size: 14px;">
                        Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                    </p>
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
                    <p style="text-align: center; color: #999; font-size: 12px;">
                        © 2026 HUTECH AUCTION - Đại học Công nghệ TP.HCM
                    </p>
                </div>
            </body>
            </html>
            """, user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(), resetUrl);
        
        emailService.sendEmail(user.getEmail(), subject, body);
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));
        
        if (resetToken.isUsed()) {
            throw new RuntimeException("Token đã được sử dụng");
        }
        
        if (resetToken.isExpired()) {
            throw new RuntimeException("Token đã hết hạn");
        }
        
        // Update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
    
    public boolean validateToken(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> !t.isUsed() && !t.isExpired())
                .orElse(false);
    }
}
