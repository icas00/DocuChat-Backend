package com.aiassistant.dto;

public class CreateClientResponse {
    private Long clientId;
    private String apiKey;
    private String adminKey;

    public CreateClientResponse() {
    }

    public CreateClientResponse(Long clientId, String apiKey, String adminKey) {
        this.clientId = clientId;
        this.apiKey = apiKey;
        this.adminKey = adminKey;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getAdminKey() {
        return adminKey;
    }

    public void setAdminKey(String adminKey) {
        this.adminKey = adminKey;
    }
}
