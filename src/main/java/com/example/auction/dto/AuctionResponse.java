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
public class AuctionResponse {
    private Long auctionId;
    private Long userId;
    private String auctionName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String productName;
    private String productDescription;
    private BigDecimal startPrice;
    private BigDecimal currentPrice; // Current highest bid
    private BigDecimal endPrice;
    private String status;
    private Long winnerId;
    private Long highestBidderId; // Current highest bidder
    private List<String> imageUrls;
    private Integer totalBids;
}
