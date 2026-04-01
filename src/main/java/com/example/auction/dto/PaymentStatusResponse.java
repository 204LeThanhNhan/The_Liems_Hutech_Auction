package com.example.auction.dto;

public class PaymentStatusResponse {
    private String status; // SUCCESS, PENDING, FAILED
    private String message;
    private Long amount;
    
    public PaymentStatusResponse() {}
    
    public PaymentStatusResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
}
