package com.example.auction.controller;

import com.example.auction.service.CountdownService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestCountdownController {
    
    private final CountdownService countdownService;
    
    /**
     * Test endpoint to manually start countdown for an auction
     */
    @PostMapping("/auction/{auctionId}/start-countdown")
    public ResponseEntity<?> startCountdown(@PathVariable Long auctionId) {
        try {
            countdownService.startCountdown(auctionId);
            return ResponseEntity.ok("Countdown started for auction " + auctionId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Test endpoint to manually reset countdown for an auction
     */
    @PostMapping("/auction/{auctionId}/reset-countdown")
    public ResponseEntity<?> resetCountdown(@PathVariable Long auctionId) {
        try {
            countdownService.resetCountdown(auctionId);
            return ResponseEntity.ok("Countdown reset for auction " + auctionId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}