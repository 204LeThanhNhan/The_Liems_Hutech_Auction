package com.example.auction.controller;

import com.example.auction.dto.CreateRequestDTO;
import com.example.auction.dto.RequestToAdminDTO;
import com.example.auction.service.RequestToAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin-requests")
@RequiredArgsConstructor
public class RequestToAdminController {
    
    private final RequestToAdminService requestService;
    
    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody CreateRequestDTO dto) {
        try {
            RequestToAdminDTO response = requestService.createRequest(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<RequestToAdminDTO>> getPendingRequests() {
        List<RequestToAdminDTO> requests = requestService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }
    
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long requestId) {
        try {
            requestService.approveRequest(requestId);
            return ResponseEntity.ok("Request approved successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId) {
        try {
            requestService.rejectRequest(requestId);
            return ResponseEntity.ok("Request rejected successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/check/{userId}")
    public ResponseEntity<?> checkPendingRequest(@PathVariable Long userId) {
        boolean hasPending = requestService.hasPendingRequest(userId);
        return ResponseEntity.ok(hasPending);
    }
}
