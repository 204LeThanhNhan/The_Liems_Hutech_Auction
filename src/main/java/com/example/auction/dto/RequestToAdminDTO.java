package com.example.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestToAdminDTO {
    private Long requestId;
    private Long userId;
    private String username;
    private String displayName;
    private String email;
    private LocalDateTime requestDate;
    private String content;
    private String status;
}
