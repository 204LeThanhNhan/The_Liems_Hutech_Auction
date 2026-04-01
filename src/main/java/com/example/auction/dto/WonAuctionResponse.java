package com.example.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WonAuctionResponse {
    private Long auctionId;
    private String auctionName;
    private String productName;
    private BigDecimal winningPrice;
    private LocalDateTime endTime;
    private String status; // PENDING, IN_PROGRESS, COMPLETED
    private Long unreadCount;
    
    // Seller info
    private Long sellerId;
    private String sellerName;
    private String sellerAvatar;
    
    // Product images
    private List<String> imageUrls;
}
