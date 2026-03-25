package com.example.auction.controller;

import com.example.auction.dto.CreateAuctionRequest;
import com.example.auction.dto.AuctionResponse;
import com.example.auction.service.AuctionService;
import com.example.auction.service.ProductValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {
    
    private final AuctionService auctionService;
    private final ProductValidationService validationService;
    
    @PostMapping
    public ResponseEntity<?> createAuction(@RequestBody CreateAuctionRequest request) {
        try {
            // Validate product before creating auction
            ProductValidationService.ProductValidationResponse validationResult = 
                validationService.validateProduct(
                    request.getAuctionName(),
                    request.getProductName(), 
                    request.getProductDescription(),
                    request.getImageUrls()
                );
            
            if (!validationResult.isValid()) {
                return ResponseEntity.badRequest().body(
                    "Product validation failed: " + 
                    (validationResult.getTextMessage() != null ? validationResult.getTextMessage() + " " : "") +
                    (validationResult.getImageMessage() != null ? validationResult.getImageMessage() : "")
                );
            }
            
            AuctionResponse response = auctionService.createAuction(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<AuctionResponse>> getAllAuctions() {
        List<AuctionResponse> auctions = auctionService.getAllAuctions();
        return ResponseEntity.ok(auctions);
    }
    
    @GetMapping("/{auctionId}")
    public ResponseEntity<?> getAuctionById(@PathVariable Long auctionId) {
        try {
            AuctionResponse response = auctionService.getAuctionById(auctionId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{auctionId}")
    public ResponseEntity<?> updateAuction(
            @PathVariable Long auctionId,
            @RequestBody CreateAuctionRequest request) {
        try {
            AuctionResponse response = auctionService.updateAuction(auctionId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{auctionId}")
    public ResponseEntity<?> deleteAuction(@PathVariable Long auctionId) {
        try {
            auctionService.deleteAuction(auctionId);
            return ResponseEntity.ok("Auction deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
