package com.example.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BidResponse {
    private boolean success;
    private String message;
    private BidData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BidData {
        private Long bidId;
        private Long auctionId;
        private Long userId;
        private String username;
        private String avatarURL;
        private BigDecimal bidAmount;
        private BigDecimal currentPrice;
        private Long highestBidderId;
        private String highestBidderName;
        private Integer totalBids;
        private LocalDateTime bidTime;
        private String bidType;
    }
    
    public static BidResponse success(BidData data) {
        return new BidResponse(true, "Đấu giá thành công", data);
    }
    
    public static BidResponse error(String message) {
        return new BidResponse(false, message, null);
    }
}
