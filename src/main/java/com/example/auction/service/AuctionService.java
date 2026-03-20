package com.example.auction.service;

import com.example.auction.dto.CreateAuctionRequest;
import com.example.auction.dto.AuctionResponse;
import com.example.auction.entity.Auction;
import com.example.auction.entity.Image;
import com.example.auction.entity.User;
import com.example.auction.repository.AuctionRepository;
import com.example.auction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        
        Auction auction = new Auction();
        auction.setUser(user);
        auction.setAuctionName(request.getAuctionName());
        auction.setProductName(request.getProductName());
        auction.setProductDescription(request.getProductDescription());
        auction.setStartPrice(request.getStartPrice());
        
        // Handle "Mở ngay" - start after 30 seconds
        if (Boolean.TRUE.equals(request.getOpenNow())) {
            auction.setStartTime(LocalDateTime.now().plusSeconds(30));
        } else {
            auction.setStartTime(request.getStartTime());
        }
        
        auction.setEndTime(request.getEndTime());
        
        // Determine status based on start time
        LocalDateTime now = LocalDateTime.now();
        if (auction.getStartTime().isAfter(now)) {
            auction.setStatus("PENDING"); // Sắp bắt đầu
        } else if (auction.getEndTime() != null && auction.getStartTime().isBefore(now) && auction.getEndTime().isAfter(now)) {
            auction.setStatus("ACTIVE"); // Đang diễn ra
        } else if (auction.getEndTime() != null && auction.getEndTime().isBefore(now)) {
            auction.setStatus("ENDED"); // Đã kết thúc
        } else {
            auction.setStatus("ACTIVE"); // Đang diễn ra (không có end time)
        }
        
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
            
            // Find the highest bid (most recent winning bid)
            auction.getBids().stream()
                .filter(bid -> Boolean.TRUE.equals(bid.getIsWinning()))
                .findFirst()
                .ifPresent(winningBid -> {
                    // Set values using method references won't work, use lambda
                });
            
            // Or find by sorting
            auction.getBids().stream()
                .max((b1, b2) -> b1.getBidAmount().compareTo(b2.getBidAmount()))
                .ifPresent(highestBid -> {
                    // These won't work in lambda, need to use different approach
                });
        }
        
        // Better approach: get from bid list
        if (auction.getBids() != null && !auction.getBids().isEmpty()) {
            var highestBid = auction.getBids().stream()
                .max((b1, b2) -> b1.getBidAmount().compareTo(b2.getBidAmount()))
                .orElse(null);
            
            if (highestBid != null) {
                currentPrice = highestBid.getBidAmount();
                highestBidderId = highestBid.getUser().getUserId();
            }
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
        response.setHighestBidderId(highestBidderId); // From bids
        response.setImageUrls(imageUrls);
        response.setTotalBids(totalBids);
        
        return response;
    }
}
