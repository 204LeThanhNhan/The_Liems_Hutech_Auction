package com.example.auction.service;

import com.example.auction.entity.Auction;
import com.example.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimerService {
    
    private final AuctionRepository auctionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    // Cache for auctions without countdown
    private final Set<Long> auctionsWithoutCountdown = ConcurrentHashMap.newKeySet();
    
    /**
     * Broadcast time remaining until auction end for auctions without countdown
     * Optimized: Only query DB once per 10 seconds
     */
    @Scheduled(fixedRate = 10000) // Every 10 seconds instead of 5
    public void broadcastTimeRemaining() {
        // Query only auctions that are ACTIVE and don't have countdown started
        List<Auction> activeAuctions = auctionRepository.findByStatusAndCountdownStartTimeIsNull("ACTIVE");
        
        for (Auction auction : activeAuctions) {
            if (auction.getEndTime() != null) {
                broadcastTimeUntilEnd(auction);
                auctionsWithoutCountdown.add(auction.getAuctionId());
            }
        }
    }
    
    private void broadcastTimeUntilEnd(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        
        if (auction.getEndTime().isBefore(now)) {
            // Auction should have ended
            auctionsWithoutCountdown.remove(auction.getAuctionId());
            return;
        }
        
        Duration timeRemaining = Duration.between(now, auction.getEndTime());
        long totalMinutes = timeRemaining.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        
        String timeMessage;
        if (hours > 0) {
            timeMessage = String.format("Còn %d giờ %d phút", hours, minutes);
        } else if (minutes > 0) {
            timeMessage = String.format("Còn %d phút", minutes);
        } else {
            long seconds = timeRemaining.getSeconds();
            timeMessage = String.format("Còn %d giây", seconds);
        }
        
        Map<String, Object> message = new HashMap<>();
        message.put("type", "TIME_REMAINING");
        message.put("auctionId", auction.getAuctionId());
        message.put("timeRemaining", timeMessage);
        message.put("totalMinutes", totalMinutes);
        message.put("endTime", auction.getEndTime());
        message.put("message", "Chưa có ai đặt giá");
        
        messagingTemplate.convertAndSend(
            "/topic/auction/" + auction.getAuctionId() + "/timer", 
            message
        );
    }
}