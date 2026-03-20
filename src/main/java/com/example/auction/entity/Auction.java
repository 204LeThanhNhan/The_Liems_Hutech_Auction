package com.example.auction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "auction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AuctionID")
    private Long auctionId;
    
    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;
    
    @Column(name = "ProductID")
    private Long productId;
    
    @Column(name = "AuctionName", nullable = false, length = 200)
    private String auctionName;
    
    @Column(name = "StartPrice", nullable = false, precision = 15, scale = 2)
    private BigDecimal startPrice;
    
    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "EndTime")
    private LocalDateTime endTime;
    
    @Column(name = "WinnerID")
    private Long winnerId;
    
    @Column(name = "Status", nullable = false, length = 20)
    private String status; // PENDING, ACTIVE, ENDED, CANCELLED
    
    @Column(name = "EndPrice", precision = 15, scale = 2)
    private BigDecimal endPrice;
    
    @Column(name = "ProductName", nullable = false, length = 200)
    private String productName;
    
    @Column(name = "ProductDescription", columnDefinition = "TEXT")
    private String productDescription;
    
    @Column(name = "CurrentPrice", precision = 15, scale = 2)
    private BigDecimal currentPrice;
    
    @Column(name = "TotalBids")
    private Integer totalBids = 0;
    
    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL)
    private List<Image> images;
    
    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL)
    private List<Bid> bids;
    
    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL)
    private List<Payment> payments;
    
    // ChatMessage uses auctionId directly, no bidirectional relationship needed
}

