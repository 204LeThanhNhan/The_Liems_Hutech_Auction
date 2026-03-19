package com.example.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String username;
    private String displayName;
    private String email;
    private String role;
    private String avatarUrl;
}
