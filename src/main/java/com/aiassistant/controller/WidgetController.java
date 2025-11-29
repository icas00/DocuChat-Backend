package com.aiassistant.controller;

import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.dto.ClientSettingsDto;
import com.aiassistant.dto.WidgetRequest;
import com.aiassistant.model.Client;
import com.aiassistant.service.ChatService;
import com.aiassistant.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/widget")
@RequiredArgsConstructor
public class WidgetController {

    private final ChatService chatService;
    private final ClientService clientService;

    @PostMapping("/chat")
    public ResponseEntity<AnswerDTO> chat(@RequestBody WidgetRequest request) {
        try {
            AnswerDTO answer = chatService.processMessage(request.getApiKey(), request.getMessage(), request.getHistory());
            return ResponseEntity.ok(answer);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(new AnswerDTO(e.getMessage(), List.of(), 0.0));
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
