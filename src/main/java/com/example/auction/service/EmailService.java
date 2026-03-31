package com.example.auction.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendWelcomeEmail(String toEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("22112000nhan@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Chào mừng bạn đến với HUTECH AUCTION!");

            String htmlContent = buildWelcomeEmailTemplate(userName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildWelcomeEmailTemplate(String userName) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Chào mừng đến với HUTECH AUCTION</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7fa;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f7fa; padding: 40px 0;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #ff6b35 0%%, #f7931e 50%%, #ffd700 100%%); padding: 40px 30px; text-align: center;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 32px; font-weight: 700; text-shadow: 2px 2px 4px rgba(0,0,0,0.2);">
                                            🏆 HUTECH AUCTION
                                        </h1>
                                        <p style="margin: 10px 0 0 0; color: #ffffff; font-size: 16px; opacity: 0.95;">
                                            Sàn đấu giá trực tuyến hàng đầu Việt Nam
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="margin: 0 0 20px 0; color: #333333; font-size: 24px; font-weight: 600;">
                                            Xin chào %s! 👋
                                        </h2>
                                        <p style="margin: 0 0 15px 0; color: #555555; font-size: 16px; line-height: 1.6;">
                                            Chúc mừng bạn đã đăng ký thành công tài khoản tại <strong>HUTECH AUCTION</strong>!
                                        </p>
                                        <p style="margin: 0 0 25px 0; color: #555555; font-size: 16px; line-height: 1.6;">
                                            Bạn đã sẵn sàng để khám phá hàng nghìn phiên đấu giá hấp dẫn với các sản phẩm chất lượng cao.
                                        </p>
                                        
                                        <!-- CTA Button -->
                                        <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 30px 0;">
                                            <tr>
                                                <td align="center">
                                                    <a href="http://localhost:5173/auctions" style="display: inline-block; padding: 16px 40px; background: linear-gradient(135deg, #ff6b35 0%%, #f7931e 100%%); color: #ffffff; text-decoration: none; border-radius: 8px; font-size: 16px; font-weight: 600; box-shadow: 0 4px 12px rgba(255, 107, 53, 0.4);">
                                                        🔥 Khám phá ngay
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <!-- Features -->
                                        <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 30px 0; border-top: 2px solid #f0f0f0; padding-top: 30px;">
                                            <tr>
                                                <td style="padding: 15px; background-color: #fff8f0; border-radius: 8px; margin-bottom: 10px;">
                                                    <p style="margin: 0; color: #ff6b35; font-size: 18px; font-weight: 600;">
                                                        ✨ Điều gì đang chờ bạn?
                                                    </p>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding: 10px 0;">
                                                    <p style="margin: 0 0 10px 0; color: #555555; font-size: 15px;">
                                                        🎯 <strong>Đấu giá real-time:</strong> Trải nghiệm đấu giá trực tiếp với hệ thống WebSocket
                                                    </p>
                                                    <p style="margin: 0 0 10px 0; color: #555555; font-size: 15px;">
                                                        🔒 <strong>An toàn & minh bạch:</strong> Giao dịch được bảo mật tuyệt đối
                                                    </p>
                                                    <p style="margin: 0 0 10px 0; color: #555555; font-size: 15px;">
                                                        💎 <strong>Sản phẩm đa dạng:</strong> Từ điện tử, thời trang đến đồ cổ
                                                    </p>
                                                    <p style="margin: 0; color: #555555; font-size: 15px;">
                                                        ⚡ <strong>Cộng đồng sôi động:</strong> Hơn 1,250+ người dùng đang tham gia
                                                    </p>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <p style="margin: 25px 0 0 0; color: #777777; font-size: 14px; line-height: 1.6;">
                                            Nếu bạn có bất kỳ câu hỏi nào, đừng ngần ngại liên hệ với chúng tôi qua email 
                                            <a href="mailto:hutech.auction@hutech.edu.vn" style="color: #ff6b35; text-decoration: none;">hutech.auction@hutech.edu.vn</a>
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e9ecef;">
                                        <p style="margin: 0 0 10px 0; color: #666666; font-size: 14px;">
                                            <strong>HUTECH AUCTION</strong>
                                        </p>
                                        <p style="margin: 0 0 5px 0; color: #888888; font-size: 13px;">
                                            475A Điện Biên Phủ, P.25, Q.Bình Thạnh, TP.HCM
                                        </p>
                                        <p style="margin: 0 0 15px 0; color: #888888; font-size: 13px;">
                                            📞 028 5445 7777 | 📧 hutech.auction@hutech.edu.vn
                                        </p>
                                        <p style="margin: 0; color: #999999; font-size: 12px;">
                                            © 2026 HUTECH AUCTION - Đại học Công nghệ TP.HCM (HUTECH)
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(userName);
    }
}
