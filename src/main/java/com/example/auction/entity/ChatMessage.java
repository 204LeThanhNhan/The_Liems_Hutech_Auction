package com.example.auction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "message", nullable = false, length = 1000)
    private String message;
    
    @Column(name = "message_time", nullable = false)
    private LocalDateTime messageTime;
    
    @PrePersist
    protected void onCreate() {
        messageTime = LocalDateTime.now();
    }
    
    // Helper method to get auctionId
    public Long getAuctionId() {
        return auction != null ? auction.getAuctionId() : null;
    }
}
