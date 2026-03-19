package com.example.auction.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateAuctionRequest {
    private Long userId;
    private String auctionName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String productName;
    private String productDescription;
    private BigDecimal startPrice;
    private List<String> imageUrls;
    private Boolean openNow; // true = mở ngay (30s countdown)
}
