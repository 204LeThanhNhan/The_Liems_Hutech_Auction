package com.example.auction.controller;

import com.example.auction.dto.CountdownStatusResponse;
import com.example.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CountdownWebSocketController {
    
    private final AuctionService auctionService;
    
    /**
     * Handle client requests for countdown status
     */
    @MessageMapping("/auction/{auctionId}/countdown/status")
    @SendTo("/topic/auction/{auctionId}/countdown")
    public CountdownStatusResponse getCountdownStatus(@DestinationVariable Long auctionId) {
        try {
            log.info("WebSocket request for countdown status: auction {}", auctionId);
            return auctionService.getCountdownStatus(auctionId);
        } catch (Exception e) {
            log.error("Error getting countdown status for auction {}: {}", auctionId, e.getMessage());
            
            // Return error response
            CountdownStatusResponse errorResponse = new CountdownStatusResponse();
            errorResponse.setAuctionId(auctionId);
            errorResponse.setCountdownStatus("ERROR");
            errorResponse.setMessage("Lỗi khi lấy thông tin countdown: " + e.getMessage());
            return errorResponse;
        }
    }
}