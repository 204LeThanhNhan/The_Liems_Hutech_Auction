package com.example.auction.repository;

import com.example.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByStatus(String status);
    List<Auction> findByUserUserId(Long userId);
    List<Auction> findByStatusAndEndTimeBefore(String status, LocalDateTime endTime);
    
    /**
     * Optimized query: Only get auctions without countdown started
     */
    @Query("SELECT a FROM Auction a WHERE a.status = ?1 AND a.countdownStartTime IS NULL")
    List<Auction> findByStatusAndCountdownStartTimeIsNull(String status);
}
