package com.example.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long messageId;
    private Long auctionId;
    private Long userId;
    private String username;
    private String displayName;
    private String avatarURL;
    private String message;
    private LocalDateTime messageTime;
}
