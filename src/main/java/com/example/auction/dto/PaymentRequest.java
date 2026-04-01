package com.example.auction.dto;

public class PaymentRequest {
    private Long auctionId;
    private Long userId;
    private Long amount;
    private String orderInfo;
    
    // Getters and Setters
    public Long getAuctionId() {
        return auctionId;
    }
    
    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public String getOrderInfo() {
        return orderInfo;
    }
    
    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }
}
