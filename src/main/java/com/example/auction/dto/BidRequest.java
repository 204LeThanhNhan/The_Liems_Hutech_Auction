package com.example.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BidRequest {
    private Long auctionId;
    private Long userId;
    private BigDecimal amount;
    private String type; // 'quick' or 'custom'
}
