package com.example.auction.repository;

import com.example.auction.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    @Query("SELECT cm FROM ChatMessage cm " +
           "JOIN FETCH cm.user u " +
           "JOIN FETCH cm.auction a " +
           "WHERE a.auctionId = :auctionId " +
           "ORDER BY cm.messageTime ASC")
    List<ChatMessage> findByAuctionId(@Param("auctionId") Long auctionId);
}
