package com.example.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountdownStatusResponse {
    private Long auctionId;
    private String countdownStatus; // WAITING, ROUND_1, ROUND_2, ROUND_3, SOLD_TEMP, SOLD_FINAL
    private Integer countdownRound;
    private LocalDateTime countdownStartTime;
    private Integer secondsRemaining;
    private Integer extensionCount;
    private LocalDateTime endTime;
    private LocalDateTime originalEndTime;
    private String message;
    
    public static CountdownStatusResponse waiting(Long auctionId, LocalDateTime endTime) {
        CountdownStatusResponse response = new CountdownStatusResponse();
        response.setAuctionId(auctionId);
        response.setCountdownStatus("WAITING");
        response.setCountdownRound(0);
        response.setEndTime(endTime);
        response.setMessage("Chưa có ai đặt giá");
        return response;
    }
    
    public static CountdownStatusResponse active(Long auctionId, String status, Integer round, 
                                                  LocalDateTime startTime, Integer secondsRemaining,
                                                  Integer extensionCount, LocalDateTime endTime, 
                                                  LocalDateTime originalEndTime) {
        CountdownStatusResponse response = new CountdownStatusResponse();
        response.setAuctionId(auctionId);
        response.setCountdownStatus(status);
        response.setCountdownRound(round);
        response.setCountdownStartTime(startTime);
        response.setSecondsRemaining(secondsRemaining);
        response.setExtensionCount(extensionCount);
        response.setEndTime(endTime);
        response.setOriginalEndTime(originalEndTime);
        
        switch (status) {
            case "ROUND_1":
                response.setMessage("LẦN 1...");
                break;
            case "ROUND_2":
                response.setMessage("LẦN 2...");
                break;
            case "ROUND_3":
                response.setMessage("LẦN 3...");
                break;
            case "SOLD_TEMP":
                response.setMessage("SOLD tạm thời - Vẫn có thể đặt giá");
                break;
            case "SOLD_FINAL":
                response.setMessage("🔨 SOLD! Phiên đấu giá kết thúc");
                break;
        }
        
        return response;
    }
}
