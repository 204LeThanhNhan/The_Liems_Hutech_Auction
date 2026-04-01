package com.example.auction.dto;

public class PaymentInfoResponse {
    private Long paidAmount;
    private Long remainingAmount;
    private Long totalAmount;
    private boolean paymentCompleted;
    
    public PaymentInfoResponse() {}
    
    public PaymentInfoResponse(Long paidAmount, Long remainingAmount, Long totalAmount, boolean paymentCompleted) {
        this.paidAmount = paidAmount;
        this.remainingAmount = remainingAmount;
        this.totalAmount = totalAmount;
        this.paymentCompleted = paymentCompleted;
    }
    
    // Getters and Setters
    public Long getPaidAmount() {
        return paidAmount;
    }
    
    public void setPaidAmount(Long paidAmount) {
        this.paidAmount = paidAmount;
    }
    
    public Long getRemainingAmount() {
        return remainingAmount;
    }
    
    public void setRemainingAmount(Long remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
    
    public Long getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public boolean isPaymentCompleted() {
        return paymentCompleted;
    }
    
    public void setPaymentCompleted(boolean paymentCompleted) {
        this.paymentCompleted = paymentCompleted;
    }
}
