package com.example.auction.controller;

import com.example.auction.dto.BidHistoryResponse;
import com.example.auction.dto.BidRequest;
import com.example.auction.dto.BidResponse;
import com.example.auction.dto.ChatMessageRequest;
import com.example.auction.dto.ChatMessageResponse;
import com.example.auction.entity.Auction;
import com.example.auction.entity.Bid;
import com.example.auction.service.BiddingService;
import com.example.auction.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class BiddingController {
    
    private final BiddingService biddingService;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * WebSocket endpoint: Place a bid
     * Client sends to: /app/auction/{auctionId}/bid
     * Server broadcasts to: /topic/auction/{auctionId}
     */
    @MessageMapping("/auction/{auctionId}/bid")
    public void placeBid(@DestinationVariable Long auctionId, BidRequest request) {
        log.info("Received bid request for auction {}: {}", auctionId, request);
        
        request.setAuctionId(auctionId);
        BidResponse response = biddingService.placeBid(request);
        
        // Broadcast to all clients in the auction room
        messagingTemplate.convertAndSend("/topic/auction/" + auctionId, response);
        
        log.info("Broadcasted bid response to auction {}: {}", auctionId, response.isSuccess());
    }
    
    /**
     * REST endpoint: Get auction details
     */
    @GetMapping("/api/auctions/{auctionId}/details")
    public ResponseEntity<Auction> getAuctionDetails(@PathVariable Long auctionId) {
        try {
            Auction auction = biddingService.getAuctionDetails(auctionId);
            return ResponseEntity.ok(auction);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * REST endpoint: Get bid history
     */
    @GetMapping("/api/auctions/{auctionId}/bids")
    public ResponseEntity<List<BidHistoryResponse>> getBidHistory(@PathVariable Long auctionId) {
        List<BidHistoryResponse> bids = biddingService.getBidHistoryDTO(auctionId);
        return ResponseEntity.ok(bids);
    }
    
    /**
     * WebSocket endpoint: Send chat message
     */
    @MessageMapping("/auction/{auctionId}/chat")
    public void sendChatMessage(@DestinationVariable Long auctionId, ChatMessageRequest chatRequest) {
        try {
            log.info("Received chat message for auction {}: {}", auctionId, chatRequest.getMessage());
            
            chatRequest.setAuctionId(auctionId);
            ChatMessageResponse response = chatService.sendMessage(chatRequest);
            
            // Broadcast to all clients in the auction's chat
            messagingTemplate.convertAndSend("/topic/auction/" + auctionId + "/chat", response);
            
            log.info("Broadcasted chat message to auction {}: {}", auctionId, response.getUsername());
            
        } catch (Exception e) {
            log.error("Error processing chat message for auction {}: ", auctionId, e);
        }
    }
    
    /**
     * REST endpoint: Get recent chat messages
     */
    @GetMapping("/api/auctions/{auctionId}/chat/recent")
    public ResponseEntity<List<ChatMessageResponse>> getRecentChatMessages(
            @PathVariable Long auctionId,
            @RequestParam(defaultValue = "50") int limit) {
        List<ChatMessageResponse> messages = chatService.getRecentMessages(auctionId, limit);
        return ResponseEntity.ok(messages);
    }
}
