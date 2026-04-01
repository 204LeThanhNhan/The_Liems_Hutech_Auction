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
    
    // Countdown mechanism fields
    @Column(name = "CountdownStartTime")
    private LocalDateTime countdownStartTime;
    
    @Column(name = "CountdownRound")
    private Integer countdownRound = 0; // 0=not started, 1=first, 2=second, 3=third
    
    @Column(name = "CountdownStatus")
    private String countdownStatus; // WAITING, ROUND_1, ROUND_2, ROUND_3, SOLD_TEMP, SOLD_FINAL
    
    @Column(name = "ExtensionCount")
    private Integer extensionCount = 0; // Track how many times extended
    
    @Column(name = "OriginalEndTime")
    private LocalDateTime originalEndTime; // Store original end time
    
    // Chỉ cascade cho Images (vì images thuộc về auction)
    // orphanRemoval = true để xóa images khi xóa auction
    @OneToMany(mappedBy = "auction", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Image> images;
    
    // Không cascade cho Bids và Payments để giữ lịch sử
    @OneToMany(mappedBy = "auction")
    private List<Bid> bids;
    
    @OneToMany(mappedBy = "auction")
    private List<Payment> payments;
    
    // ChatMessage uses auctionId directly, no bidirectional relationship needed
}
