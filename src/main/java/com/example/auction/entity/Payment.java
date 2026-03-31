package com.example.auction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentID")
    private Long paymentId;
    
    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "AuctionID", nullable = false)
    private Auction auction;
    
    @Column(name = "ProductID")
    private Long productId;
    
    @Column(name = "Amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "PayTime")
    private LocalDateTime payTime;
    
    @Column(name = "PayStatus", nullable = false, length = 20)
    private String payStatus; // PENDING, COMPLETED, FAILED
}
