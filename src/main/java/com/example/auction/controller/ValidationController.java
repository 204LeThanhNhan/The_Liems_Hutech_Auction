package com.example.auction.controller;

import com.example.auction.service.ProductValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/validate")
@RequiredArgsConstructor
public class ValidationController {
    
    private final ProductValidationService validationService;
    
    @PostMapping("/product")
    public ResponseEntity<?> validateProduct(@RequestBody Map<String, Object> request) {
        try {
            String auctionName = (String) request.get("auctionName");
            String productName = (String) request.get("productName");
            String productDescription = (String) request.get("productDescription");
            List<String> imageUrls = (List<String>) request.get("imageUrls");
            
            ProductValidationService.ProductValidationResponse response = 
                validationService.validateProduct(auctionName, productName, productDescription, imageUrls);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        }
    }
}
