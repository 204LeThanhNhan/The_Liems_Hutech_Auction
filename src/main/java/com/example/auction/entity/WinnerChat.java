package com.example.auction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "winner_chat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WinnerChat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ChatID")
    private Long chatId;
    
    @ManyToOne
    @JoinColumn(name = "AuctionID", nullable = false)
    private Auction auction;
    
    @ManyToOne
    @JoinColumn(name = "WinnerID", nullable = false)
    private User winner;
    
    @ManyToOne
    @JoinColumn(name = "SellerID", nullable = false)
    private User seller;
    
    @Column(name = "SenderID", nullable = false)
    private Long senderId;
    
    @Column(name = "Message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Column(name = "MessageTime", nullable = false)
    private LocalDateTime messageTime;
    
    @Column(name = "IsRead")
    private Boolean isRead = false;
    
    @Column(name = "Status", length = 20)
    private String status = "PENDING"; // PENDING, IN_PROGRESS, COMPLETED
}
