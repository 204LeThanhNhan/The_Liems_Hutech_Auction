package com.example.auction.service;

import com.example.auction.entity.Auction;
import com.example.auction.repository.AuctionRepository;
import com.example.auction.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionFinalizationService {
    
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    
    /**
     * Scheduled task to finalize ended auctions
     * Runs every minute to check for auctions that have ended
     */
    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    @Transactional
    public void finalizeEndedAuctions() {
        try {
            // Find all active auctions that have ended
            List<Auction> endedAuctions = auctionRepository
                .findByStatusAndEndTimeBefore("ACTIVE", LocalDateTime.now());
            
            if (endedAuctions.isEmpty()) {
                return;
            }
            
            log.info("Found {} auctions to finalize", endedAuctions.size());
            
            for (Auction auction : endedAuctions) {
                finalizeAuction(auction);
            }
            
        } catch (Exception e) {
            log.error("Error finalizing auctions: ", e);
        }
    }
    
    /**
     * Finalize a single auction
     * - Update status to COMPLETED
     * - Calculate and save total bids
     * - Set winner information
     */
    @Transactional
    public void finalizeAuction(Auction auction) {
        try {
            Long auctionId = auction.getAuctionId();
            
            // Calculate total bids
            Long totalBidsLong = bidRepository.countBidsByAuctionId(auctionId);
            int totalBids = totalBidsLong != null ? totalBidsLong.intValue() : 0;
            
            // Get highest bidder (winner)
            List<com.example.auction.entity.Bid> bids = bidRepository
                .findByAuction_AuctionIdOrderByBidTimeDesc(auctionId);
            
            Long winnerId = null;
            BigDecimal endPrice = auction.getCurrentPrice();
            
            if (!bids.isEmpty()) {
                com.example.auction.entity.Bid winningBid = bids.get(0);
                winnerId = winningBid.getUser().getUserId();
                endPrice = winningBid.getBidAmount();
            }
            
            // Update auction
            auction.setStatus("COMPLETED");
            auction.setTotalBids(totalBids);
            auction.setWinnerId(winnerId);
            auction.setEndPrice(endPrice);
            
            auctionRepository.save(auction);
            
            log.info("Finalized auction {}: {} total bids, winner: {}, end price: {}", 
                auctionId, totalBids, winnerId, endPrice);
            
        } catch (Exception e) {
            log.error("Error finalizing auction {}: ", auction.getAuctionId(), e);
        }
    }
    
    /**
     * Manual finalization for testing or admin actions
     */
    @Transactional
    public void finalizeAuctionById(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));
        finalizeAuction(auction);
    }
}
