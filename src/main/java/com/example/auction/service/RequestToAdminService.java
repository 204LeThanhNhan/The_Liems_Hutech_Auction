package com.example.auction.service;

import com.example.auction.dto.CreateRequestDTO;
import com.example.auction.dto.RequestToAdminDTO;
import com.example.auction.entity.RequestToAdmin;
import com.example.auction.entity.User;
import com.example.auction.repository.RequestToAdminRepository;
import com.example.auction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestToAdminService {
    
    private final RequestToAdminRepository requestRepository;
    private final UserRepository userRepository;
    
    public RequestToAdminDTO createRequest(CreateRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user already has a pending request
        if (requestRepository.existsByUserUserIdAndStatus(dto.getUserId(), "PENDING")) {
            throw new RuntimeException("You already have a pending request");
        }
        
        // Check if user is already admin
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("You are already an admin");
        }
        
        RequestToAdmin request = new RequestToAdmin();
        request.setUser(user);
        request.setRequestDate(LocalDateTime.now());
        request.setContent(dto.getContent());
        request.setStatus("PENDING");
        
        RequestToAdmin savedRequest = requestRepository.save(request);
        
        return convertToDTO(savedRequest);
    }
    
    public List<RequestToAdminDTO> getPendingRequests() {
        return requestRepository.findByStatus("PENDING")
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public void approveRequest(Long requestId) {
        RequestToAdmin request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));
        
        User user = request.getUser();
        user.setRole("ADMIN");
        userRepository.save(user);
        
        // Delete the request after approval
        requestRepository.delete(request);
    }
    
    public void rejectRequest(Long requestId) {
        RequestToAdmin request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));
        
        // Delete the request after rejection
        requestRepository.delete(request);
    }
    
    public boolean hasPendingRequest(Long userId) {
        return requestRepository.existsByUserUserIdAndStatus(userId, "PENDING");
    }
    
    private RequestToAdminDTO convertToDTO(RequestToAdmin request) {
        return new RequestToAdminDTO(
            request.getRequestId(),
            request.getUser().getUserId(),
            request.getUser().getUsername(),
            request.getUser().getDisplayName(),
            request.getUser().getEmail(),
            request.getRequestDate(),
            request.getContent(),
            request.getStatus()
        );
    }
}
