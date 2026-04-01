package com.example.auction.service;

import com.example.auction.dto.CreateAuctionRequest;
import com.example.auction.dto.AuctionResponse;
import com.example.auction.dto.CountdownStatusResponse;
import com.example.auction.entity.Auction;
import com.example.auction.entity.Image;
import com.example.auction.entity.User;
import com.example.auction.repository.AuctionRepository;
import com.example.auction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionService {
    
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public AuctionResponse createAuction(CreateAuctionRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Only admins can create auctions");
        }
        
        // Time validation
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        LocalDateTime endTime = request.getEndTime(); // Can be null
        
        // Handle "Mở ngay" - start after 30 seconds
        if (Boolean.TRUE.equals(request.getOpenNow())) {
            startTime = now.plusSeconds(30);
        } else {
            startTime = request.getStartTime();
            
            // Validate start time is not in the past
            if (startTime.isBefore(now)) {
                throw new RuntimeException("Thời gian bắt đầu không thể là thời gian trong quá khứ");
            }
        }
        
        // Validate end time only if provided (not required)
        if (endTime != null) {
            // End time must be after start time
            if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
                throw new RuntimeException("Thời gian kết thúc phải sau thời gian bắt đầu");
            }
            
            // End time must be at least 2 hours from now
            if (endTime.isBefore(now.plusHours(2))) {
                throw new RuntimeException("Thời gian kết thúc phải cách thời gian hiện tại ít nhất 2 giờ");
            }
            
            // Minimum auction duration: 30 minutes
            if (Duration.between(startTime, endTime).toMinutes() < 30) {
                throw new RuntimeException("Phiên đấu giá phải kéo dài ít nhất 30 phút");
            }
        }
        
        Auction auction = new Auction();
        auction.setUser(user);
        auction.setAuctionName(request.getAuctionName());
        auction.setProductName(request.getProductName());
        auction.setProductDescription(request.getProductDescription());
        auction.setStartPrice(request.getStartPrice());
        auction.setStartTime(startTime);
        auction.setEndTime(endTime);
        
        // Set status to ACTIVE immediately
        // Frontend will handle access control based on startTime
        auction.setStatus("ACTIVE");
        
        Auction savedAuction = auctionRepository.save(auction);
        
        // Save images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            final Auction finalAuction = savedAuction;
            List<Image> images = request.getImageUrls().stream()
                .map(url -> {
                    Image image = new Image();
                    image.setImageUrl(url);
                    image.setAuction(finalAuction);
                    return image;
                })
                .collect(Collectors.toList());
            savedAuction.setImages(images);
            savedAuction = auctionRepository.save(savedAuction);
        }
        
        return convertToResponse(savedAuction);
    }
    
    public List<AuctionResponse> getAllAuctions() {
        return auctionRepository.findAll()
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public AuctionResponse getAuctionById(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        // Allow viewing auction details even if not started yet
        // Frontend will handle preview mode vs bidding mode
        return convertToResponse(auction);
    }
    
    @Transactional
    public AuctionResponse updateAuction(Long auctionId, CreateAuctionRequest request) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        auction.setAuctionName(request.getAuctionName());
        auction.setProductName(request.getProductName());
        auction.setProductDescription(request.getProductDescription());
        auction.setStartPrice(request.getStartPrice());
        auction.setStartTime(request.getStartTime());
        auction.setEndTime(request.getEndTime());
        
        Auction updatedAuction = auctionRepository.save(auction);
        return convertToResponse(updatedAuction);
    }
    
    public void deleteAuction(Long auctionId) {
        if (!auctionRepository.existsById(auctionId)) {
            throw new RuntimeException("Auction not found");
        }
        auctionRepository.deleteById(auctionId);
    }
    
    public List<AuctionResponse> getAuctionsByUser(Long userId) {
        return auctionRepository.findByUserUserId(userId)
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public CountdownStatusResponse getCountdownStatus(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        // If no countdown started yet
        if (auction.getCountdownStartTime() == null) {
            return CountdownStatusResponse.waiting(auctionId, auction.getEndTime());
        }
        
        // Calculate seconds remaining based on current round
        LocalDateTime now = LocalDateTime.now();
        long secondsElapsed = Duration.between(auction.getCountdownStartTime(), now).getSeconds();
        int secondsRemaining = 0;
        
        String status = auction.getCountdownStatus();
        switch (status) {
            case "ROUND_1":
                secondsRemaining = Math.max(0, 30 - (int)secondsElapsed);
                break;
            case "ROUND_2":
                secondsRemaining = Math.max(0, 20 - (int)secondsElapsed);
                break;
            case "ROUND_3":
                secondsRemaining = Math.max(0, 10 - (int)secondsElapsed);
                break;
            case "SOLD_TEMP":
            case "SOLD_FINAL":
                secondsRemaining = 0;
                break;
        }
        
        return CountdownStatusResponse.active(
            auctionId,
            status,
            auction.getCountdownRound(),
            auction.getCountdownStartTime(),
            secondsRemaining,
            auction.getExtensionCount(),
            auction.getEndTime(),
            auction.getOriginalEndTime()
        );
    }
    
    private AuctionResponse convertToResponse(Auction auction) {
        List<String> imageUrls = auction.getImages() != null 
            ? auction.getImages().stream()
                .map(Image::getImageUrl)
                .collect(Collectors.toList())
            : List.of();
        
        // Calculate current price and highest bidder from bids
        BigDecimal currentPrice = null;
        Long highestBidderId = null;
        Integer totalBids = 0;
        
        if (auction.getBids() != null && !auction.getBids().isEmpty()) {
            totalBids = auction.getBids().size();
            
            var highestBid = auction.getBids().stream()
                .max((b1, b2) -> b1.getBidAmount().compareTo(b2.getBidAmount()))
                .orElse(null);
            
            if (highestBid != null) {
                currentPrice = highestBid.getBidAmount();
                highestBidderId = highestBid.getUser().getUserId();
            }
        }
        
        // Get winner information
        String winnerDisplayName = null;
        String winnerUsername = null;
        String winnerAvatar = null;
        if (auction.getWinnerId() != null) {
            User winner = userRepository.findById(auction.getWinnerId()).orElse(null);
            if (winner != null) {
                winnerDisplayName = winner.getDisplayName(); // For admin view
                winnerUsername = winner.getUsername(); // For user view
                winnerAvatar = winner.getAvatarUrl(); // Winner avatar
            }
        }
        
        // Check payment status (simplified - assume completed if endPrice matches total payments)
        Boolean paymentCompleted = false;
        if (auction.getEndPrice() != null) {
            // TODO: Implement actual payment status check
            // For now, assume not completed unless explicitly marked
            paymentCompleted = false;
        }
        
        AuctionResponse response = new AuctionResponse();
        response.setAuctionId(auction.getAuctionId());
        response.setUserId(auction.getUser().getUserId());
        response.setAuctionName(auction.getAuctionName());
        response.setStartTime(auction.getStartTime());
        response.setEndTime(auction.getEndTime());
        response.setProductName(auction.getProductName());
        response.setProductDescription(auction.getProductDescription());
        response.setStartPrice(auction.getStartPrice());
        response.setCurrentPrice(currentPrice); // From bids
        response.setEndPrice(auction.getEndPrice());
        response.setStatus(auction.getStatus());
        response.setWinnerId(auction.getWinnerId());
        response.setWinnerDisplayName(winnerDisplayName); // For admin
        response.setWinnerUsername(winnerUsername); // For user
        response.setWinnerAvatar(winnerAvatar); // Winner avatar
        response.setHighestBidderId(highestBidderId); // From bids
        response.setImageUrls(imageUrls);
        response.setTotalBids(totalBids);
        response.setPaymentCompleted(paymentCompleted); // Payment status
        
        return response;
    }
}
