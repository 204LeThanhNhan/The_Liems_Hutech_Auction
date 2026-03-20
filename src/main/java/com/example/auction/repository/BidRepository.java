package com.example.auction.repository;

import com.example.auction.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    
    List<Bid> findByAuction_AuctionIdOrderByBidTimeDesc(Long auctionId);
    
    @Query("SELECT COUNT(b) FROM Bid b WHERE b.auction.auctionId = :auctionId")
    Long countBidsByAuctionId(Long auctionId);
    
    @Query("SELECT b FROM Bid b WHERE b.auction.auctionId = :auctionId ORDER BY b.bidTime DESC")
    List<Bid> findTop20ByAuctionIdOrderByBidTimeDesc(Long auctionId);
}
