package com.example.auction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bid {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bidId;
    
    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal bidAmount;
    
    @Column(nullable = false)
    private LocalDateTime bidTime;
    
    @Column(length = 20)
    private String bidType; // 'quick' or 'custom'
    
    @Column(nullable = false)
    private Boolean isWinning = false;
    
    @PrePersist
    protected void onCreate() {
        bidTime = LocalDateTime.now();
    }
}
