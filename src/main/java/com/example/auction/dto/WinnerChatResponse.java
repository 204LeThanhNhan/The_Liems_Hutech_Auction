package com.example.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WinnerChatResponse {
    private Long chatId;
    private Long auctionId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String message;
    private LocalDateTime messageTime;
    private Boolean isRead;
}
