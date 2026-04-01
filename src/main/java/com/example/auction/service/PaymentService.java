package com.example.auction.service;

import com.example.auction.dto.PaymentInfoResponse;
import com.example.auction.dto.PaymentStatusResponse;
import com.example.auction.entity.Auction;
import com.example.auction.entity.Payment;
import com.example.auction.entity.User;
import com.example.auction.entity.WinnerChat;
import com.example.auction.repository.AuctionRepository;
import com.example.auction.repository.PaymentRepository;
import com.example.auction.repository.UserRepository;
import com.example.auction.repository.WinnerChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private AuctionRepository auctionRepository;
    
    @Autowired
    private WinnerChatRepository winnerChatRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    public PaymentInfoResponse getPaymentInfo(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        Long paidAmount = paymentRepository.getTotalPaidAmount(auctionId);
        if (paidAmount == null) {
            paidAmount = 0L;
        }
        
        Long totalAmount = auction.getEndPrice().longValue();
        Long remainingAmount = totalAmount - paidAmount;
        
        boolean paymentCompleted = remainingAmount <= 0;
        
        return new PaymentInfoResponse(paidAmount, remainingAmount, totalAmount, paymentCompleted);
    }
    
    public PaymentStatusResponse getPaymentStatus(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setStatus(payment.getStatus());
        response.setAmount(payment.getAmount());
        
        if ("SUCCESS".equals(payment.getStatus())) {
            response.setMessage("Payment successful");
        } else if ("FAILED".equals(payment.getStatus())) {
            response.setMessage("Payment failed");
        } else {
            response.setMessage("Payment pending");
        }
        
        return response;
    }
    
    // Track which payments have already sent notifications to prevent duplicates
    private final java.util.Set<String> notificationSentCache = java.util.Collections.synchronizedSet(
        new java.util.HashSet<>()
    );
    
    public void sendPaymentNotificationToChat(Long auctionId, Long amount, String status) {
        try {
            // Create unique key for this notification
            String notificationKey = auctionId + "_" + amount + "_" + status + "_" + System.currentTimeMillis() / 10000;
            
            // Check if notification already sent (within 10 seconds window)
            if (notificationSentCache.contains(notificationKey)) {
                System.out.println("Notification already sent for this payment, skipping duplicate...");
                return;
            }
            
            System.out.println("=== Sending Payment Notification to Chat ===");
            System.out.println("AuctionId: " + auctionId);
            System.out.println("Amount: " + amount);
            System.out.println("Status: " + status);
            
            Auction auction = auctionRepository.findById(auctionId)
                    .orElseThrow(() -> new RuntimeException("Auction not found"));
            
            System.out.println("Auction found: " + auction.getAuctionName());
            System.out.println("WinnerId: " + auction.getWinnerId());
            
            // Mark notification as sent
            notificationSentCache.add(notificationKey);
            
            // Clean up old entries (keep only last 100)
            if (notificationSentCache.size() > 100) {
                notificationSentCache.clear();
            }
            
            // Get winner and seller
            User winner = userRepository.findById(auction.getWinnerId())
                    .orElseThrow(() -> new RuntimeException("Winner not found"));
            User seller = auction.getUser(); // Seller is the auction creator
            
            System.out.println("Winner: " + winner.getUsername());
            System.out.println("Seller: " + seller.getUsername());
            
            // Format amount
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedAmount = formatter.format(amount);
            
            // Create system message
            WinnerChat systemMessage = new WinnerChat();
            systemMessage.setAuction(auction);
            systemMessage.setWinner(winner);
            systemMessage.setSeller(seller);
            systemMessage.setSenderId(-1L); // System message (use -1 instead of null)
            systemMessage.setMessageTime(LocalDateTime.now());
            systemMessage.setIsRead(false);
            
            if ("SUCCESS".equals(status)) {
                // Get payment info
                PaymentInfoResponse paymentInfo = getPaymentInfo(auctionId);
                
                String message = String.format("💰 Thanh toán thành công %s. Còn lại: %s", 
                        formattedAmount, 
                        formatter.format(paymentInfo.getRemainingAmount()));
                
                systemMessage.setMessage(message);
                
                System.out.println("System message: " + message);
                
                // Save to database
                WinnerChat savedMessage = winnerChatRepository.save(systemMessage);
                System.out.println("System message saved to database");
                
                // Send via WebSocket ONCE
                messagingTemplate.convertAndSend("/topic/winner-chat/" + auctionId, savedMessage);
                System.out.println("System message sent via WebSocket to /topic/winner-chat/" + auctionId);
                
                // Check if payment completed - send separate message
                if (paymentInfo.isPaymentCompleted()) {
                    WinnerChat completedMessage = new WinnerChat();
                    completedMessage.setAuction(auction);
                    completedMessage.setWinner(winner);
                    completedMessage.setSeller(seller);
                    completedMessage.setSenderId(-1L); // System message
                    completedMessage.setMessageTime(LocalDateTime.now());
                    completedMessage.setIsRead(false);
                    completedMessage.setMessage("✅ Đã thanh toán đủ! Giao dịch hoàn tất.");
                    
                    WinnerChat savedCompletedMessage = winnerChatRepository.save(completedMessage);
                    System.out.println("Payment completed message saved");
                    
                    // Send via WebSocket ONCE
                    messagingTemplate.convertAndSend("/topic/winner-chat/" + auctionId, savedCompletedMessage);
                    System.out.println("Payment completed message sent via WebSocket");
                }
            } else {
                systemMessage.setMessage("❌ Thanh toán " + formattedAmount + " thất bại. Vui lòng thử lại.");
                
                winnerChatRepository.save(systemMessage);
                
                // Send via WebSocket
                messagingTemplate.convertAndSend("/topic/winner-chat/" + auctionId, systemMessage);
            }
            
            System.out.println("=== Payment Notification Sent Successfully ===");
        } catch (Exception e) {
            System.err.println("Error sending payment notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void markPaymentAsCompleted(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        // Get payment info
        PaymentInfoResponse paymentInfo = getPaymentInfo(auctionId);
        
        if (paymentInfo.getRemainingAmount() <= 0) {
            throw new RuntimeException("Phiên đấu giá này đã thanh toán đủ");
        }
        
        // Get winner
        User winner = userRepository.findById(auction.getWinnerId())
                .orElseThrow(() -> new RuntimeException("Winner not found"));
        
        // Create a manual payment record for the remaining amount
        Payment manualPayment = new Payment();
        manualPayment.setAuction(auction);
        manualPayment.setUser(winner);
        manualPayment.setAmount(paymentInfo.getRemainingAmount());
        manualPayment.setOrderId("MANUAL_" + auctionId + "_" + System.currentTimeMillis());
        manualPayment.setRequestId("ADMIN_MANUAL");
        manualPayment.setTransId("ADMIN_COMPLETED");
        manualPayment.setStatus("SUCCESS");
        manualPayment.setOrderInfo("Admin đánh dấu hoàn thành thanh toán");
        manualPayment.setPaymentTime(LocalDateTime.now());
        
        paymentRepository.save(manualPayment);
        
        // Send notification to chat
        sendPaymentNotificationToChat(auctionId, paymentInfo.getRemainingAmount(), "SUCCESS");
    }
}
