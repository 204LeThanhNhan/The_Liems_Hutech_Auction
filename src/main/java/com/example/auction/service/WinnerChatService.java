package com.example.auction.service;

import com.example.auction.dto.WinnerChatRequest;
import com.example.auction.dto.WinnerChatResponse;
import com.example.auction.dto.WonAuctionResponse;
import com.example.auction.entity.Auction;
import com.example.auction.entity.User;
import com.example.auction.entity.WinnerChat;
import com.example.auction.repository.AuctionRepository;
import com.example.auction.repository.UserRepository;
import com.example.auction.repository.WinnerChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WinnerChatService {
    
    private final WinnerChatRepository winnerChatRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    
    /**
     * Get all won auctions for a user
     */
    public List<WonAuctionResponse> getWonAuctions(Long userId) {
        // Get all auctions where user is the winner
        List<Auction> wonAuctions = auctionRepository.findAll().stream()
            .filter(a -> userId.equals(a.getWinnerId()))
            .collect(Collectors.toList());
        
        return wonAuctions.stream()
            .map(auction -> {
                WonAuctionResponse response = new WonAuctionResponse();
                response.setAuctionId(auction.getAuctionId());
                response.setAuctionName(auction.getAuctionName());
                response.setProductName(auction.getProductName());
                response.setWinningPrice(auction.getEndPrice());
                response.setEndTime(auction.getEndTime());
                
                // Get seller info
                response.setSellerId(auction.getUser().getUserId());
                response.setSellerName(auction.getUser().getDisplayName() != null 
                    ? auction.getUser().getDisplayName() 
                    : auction.getUser().getUsername());
                response.setSellerAvatar(auction.getUser().getAvatarUrl());
                
                // Get images
                if (auction.getImages() != null) {
                    response.setImageUrls(auction.getImages().stream()
                        .map(img -> img.getImageUrl())
                        .collect(Collectors.toList()));
                }
                
                // Get unread count
                Long unreadCount = winnerChatRepository.countUnreadMessages(auction.getAuctionId(), userId);
                response.setUnreadCount(unreadCount != null ? unreadCount : 0L);
                
                // Status: check if there are any messages
                List<WinnerChat> messages = winnerChatRepository.findByAuction_AuctionIdOrderByMessageTimeAsc(auction.getAuctionId());
                if (messages.isEmpty()) {
                    response.setStatus("PENDING");
                } else {
                    response.setStatus("IN_PROGRESS");
                }
                
                return response;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Send a message in winner chat
     * SECURITY: Verify sender is either winner or seller
     */
    @Transactional
    public WinnerChatResponse sendMessage(WinnerChatRequest request) {
        Auction auction = auctionRepository.findById(request.getAuctionId())
            .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        User sender = userRepository.findById(request.getSenderId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // SECURITY CHECK: Verify sender is either winner or seller
        boolean isWinner = auction.getWinnerId() != null && auction.getWinnerId().equals(request.getSenderId());
        boolean isSeller = auction.getUser().getUserId().equals(request.getSenderId());
        
        if (!isWinner && !isSeller) {
            throw new RuntimeException("Unauthorized: You are not part of this conversation");
        }
        
        // Get winner and seller
        User winner = userRepository.findById(auction.getWinnerId())
            .orElseThrow(() -> new RuntimeException("Winner not found"));
        User seller = auction.getUser();
        
        // Create message
        WinnerChat chat = new WinnerChat();
        chat.setAuction(auction);
        chat.setWinner(winner);
        chat.setSeller(seller);
        chat.setSenderId(request.getSenderId());
        chat.setMessage(request.getMessage());
        chat.setMessageTime(LocalDateTime.now());
        chat.setIsRead(false);
        
        WinnerChat saved = winnerChatRepository.save(chat);
        
        log.info("Winner chat message sent: Auction {}, Sender {}", request.getAuctionId(), request.getSenderId());
        
        return convertToResponse(saved);
    }
    
    /**
     * Get chat messages for an auction
     * SECURITY: Verify user is either winner or seller
     */
    public List<WinnerChatResponse> getChatMessages(Long auctionId, Long userId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        // SECURITY CHECK: Verify user is either winner or seller
        boolean isWinner = auction.getWinnerId() != null && auction.getWinnerId().equals(userId);
        boolean isSeller = auction.getUser().getUserId().equals(userId);
        
        if (!isWinner && !isSeller) {
            throw new RuntimeException("Unauthorized: You are not part of this conversation");
        }
        
        List<WinnerChat> messages = winnerChatRepository.findByAuction_AuctionIdOrderByMessageTimeAsc(auctionId);
        
        return messages.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Mark messages as read
     */
    @Transactional
    public void markAsRead(Long auctionId, Long userId) {
        winnerChatRepository.markAllAsRead(auctionId, userId);
    }
    
    /**
     * Get total unread count for a user
     */
    public Long getTotalUnreadCount(Long userId) {
        Long count = winnerChatRepository.countTotalUnreadForWinner(userId);
        return count != null ? count : 0L;
    }
    
    /**
     * Mark auction as completed
     * SECURITY: Only winner or seller can mark as completed
     */
    @Transactional
    public void markAsCompleted(Long auctionId, Long userId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        // SECURITY CHECK
        boolean isWinner = auction.getWinnerId() != null && auction.getWinnerId().equals(userId);
        boolean isSeller = auction.getUser().getUserId().equals(userId);
        
        if (!isWinner && !isSeller) {
            throw new RuntimeException("Unauthorized");
        }
        
        // Update auction status or add a completion flag
        // For now, we can add a field to track this
        log.info("Auction {} marked as completed by user {}", auctionId, userId);
    }
    
    private WinnerChatResponse convertToResponse(WinnerChat chat) {
        User sender = userRepository.findById(chat.getSenderId()).orElse(null);
        
        WinnerChatResponse response = new WinnerChatResponse();
        response.setChatId(chat.getChatId());
        response.setAuctionId(chat.getAuction().getAuctionId());
        response.setSenderId(chat.getSenderId());
        
        if (sender != null) {
            response.setSenderName(sender.getDisplayName() != null 
                ? sender.getDisplayName() 
                : sender.getUsername());
            response.setSenderAvatar(sender.getAvatarUrl());
        }
        
        response.setMessage(chat.getMessage());
        response.setMessageTime(chat.getMessageTime());
        response.setIsRead(chat.getIsRead());
        
        return response;
    }
}
