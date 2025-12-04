package com.aiassistant.dto;

public class ClientSettingsDto {
    private String widgetColor;
    private String chatbotName;
    private String welcomeMessage;

    public ClientSettingsDto() {
    }

    public String getWidgetColor() {
        return widgetColor;
    }

    public void setWidgetColor(String widgetColor) {
        this.widgetColor = widgetColor;
    }

    public String getChatbotName() {
        return chatbotName;
    }

    public void setChatbotName(String chatbotName) {
        this.chatbotName = chatbotName;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }
}
