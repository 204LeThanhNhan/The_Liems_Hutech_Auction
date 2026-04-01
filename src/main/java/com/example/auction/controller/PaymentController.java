package com.example.auction.controller;

import com.example.auction.dto.*;
import com.example.auction.service.MomoService;
import com.example.auction.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    @Autowired
    private MomoService momoService;
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * Get payment information for an auction
     */
    @GetMapping("/{auctionId}/info")
    public ResponseEntity<PaymentInfoResponse> getPaymentInfo(@PathVariable Long auctionId) {
        try {
            PaymentInfoResponse response = paymentService.getPaymentInfo(auctionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Create MOMO payment
     */
    @PostMapping("/momo/create")
    public ResponseEntity<?> createMomoPayment(@RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = momoService.createPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    /**
     * Get payment status
     */
    @GetMapping("/momo/status/{orderId}")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(@PathVariable String orderId) {
        try {
            PaymentStatusResponse response = paymentService.getPaymentStatus(orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new PaymentStatusResponse("ERROR", e.getMessage()));
        }
    }
    
    /**
     * MOMO IPN (Instant Payment Notification) callback
     * TEMPORARILY DISABLED - Only works in production, causes duplicate notifications in localhost
     */
    // @PostMapping("/momo/notify")
    // public ResponseEntity<?> momoNotify(@RequestBody Map<String, Object> payload) {
    //     try {
    //         String orderId = (String) payload.get("orderId");
    //         String transId = (String) payload.get("transId");
    //         Integer resultCode = (Integer) payload.get("resultCode");
    //         Long amount = ((Number) payload.get("amount")).longValue();
    //         
    //         // Handle payment notification
    //         momoService.handlePaymentNotification(orderId, transId, resultCode);
    //         
    //         // Send notification to chat
    //         // Extract auctionId from orderId (format: AUC{auctionId}_{timestamp})
    //         String[] parts = orderId.split("_");
    //         if (parts.length > 0) {
    //             String auctionIdStr = parts[0].replace("AUC", "");
    //             Long auctionId = Long.parseLong(auctionIdStr);
    //             
    //             String status = resultCode == 0 ? "SUCCESS" : "FAILED";
    //             paymentService.sendPaymentNotificationToChat(auctionId, amount, status);
    //         }
    //         
    //         return ResponseEntity.ok(Map.of("message", "Notification received"));
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    //     }
    // }
    
    /**
     * Manual callback from frontend (for localhost development where MOMO can't reach IPN)
     */
    @PostMapping("/momo/callback")
    public ResponseEntity<?> momoCallback(@RequestBody Map<String, Object> payload) {
        try {
            String orderId = (String) payload.get("orderId");
            String transId = (String) payload.get("transId");
            Integer resultCode = (Integer) payload.get("resultCode");
            Long amount = payload.get("amount") != null ? ((Number) payload.get("amount")).longValue() : 0L;
            
            System.out.println("=== Manual Payment Callback ===");
            System.out.println("OrderId: " + orderId);
            System.out.println("TransId: " + transId);
            System.out.println("ResultCode: " + resultCode);
            System.out.println("Amount: " + amount);
            
            // Handle payment notification
            momoService.handlePaymentNotification(orderId, transId, resultCode);
            
            // Send notification to chat
            String[] parts = orderId.split("_");
            if (parts.length > 0) {
                String auctionIdStr = parts[0].replace("AUC", "");
                Long auctionId = Long.parseLong(auctionIdStr);
                
                // If amount is 0, get it from payment record
                if (amount == 0) {
                    System.out.println("Amount is 0, fetching from payment record...");
                    // Get payment record to find actual amount
                    PaymentStatusResponse paymentStatus = paymentService.getPaymentStatus(orderId);
                    amount = paymentStatus.getAmount();
                    System.out.println("Fetched amount from payment: " + amount);
                }
                
                String status = resultCode == 0 ? "SUCCESS" : "FAILED";
                System.out.println("Sending payment notification to chat - AuctionId: " + auctionId + ", Amount: " + amount + ", Status: " + status);
                paymentService.sendPaymentNotificationToChat(auctionId, amount, status);
                System.out.println("Payment notification sent successfully");
            }
            
            return ResponseEntity.ok(Map.of("message", "Payment callback processed successfully"));
        } catch (Exception e) {
            System.err.println("Error in payment callback: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    /**
     * Manual payment status update (for testing)
     */
    @PostMapping("/momo/test-success/{orderId}")
    public ResponseEntity<?> testPaymentSuccess(@PathVariable String orderId) {
        try {
            // Simulate successful payment
            momoService.handlePaymentNotification(orderId, "TEST_TRANS_" + System.currentTimeMillis(), 0);
            
            // Extract auctionId and send notification
            String[] parts = orderId.split("_");
            if (parts.length > 0) {
                String auctionIdStr = parts[0].replace("AUC", "");
                Long auctionId = Long.parseLong(auctionIdStr);
                
                PaymentInfoResponse paymentInfo = paymentService.getPaymentInfo(auctionId);
                Long amount = 0L; // You may need to get this from payment record
                
                paymentService.sendPaymentNotificationToChat(auctionId, amount, "SUCCESS");
            }
            
            return ResponseEntity.ok(Map.of("message", "Payment marked as successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    /**
     * Admin marks auction payment as completed
     */
    @PostMapping("/{auctionId}/mark-completed")
    public ResponseEntity<?> markAsCompleted(@PathVariable Long auctionId) {
        try {
            paymentService.markPaymentAsCompleted(auctionId);
            return ResponseEntity.ok(Map.of("message", "Đã đánh dấu hoàn thành thanh toán"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
