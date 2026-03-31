package com.example.auction.dto;

import lombok.Data;

@Data
public class CreateRequestDTO {
    private Long userId;
    private String content;
}
