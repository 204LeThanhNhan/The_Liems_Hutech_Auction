package com.example.auction.repository;

import com.example.auction.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByOrderId(String orderId);
    
    List<Payment> findByAuction_AuctionIdOrderByCreatedAtDesc(Long auctionId);
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.auction.auctionId = :auctionId AND p.status = 'SUCCESS'")
    Long getTotalPaidAmount(Long auctionId);
    
    List<Payment> findByAuction_AuctionIdAndStatusOrderByCreatedAtDesc(Long auctionId, String status);
}
