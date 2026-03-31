package com.example.auction.repository;

import com.example.auction.entity.RequestToAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestToAdminRepository extends JpaRepository<RequestToAdmin, Long> {
    List<RequestToAdmin> findByStatus(String status);
    Optional<RequestToAdmin> findByUserUserIdAndStatus(Long userId, String status);
    boolean existsByUserUserIdAndStatus(Long userId, String status);
}
