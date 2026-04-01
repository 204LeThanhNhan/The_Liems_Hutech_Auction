package com.example.auction.controller;

import com.example.auction.dto.WinnerChatRequest;
import com.example.auction.dto.WinnerChatResponse;
import com.example.auction.dto.WonAuctionResponse;
import com.example.auction.service.WinnerChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "https://hutech-auction.click"})
public class WinnerChatController {
    
    private final WinnerChatService winnerChatService;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Get all won auctions for a user
     */
    @GetMapping("/api/my-wins/{userId}")
    public ResponseEntity<List<WonAuctionResponse>> getWonAuctions(@PathVariable Long userId) {
        try {
            List<WonAuctionResponse> wonAuctions = winnerChatService.getWonAuctions(userId);
            return ResponseEntity.ok(wonAuctions);
        } catch (Exception e) {
            log.error("Error getting won auctions: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get chat messages for an auction
     */
    @GetMapping("/api/winner-chat/{auctionId}/messages")
    public ResponseEntity<?> getChatMessages(
            @PathVariable Long auctionId,
            @RequestParam Long userId) {
        try {
            List<WinnerChatResponse> messages = winnerChatService.getChatMessages(auctionId, userId);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
    
    /**
     * WebSocket: Send message in winner chat
     */
    @MessageMapping("/winner-chat/{auctionId}/send")
    public void sendMessage(@DestinationVariable Long auctionId, WinnerChatRequest request) {
        try {
            log.info("Received winner chat message for auction {}", auctionId);
            
            request.setAuctionId(auctionId);
            WinnerChatResponse response = winnerChatService.sendMessage(request);
            
            // Broadcast to both winner and seller
            messagingTemplate.convertAndSend("/topic/winner-chat/" + auctionId, response);
            
            log.info("Broadcasted winner chat message to auction {}", auctionId);
        } catch (Exception e) {
            log.error("Error sending winner chat message: ", e);
        }
    }
    
    /**
     * Mark messages as read
     */
    @PostMapping("/api/winner-chat/{auctionId}/mark-read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long auctionId,
            @RequestParam Long userId) {
        try {
            winnerChatService.markAsRead(auctionId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Get total unread count
     */
    @GetMapping("/api/my-wins/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        Long count = winnerChatService.getTotalUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
    
    /**
     * Mark auction as completed
     */
    @PostMapping("/api/winner-chat/{auctionId}/complete")
    public ResponseEntity<?> markAsCompleted(
            @PathVariable Long auctionId,
            @RequestParam Long userId) {
        try {
            winnerChatService.markAsCompleted(auctionId, userId);
            return ResponseEntity.ok("Marked as completed");
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}
