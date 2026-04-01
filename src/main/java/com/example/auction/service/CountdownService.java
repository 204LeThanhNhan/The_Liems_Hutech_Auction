package com.example.auction.service;

import com.example.auction.entity.Auction;
import com.example.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class CountdownService {
    
    private final AuctionRepository auctionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    // In-memory cache for auction IDs with active countdowns
    private final Set<Long> activeCountdownIds = ConcurrentHashMap.newKeySet();
    
    // Cache for auction data to reduce DB queries
    private final Map<Long, AuctionCountdownData> countdownCache = new ConcurrentHashMap<>();
    
    // Countdown durations
    private static final int ROUND_1_SECONDS = 30;
    private static final int ROUND_2_SECONDS = 20;
    private static final int ROUND_3_SECONDS = 10;
    
    // Simplified: 2 cycles (6 rounds total) to SOLD_FINAL
    private static final int TOTAL_CYCLES = 2; // Must complete 2 full cycles
    
    /**
     * Start countdown when first bid is placed
     */
    @Transactional
    public void startCountdown(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        auction.setCountdownStartTime(LocalDateTime.now());
        auction.setCountdownRound(1);
        auction.setCountdownStatus("ROUND_1");
        auction.setExtensionCount(0); // Use this field to track cycle number (0 = cycle 1)
        
        auctionRepository.save(auction);
        
        // Add to active countdowns cache
        activeCountdownIds.add(auctionId);
        countdownCache.put(auctionId, new AuctionCountdownData(auction));
        
        log.info("Countdown started for auction {} - CYCLE 1", auctionId);
    }
    
    /**
     * Reset countdown when new bid is placed
     */
    @Transactional
    public void resetCountdown(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        auction.setCountdownStartTime(LocalDateTime.now());
        auction.setCountdownRound(1);
        auction.setCountdownStatus("ROUND_1");
        auction.setExtensionCount(0); // Reset to cycle 1
        
        auctionRepository.save(auction);
        
        // Update cache
        activeCountdownIds.add(auctionId);
        countdownCache.put(auctionId, new AuctionCountdownData(auction));
        
        log.info("Countdown reset for auction {} - Back to CYCLE 1", auctionId);
    }
    
    /**
     * Check countdown status every 2 seconds (optimized)
     * Only processes auctions with active countdowns
     */
    @Scheduled(fixedRate = 2000) // Run every 2 seconds instead of 1
    @Transactional
    public void checkCountdowns() {
        // Only process auctions that have active countdowns
        if (activeCountdownIds.isEmpty()) {
            return; // No countdowns to process
        }
        
        for (Long auctionId : activeCountdownIds) {
            try {
                AuctionCountdownData data = countdownCache.get(auctionId);
                if (data == null) {
                    // Load from DB if not in cache
                    Auction auction = auctionRepository.findById(auctionId).orElse(null);
                    if (auction == null) {
                        activeCountdownIds.remove(auctionId);
                        countdownCache.remove(auctionId);
                        continue;
                    }
                    data = new AuctionCountdownData(auction);
                    countdownCache.put(auctionId, data);
                }
                
                processCountdown(data);
            } catch (Exception e) {
                log.error("Error processing countdown for auction {}: {}", auctionId, e.getMessage());
            }
        }
    }
    
    private void processCountdown(AuctionCountdownData data) {
        LocalDateTime now = LocalDateTime.now();
        long secondsElapsed = Duration.between(data.countdownStartTime, now).getSeconds();
        
        String currentStatus = data.countdownStatus;
        
        switch (currentStatus) {
            case "ROUND_1":
                if (secondsElapsed >= ROUND_1_SECONDS) {
                    moveToRound2(data);
                } else {
                    broadcastCountdown(data.auctionId, ROUND_1_SECONDS - (int)secondsElapsed, 1);
                }
                break;
                
            case "ROUND_2":
                if (secondsElapsed >= ROUND_2_SECONDS) {
                    moveToRound3(data);
                } else {
                    broadcastCountdown(data.auctionId, ROUND_2_SECONDS - (int)secondsElapsed, 2);
                }
                break;
                
            case "ROUND_3":
                if (secondsElapsed >= ROUND_3_SECONDS) {
                    handleSold(data);
                } else {
                    broadcastCountdown(data.auctionId, ROUND_3_SECONDS - (int)secondsElapsed, 3);
                }
                break;
                
            case "SOLD_FINAL":
                // Auction is finished, remove from active countdowns
                activeCountdownIds.remove(data.auctionId);
                countdownCache.remove(data.auctionId);
                break;
        }
    }
    
    @Transactional
    private void moveToRound2(AuctionCountdownData data) {
        data.countdownStartTime = LocalDateTime.now();
        data.countdownRound = 2;
        data.countdownStatus = "ROUND_2";
        
        // Update DB
        Auction auction = auctionRepository.findById(data.auctionId).orElse(null);
        if (auction != null) {
            auction.setCountdownStartTime(data.countdownStartTime);
            auction.setCountdownRound(data.countdownRound);
            auction.setCountdownStatus(data.countdownStatus);
            auctionRepository.save(auction);
        }
        
        log.info("Auction {} moved to ROUND 2", data.auctionId);
        broadcastCountdown(data.auctionId, ROUND_2_SECONDS, 2);
    }
    
    @Transactional
    private void moveToRound3(AuctionCountdownData data) {
        data.countdownStartTime = LocalDateTime.now();
        data.countdownRound = 3;
        data.countdownStatus = "ROUND_3";
        
        // Update DB
        Auction auction = auctionRepository.findById(data.auctionId).orElse(null);
        if (auction != null) {
            auction.setCountdownStartTime(data.countdownStartTime);
            auction.setCountdownRound(data.countdownRound);
            auction.setCountdownStatus(data.countdownStatus);
            auctionRepository.save(auction);
        }
        
        log.info("Auction {} moved to ROUND 3", data.auctionId);
        broadcastCountdown(data.auctionId, ROUND_3_SECONDS, 3);
    }
    
    @Transactional
    private void handleSold(AuctionCountdownData data) {
        Auction auction = auctionRepository.findById(data.auctionId).orElse(null);
        if (auction == null) return;
        
        int currentCycle = auction.getExtensionCount() + 1; // extensionCount: 0=cycle1, 1=cycle2
        
        log.info("Auction {} completed cycle {} round 3", data.auctionId, currentCycle);
        
        // Check if completed 2 full cycles
        if (currentCycle >= TOTAL_CYCLES) {
            // Completed 2 cycles (6 rounds total) → SOLD_FINAL
            finalizeAuction(data, auction);
        } else {
            // Start next cycle (cycle 2)
            startNextCycle(data, auction);
        }
    }
    
    @Transactional
    private void startNextCycle(AuctionCountdownData data, Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        
        auction.setCountdownStartTime(now);
        auction.setCountdownRound(1);
        auction.setCountdownStatus("ROUND_1");
        auction.setExtensionCount(auction.getExtensionCount() + 1); // Increment cycle
        
        auctionRepository.save(auction);
        
        // Update cache
        data.countdownStartTime = now;
        data.countdownRound = 1;
        data.countdownStatus = "ROUND_1";
        data.extensionCount = auction.getExtensionCount();
        
        int cycleNumber = auction.getExtensionCount() + 1;
        log.info("Auction {} - Starting CYCLE {} (Round 1)", data.auctionId, cycleNumber);
        
        // Broadcast cycle change
        broadcastCycleChange(data.auctionId, cycleNumber);
    }
    
    @Transactional
    private void finalizeAuction(AuctionCountdownData data, Auction auction) {
        // Set winner from highest bid
        if (auction.getBids() != null && !auction.getBids().isEmpty()) {
            var highestBid = auction.getBids().stream()
                .max((b1, b2) -> b1.getBidAmount().compareTo(b2.getBidAmount()))
                .orElse(null);
            
            if (highestBid != null) {
                auction.setWinnerId(highestBid.getUser().getUserId());
                auction.setEndPrice(highestBid.getBidAmount());
            }
        }
        
        auction.setCountdownStatus("SOLD_FINAL");
        auction.setStatus("ENDED");
        auctionRepository.save(auction);
        
        // Remove from active countdowns
        activeCountdownIds.remove(data.auctionId);
        countdownCache.remove(data.auctionId);
        
        log.info("Auction {} - FINAL SOLD to user {}", data.auctionId, auction.getWinnerId());
        broadcastSoldFinal(data.auctionId);
    }
    
    private void broadcastCountdown(Long auctionId, int secondsRemaining, int round) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "COUNTDOWN");
        message.put("auctionId", auctionId);
        message.put("round", round);
        message.put("secondsRemaining", secondsRemaining);
        
        messagingTemplate.convertAndSend(
            "/topic/auction/" + auctionId + "/countdown", 
            message
        );
    }
    
    private void broadcastCycleChange(Long auctionId, int cycleNumber) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "CYCLE_CHANGE");
        message.put("auctionId", auctionId);
        message.put("cycleNumber", cycleNumber);
        message.put("message", String.format("Vòng %d - Lần 1", cycleNumber));
        
        messagingTemplate.convertAndSend(
            "/topic/auction/" + auctionId + "/countdown", 
            message
        );
    }
    
    private void broadcastSoldFinal(Long auctionId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "SOLD_FINAL");
        message.put("auctionId", auctionId);
        message.put("message", "🔨 SOLD! Phiên đấu giá kết thúc");
        
        messagingTemplate.convertAndSend(
            "/topic/auction/" + auctionId + "/countdown", 
            message
        );
    }
    
    /**
     * Inner class to cache auction countdown data
     */
    private static class AuctionCountdownData {
        Long auctionId;
        LocalDateTime countdownStartTime;
        Integer countdownRound;
        String countdownStatus;
        LocalDateTime endTime;
        Integer extensionCount;
        
        AuctionCountdownData(Auction auction) {
            this.auctionId = auction.getAuctionId();
            this.countdownStartTime = auction.getCountdownStartTime();
            this.countdownRound = auction.getCountdownRound();
            this.countdownStatus = auction.getCountdownStatus();
            this.endTime = auction.getEndTime();
            this.extensionCount = auction.getExtensionCount();
        }
    }
}
