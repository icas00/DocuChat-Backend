package com.aiassistant.controller;

import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.dto.ClientSettingsDto;
import com.aiassistant.dto.WidgetRequest;
import com.aiassistant.model.Client;
import com.aiassistant.service.ChatService;
import com.aiassistant.service.ClientService;
import com.aiassistant.service.ChatService;
import com.aiassistant.service.ClientService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/widget")
public class WidgetController {

    private final ChatService chatService;
    private final ClientService clientService;

    public WidgetController(ChatService chatService, ClientService clientService) {
        this.chatService = chatService;
        this.clientService = clientService;
    }

    @PostMapping(value = "/stream-chat", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> streamChat(@RequestBody WidgetRequest request) {
        try {
            return chatService.processStreamingMessage(request.getApiKey(), request.getMessage(), request.getHistory())
                    .map(chunk -> "data: " + chunk + "\n\n"); // Format as SSE manually
        } catch (SecurityException e) {
            return Flux.just("data: Error: " + e.getMessage() + "\n\n");
        }
    }

    @GetMapping("/settings")
    public ResponseEntity<ClientSettingsDto> getWidgetSettings(@RequestParam String apiKey) {
        Optional<Client> clientOpt = clientService.findByApiKey(apiKey);
        if (clientOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Client client = clientOpt.get();
        ClientSettingsDto settings = new ClientSettingsDto();
        settings.setWidgetColor(client.getWidgetColor());
        settings.setChatbotName(client.getChatbotName());
        settings.setWelcomeMessage(client.getWelcomeMessage());
        return ResponseEntity.ok(settings);
    }
}
