package com.aiassistant.controller;

import com.aiassistant.dto.ApiResponse;
import com.aiassistant.service.ClientService;
import com.aiassistant.service.EmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*") 
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);
    private final EmbeddingService embeddingService;
    private final ClientService clientService;

    public ClientController(EmbeddingService embeddingService, ClientService clientService) {
        this.embeddingService = embeddingService;
        this.clientService = clientService;
    }

    @PostMapping(value = "/{clientId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadDocument(
            @PathVariable Long clientId,
            @RequestParam("file") MultipartFile file) {

        logger.info("Received file upload for Client ID: {}", clientId);

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse("File is empty"));
        }

        try {
            String content = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            logger.info("File parsed successfully. Size: {} chars", content.length());
            clientService.saveDocument(clientId, file.getOriginalFilename(), content);
            
            return ResponseEntity.ok(new ApiResponse("Document uploaded successfully. Please trigger indexing next."));

        } catch (IOException e) {
            logger.error("Failed to read file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("Error processing file"));
        }
    }

    @PostMapping("/{clientId}/index")
    public ResponseEntity<ApiResponse> indexDocuments(@PathVariable Long clientId) {
        logger.info("Triggering Indexing for Client ID: {}", clientId);
        embeddingService.indexClientDocs(clientId);
        return ResponseEntity.ok(new ApiResponse("Indexing completed for client " + clientId));
    }

    @DeleteMapping("/{clientId}/data")
    public ResponseEntity<ApiResponse> clearClientData(@PathVariable Long clientId) {
        logger.info("Clearing all data for Client ID: {}", clientId);
        try {
            long count = clientService.clearAllData(clientId);
            return ResponseEntity.ok(new ApiResponse(String.format("Successfully deleted %d document(s).", count)));
        } catch (Exception e) {
            logger.error("Error clearing data for client ID: {}", clientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage()));
        }
    }
}
