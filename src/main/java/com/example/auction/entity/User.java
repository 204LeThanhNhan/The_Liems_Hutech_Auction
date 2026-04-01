package com.example.auction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Long userId;
    
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "display_name", length = 100)
    private String displayName;
    
    @Column(name = "Email", unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(name = "Role", nullable = false, length = 20)
    private String role; // ADMIN or USER
    
    @Column(name = "status", length = 20)
    private String status = "ACTIVE"; // ACTIVE, BANNED, WARNED
    
    @Column(name = "AvatarURL", length = 500)
    private String avatarUrl;
    
    // Không dùng cascade để tránh xóa nhầm dữ liệu quan trọng
    @OneToMany(mappedBy = "user")
    private List<Auction> auctions;
    
    @OneToMany(mappedBy = "user")
    private List<Bid> bids;
    
    @OneToMany(mappedBy = "user")
    private List<Payment> payments;
    
    @OneToMany(mappedBy = "user")
    private List<ChatMessage> chatMessages;
}
