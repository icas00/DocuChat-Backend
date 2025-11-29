package com.aiassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateClientResponse {
    private Long clientId;
    private String apiKey;
    private String adminKey;
}
