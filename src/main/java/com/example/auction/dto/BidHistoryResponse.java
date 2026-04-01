package com.example.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BidHistoryResponse {
    private Long bidId;
    private Long auctionId;
    private Long userId;
    private String username;
    private String displayName; // Display name for UI
    private String avatarURL;
    private BigDecimal bidAmount;
    private LocalDateTime bidTime;
    private String bidType;
    private Boolean isWinning;
}
