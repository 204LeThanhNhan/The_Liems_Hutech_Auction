package com.example.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WinnerChatRequest {
    private Long auctionId;
    private Long senderId;
    private String message;
}
