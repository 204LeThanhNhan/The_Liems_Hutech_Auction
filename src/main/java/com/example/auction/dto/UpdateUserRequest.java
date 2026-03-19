package com.example.auction.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String displayName;
    private String email;
    private String avatarUrl;
}
