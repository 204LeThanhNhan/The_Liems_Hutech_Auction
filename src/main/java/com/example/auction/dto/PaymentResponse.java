package com.example.auction.dto;

public class PaymentResponse {
    private String orderId;
    private String qrCodeUrl;
    private String payUrl;
    private String deeplink;
    private String message;
    
    public PaymentResponse() {}
    
    public PaymentResponse(String orderId, String qrCodeUrl) {
        this.orderId = orderId;
        this.qrCodeUrl = qrCodeUrl;
    }
    
    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getQrCodeUrl() {
        return qrCodeUrl;
    }
    
    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }
    
    public String getPayUrl() {
        return payUrl;
    }
    
    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }
    
    public String getDeeplink() {
        return deeplink;
    }
    
    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
