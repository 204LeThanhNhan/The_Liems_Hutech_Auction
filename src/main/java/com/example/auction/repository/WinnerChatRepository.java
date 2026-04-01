package com.example.auction.repository;

import com.example.auction.entity.WinnerChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface WinnerChatRepository extends JpaRepository<WinnerChat, Long> {
    
    // Get all messages for a specific auction chat
    List<WinnerChat> findByAuction_AuctionIdOrderByMessageTimeAsc(Long auctionId);
    
    // Get all won auctions for a user (distinct auctions where user is winner)
    @Query("SELECT DISTINCT wc.auction FROM WinnerChat wc WHERE wc.winner.userId = :userId ORDER BY wc.messageTime DESC")
    List<com.example.auction.entity.Auction> findWonAuctionsByWinnerId(Long userId);
    
    // Count unread messages for a user in a specific auction
    @Query("SELECT COUNT(wc) FROM WinnerChat wc WHERE wc.auction.auctionId = :auctionId AND wc.senderId != :userId AND wc.isRead = false")
    Long countUnreadMessages(Long auctionId, Long userId);
    
    // Count total unread messages for a user across all won auctions
    @Query("SELECT COUNT(wc) FROM WinnerChat wc WHERE wc.winner.userId = :userId AND wc.senderId != :userId AND wc.isRead = false")
    Long countTotalUnreadForWinner(Long userId);
    
    // Mark all messages as read for a user in an auction
    @Modifying
    @Transactional
    @Query("UPDATE WinnerChat wc SET wc.isRead = true WHERE wc.auction.auctionId = :auctionId AND wc.senderId != :userId")
    void markAllAsRead(Long auctionId, Long userId);
}
