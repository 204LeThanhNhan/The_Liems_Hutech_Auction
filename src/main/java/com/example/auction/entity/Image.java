package com.example.auction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ImageID")
    private Long imageId;
    
    @Column(name = "ImageURL", nullable = false, length = 500)
    private String imageUrl;
    
    @ManyToOne
    @JoinColumn(name = "AuctionID", nullable = false)
    private Auction auction;
}
