package com.example.auction.service;

import com.example.auction.dto.ChatMessageRequest;
import com.example.auction.dto.ChatMessageResponse;
import com.example.auction.entity.Auction;
import com.example.auction.entity.ChatMessage;
import com.example.auction.entity.User;
import com.example.auction.repository.AuctionRepository;
import com.example.auction.repository.ChatMessageRepository;
import com.example.auction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    
    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        try {
            // Validate user
            User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Validate auction
            Auction auction = auctionRepository.findById(request.getAuctionId())
                .orElseThrow(() -> new RuntimeException("Auction not found"));
            
            // Validate message
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                throw new RuntimeException("Message cannot be empty");
            }
            
            if (request.getMessage().length() > 1000) {
                throw new RuntimeException("Message too long (max 1000 characters)");
            }
            
            // Create and save chat message
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setAuction(auction);
            chatMessage.setUser(user);
            chatMessage.setMessage(request.getMessage().trim());
            
            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
            
            log.info("Chat message sent: User {} in Auction {}", user.getUsername(), auction.getAuctionId());
            
            // Convert to response
            return convertToResponse(savedMessage);
            
        } catch (Exception e) {
            log.error("Error sending chat message: ", e);
            throw new RuntimeException("Failed to send message: " + e.getMessage());
        }
    }
    
    public List<ChatMessageResponse> getRecentMessages(Long auctionId, int limit) {
        try {
            List<ChatMessage> messages = chatMessageRepository.findByAuctionId(auctionId);
            
            // Limit in Java instead of SQL
            return messages.stream()
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error fetching chat messages for auction {}: ", auctionId, e);
            throw new RuntimeException("Failed to fetch messages");
        }
    }
    
    private ChatMessageResponse convertToResponse(ChatMessage message) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setMessageId(message.getMessageId());
        response.setAuctionId(message.getAuctionId());
        response.setUserId(message.getUser().getUserId());
        response.setUsername(message.getUser().getUsername());
        response.setDisplayName(message.getUser().getDisplayName() != null 
            ? message.getUser().getDisplayName() 
            : message.getUser().getUsername());
        response.setAvatarURL(message.getUser().getAvatarUrl());
        response.setMessage(message.getMessage());
        response.setMessageTime(message.getMessageTime());
        return response;
    }
}
