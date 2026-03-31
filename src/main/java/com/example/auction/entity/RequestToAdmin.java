package com.example.auction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_to_admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestToAdmin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RequestID")
    private Long requestId;
    
    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;
    
    @Column(name = "RequestDate", nullable = false)
    private LocalDateTime requestDate;
    
    @Column(name = "Content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "Status", nullable = false, length = 20)
    private String status; // PENDING, APPROVED, REJECTED
}
