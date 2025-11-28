package com.aiassistant.controller;

import com.aiassistant.dto.AnswerDTO;
import com.aiassistant.dto.WidgetRequest;
import com.aiassistant.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/widget")
@RequiredArgsConstructor
public class WidgetController {

    private final ChatService chatService;

    @PostMapping("/chat")
    public ResponseEntity<AnswerDTO> chat(@RequestBody WidgetRequest request) {
        try {
            // Handle a chat message from the widget.
            AnswerDTO answer = chatService.processMessage(request.getApiKey(), request.getMessage(), request.getHistory());
            return ResponseEntity.ok(answer);
        } catch (SecurityException e) {
            // If API key is bad, return 403.
            return ResponseEntity.status(403).body(new AnswerDTO(e.getMessage(), List.of(), 0.0));
        }
    }
}
