package com.example.auction.service;

import com.example.auction.dto.BidHistoryResponse;
import com.example.auction.dto.BidRequest;
import com.example.auction.dto.BidResponse;
import com.example.auction.entity.Auction;
import com.example.auction.entity.Bid;
import com.example.auction.entity.User;
import com.example.auction.repository.AuctionRepository;
import com.example.auction.repository.BidRepository;
import com.example.auction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class BiddingService {
    
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final CountdownService countdownService;
    
    // In-memory cache for active auctions
    private final Map<Long, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();
    
    @Transactional
    public BidResponse placeBid(BidRequest request) {
        Long auctionId = request.getAuctionId();
        
        // Get or create lock for this auction
        ReentrantLock lock = auctionLocks.computeIfAbsent(auctionId, k -> new ReentrantLock());
        
        lock.lock();
        try {
            // 1. Validate auction
            Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Phiên đấu giá không tồn tại"));
            
            // CRITICAL: Check if auction has started
            LocalDateTime now = LocalDateTime.now();
            if (auction.getStartTime().isAfter(now)) {
                return BidResponse.error("Phiên đấu giá chưa bắt đầu. Vui lòng quay lại sau.");
            }
            
            if (!"ACTIVE".equals(auction.getStatus())) {
                return BidResponse.error("Phiên đấu giá không còn hoạt động");
            }
            
            // Check countdown status
            String countdownStatus = auction.getCountdownStatus();
            
            // Allow bidding if:
            // - No countdown started yet (null)
            // - In SOLD_TEMP status
            // - In any countdown round (ROUND_1, ROUND_2, ROUND_3)
            if ("SOLD_FINAL".equals(countdownStatus)) {
                return BidResponse.error("Phiên đấu giá đã kết thúc");
            }
            
            if ("SOLD_TEMP".equals(countdownStatus)) {
                log.info("Auction {} in SOLD_TEMP - new bid will restart countdown", auctionId);
            }
            
            // Check if auction time has ended (only if not in SOLD_TEMP)
            if (auction.getEndTime() != null && now.isAfter(auction.getEndTime()) && 
                !"SOLD_TEMP".equals(countdownStatus)) {
                return BidResponse.error("Phiên đấu giá đã hết thời gian");
            }
            
            // 2. Validate user
            User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            
            // 3. Validate bid amount - get current price from latest bid
            BigDecimal currentPrice;
            List<Bid> recentBids = bidRepository
                .findByAuction_AuctionIdOrderByBidTimeDesc(auctionId);
            
            if (!recentBids.isEmpty()) {
                currentPrice = recentBids.get(0).getBidAmount();
            } else {
                currentPrice = auction.getStartPrice();
            }
            
            // Custom bid: amount must be greater than currentPrice
            if (request.getAmount().compareTo(currentPrice) <= 0) {
                return BidResponse.error("Giá đặt phải cao hơn giá hiện tại: " + 
                    currentPrice.longValue() + " ₫");
            }
            
            // 4. Create bid
            Bid bid = new Bid();
            bid.setAuction(auction);
            bid.setUser(user);
            bid.setBidAmount(request.getAmount());
            bid.setBidType(request.getType());
            bid.setIsWinning(true);
            
            // 5. Update previous winning bid
            if (!recentBids.isEmpty()) {
                Bid previousWinningBid = recentBids.get(0);
                previousWinningBid.setIsWinning(false);
                bidRepository.save(previousWinningBid);
            }
            
            // 6. Save bid to DB (don't update auction.currentPrice during bidding)
            Bid savedBid = bidRepository.save(bid);
            
            // 7. Handle countdown logic
            if (auction.getCountdownStartTime() == null) {
                // First bid - start countdown
                countdownService.startCountdown(auctionId);
                log.info("First bid placed - countdown started for auction {}", auctionId);
            } else {
                // Subsequent bid - reset countdown
                countdownService.resetCountdown(auctionId);
                log.info("New bid placed - countdown reset for auction {}", auctionId);
            }
            
            log.info("Bid saved - ID: {}, Amount: {}, Current highest: {}", 
                savedBid.getBidId(), savedBid.getBidAmount(), currentPrice);
            
            // 8. Calculate total bids from bid history (don't store in DB during bidding)
            Long totalBidsLong = bidRepository.countBidsByAuctionId(auctionId);
            int totalBids = totalBidsLong != null ? totalBidsLong.intValue() : 0;
            
            log.info("Bid placed successfully: User {} bid {} on Auction {} (Total bids: {})", 
                user.getUsername(), request.getAmount(), auctionId, totalBids);
            
            // 9. Prepare response - use bid amount as current price
            BidResponse.BidData bidData = new BidResponse.BidData();
            bidData.setBidId(savedBid.getBidId());
            bidData.setAuctionId(auctionId);
            bidData.setUserId(user.getUserId());
            bidData.setUsername(user.getUsername());
            bidData.setAvatarURL(user.getAvatarUrl());
            bidData.setBidAmount(request.getAmount());
            bidData.setCurrentPrice(request.getAmount()); // Current price = this bid amount
            bidData.setHighestBidderId(user.getUserId());
            bidData.setHighestBidderName(user.getUsername());
            bidData.setTotalBids(totalBids); // Use calculated value, not from DB
            bidData.setBidTime(savedBid.getBidTime());
            bidData.setBidType(request.getType());
            
            return BidResponse.success(bidData);
            
        } catch (Exception e) {
            log.error("Error placing bid: ", e);
            return BidResponse.error("Có lỗi xảy ra: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
    
    public List<Bid> getBidHistory(Long auctionId) {
        return bidRepository.findByAuction_AuctionIdOrderByBidTimeDesc(auctionId);
    }
    
    public List<BidHistoryResponse> getBidHistoryDTO(Long auctionId) {
        List<Bid> bids = bidRepository.findByAuction_AuctionIdOrderByBidTimeDesc(auctionId);
        return bids.stream()
            .map(this::convertBidToDTO)
            .collect(java.util.stream.Collectors.toList());
    }
    
    private BidHistoryResponse convertBidToDTO(Bid bid) {
        BidHistoryResponse dto = new BidHistoryResponse();
        dto.setBidId(bid.getBidId());
        dto.setAuctionId(bid.getAuction().getAuctionId());
        dto.setUserId(bid.getUser().getUserId());
        dto.setUsername(bid.getUser().getUsername());
        dto.setDisplayName(bid.getUser().getDisplayName() != null 
            ? bid.getUser().getDisplayName() 
            : bid.getUser().getUsername()); // Fallback to username if no display name
        dto.setAvatarURL(bid.getUser().getAvatarUrl());
        dto.setBidAmount(bid.getBidAmount());
        dto.setBidTime(bid.getBidTime());
        dto.setBidType(bid.getBidType());
        dto.setIsWinning(bid.getIsWinning());
        return dto;
    }
    
    public Auction getAuctionDetails(Long auctionId) {
        return auctionRepository.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Phiên đấu giá không tồn tại"));
    }
}
