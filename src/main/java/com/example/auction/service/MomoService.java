package com.example.auction.service;

import com.example.auction.dto.PaymentRequest;
import com.example.auction.dto.PaymentResponse;
import com.example.auction.entity.Auction;
import com.example.auction.entity.Payment;
import com.example.auction.entity.User;
import com.example.auction.repository.AuctionRepository;
import com.example.auction.repository.PaymentRepository;
import com.example.auction.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MomoService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private AuctionRepository auctionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // MOMO Configuration
    private static final String PARTNER_CODE = "MOMO";
    private static final String ACCESS_KEY = "F8BBA842ECF85";
    private static final String SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    private static final String MOMO_API_URL = "https://test-payment.momo.vn/v2/gateway/api/create";
    // Production URLs
    private static final String RETURN_URL = "https://hutech-auction.click/payment/callback";
    private static final String NOTIFY_URL = "https://hutech-auction.click/api/payment/momo/notify";
    private static final String REQUEST_TYPE = "captureWallet";
    
    public PaymentResponse createPayment(PaymentRequest request) throws Exception {
        // Validate
        Auction auction = auctionRepository.findById(request.getAuctionId())
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user is winner
        if (!auction.getWinnerId().equals(user.getUserId())) {
            throw new RuntimeException("User is not the winner of this auction");
        }
        
        // Validate amount
        if (request.getAmount() > 50000000) {
            throw new RuntimeException("Amount exceeds maximum limit of 50,000,000 VND");
        }
        
        // Calculate remaining amount
        Long paidAmount = paymentRepository.getTotalPaidAmount(request.getAuctionId());
        if (paidAmount == null) {
            paidAmount = 0L;
        }
        
        Long totalAmount = auction.getEndPrice().longValue();
        Long remainingAmount = totalAmount - paidAmount;
        
        if (request.getAmount() > remainingAmount) {
            throw new RuntimeException("Amount exceeds remaining amount");
        }
        
        // Generate unique IDs
        String orderId = "AUC" + request.getAuctionId() + "_" + System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        
        // Create payment record
        Payment payment = new Payment();
        payment.setAuction(auction);
        payment.setUser(user);
        payment.setAmount(request.getAmount());
        payment.setOrderId(orderId);
        payment.setRequestId(requestId);
        payment.setOrderInfo(request.getOrderInfo());
        payment.setStatus("PENDING");
        
        paymentRepository.save(payment);
        
        // Create MOMO payment request
        String extraData = "";
        String rawSignature = "accessKey=" + ACCESS_KEY +
                "&amount=" + request.getAmount() +
                "&extraData=" + extraData +
                "&ipnUrl=" + NOTIFY_URL +
                "&orderId=" + orderId +
                "&orderInfo=" + request.getOrderInfo() +
                "&partnerCode=" + PARTNER_CODE +
                "&redirectUrl=" + RETURN_URL +
                "&requestId=" + requestId +
                "&requestType=" + REQUEST_TYPE;
        
        String signature = hmacSHA256(rawSignature, SECRET_KEY);
        
        // Build request body for MOMO API
        Map<String, Object> momoRequest = new HashMap<>();
        momoRequest.put("partnerCode", PARTNER_CODE);
        momoRequest.put("accessKey", ACCESS_KEY);
        momoRequest.put("requestId", requestId);
        momoRequest.put("amount", request.getAmount().toString());
        momoRequest.put("orderId", orderId);
        momoRequest.put("orderInfo", request.getOrderInfo());
        momoRequest.put("redirectUrl", RETURN_URL);
        momoRequest.put("ipnUrl", NOTIFY_URL);
        momoRequest.put("extraData", extraData);
        momoRequest.put("requestType", REQUEST_TYPE);
        momoRequest.put("signature", signature);
        momoRequest.put("lang", "vi");
        
        // Call MOMO API
        String jsonRequest = objectMapper.writeValueAsString(momoRequest);
        
        System.out.println("=== MOMO Payment Request ===");
        System.out.println("URL: " + MOMO_API_URL);
        System.out.println("Request: " + jsonRequest);
        System.out.println("Signature: " + signature);
        
        URL url = new URL(MOMO_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        System.out.println("MOMO Response Code: " + responseCode);
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Read response
            java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseBody = response.toString();
            System.out.println("MOMO Response: " + responseBody);
            
            // Parse MOMO response
            @SuppressWarnings("unchecked")
            Map<String, Object> momoResponse = objectMapper.readValue(responseBody, Map.class);
            
            // Check result code from MOMO
            Integer resultCode = (Integer) momoResponse.get("resultCode");
            if (resultCode != null && resultCode != 0) {
                String message = (String) momoResponse.get("message");
                throw new RuntimeException("MOMO Error: " + message + " (Code: " + resultCode + ")");
            }
            
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setOrderId(orderId);
            
            // MOMO returns payUrl (for web redirect)
            if (momoResponse.containsKey("payUrl")) {
                String payUrl = (String) momoResponse.get("payUrl");
                paymentResponse.setPayUrl(payUrl);     // Set to payUrl field
                paymentResponse.setQrCodeUrl(payUrl);  // For backward compatibility
                paymentResponse.setDeeplink(payUrl);   // Also set to deeplink
                System.out.println("Payment URL: " + payUrl);
            }
            
            paymentResponse.setMessage("Payment created successfully");
            
            return paymentResponse;
        } else {
            // Read error response
            java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorResponse = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                errorResponse.append(responseLine.trim());
            }
            
            System.out.println("MOMO Error Response: " + errorResponse.toString());
            throw new RuntimeException("Failed to create MOMO payment. Response code: " + responseCode + ", Error: " + errorResponse.toString());
        }
    }
    
    public void handlePaymentNotification(String orderId, String transId, int resultCode) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (resultCode == 0) {
            payment.setStatus("SUCCESS");
            payment.setTransId(transId);
            payment.setPaymentTime(java.time.LocalDateTime.now());
        } else {
            payment.setStatus("FAILED");
        }
        
        paymentRepository.save(payment);
        
        // Check if payment is completed
        if ("SUCCESS".equals(payment.getStatus())) {
            checkAndCompletePayment(payment.getAuction().getAuctionId());
        }
    }
    
    private void checkAndCompletePayment(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        Long paidAmount = paymentRepository.getTotalPaidAmount(auctionId);
        if (paidAmount == null) {
            paidAmount = 0L;
        }
        
        Long totalAmount = auction.getEndPrice().longValue();
        
        if (paidAmount >= totalAmount) {
            // Mark auction as payment completed
            // You may need to add a field to Auction entity
            System.out.println("Payment completed for auction: " + auctionId);
        }
    }
    
    private String hmacSHA256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
