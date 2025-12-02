package com.aiassistant.controller;

import com.aiassistant.dto.ApiResponse;
import com.aiassistant.dto.ClientSettingsDto;
import com.aiassistant.dto.CreateClientResponse;
import com.aiassistant.model.Client;
import com.aiassistant.service.ClientService;
import com.aiassistant.service.EmbeddingService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);
    private final EmbeddingService embeddingService;
    private final ClientService clientService;

    @Value("${app.admin-key}")
    private String systemAdminKey;

    public ClientController(EmbeddingService embeddingService, ClientService clientService) {
        this.embeddingService = embeddingService;
        this.clientService = clientService;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateClientResponse> createClient() {
        Client client = clientService.createClient("New Client");
        CreateClientResponse response = new CreateClientResponse(
                client.getId(),
                client.getApiKey(),
                client.getAdminKey());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
            String content;
            String filename = file.getOriginalFilename();

            if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
                logger.info("Detected PDF file. Extracting text...");
                try (PDDocument document = PDDocument.load(file.getInputStream())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    content = stripper.getText(document);
                }
            } else {
                // Default to text parsing
                content = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));
            }

            // Sanitize content to remove null bytes which Postgres hates
            content = content.replace("\u0000", "");

            logger.info("File parsed successfully. Size: {} chars", content.length());
            clientService.saveDocument(clientId, filename, content);

            return ResponseEntity.ok(new ApiResponse("Document uploaded successfully. Please trigger indexing next."));

        } catch (IOException e) {
            logger.error("Failed to read file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error processing file: " + e.getMessage()));
        }
    }

    @PostMapping("/{clientId}/index")
    public Mono<ResponseEntity<ApiResponse>> indexDocuments(@PathVariable Long clientId) {
        logger.info("Triggering Indexing for Client ID: {}", clientId);
        return embeddingService.indexClientDocs(clientId)
                .then(Mono.just(ResponseEntity.ok(new ApiResponse("Indexing started for client " + clientId))));
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

    @DeleteMapping("/admin/data")
    public ResponseEntity<ApiResponse> clearSystemData(@RequestHeader("X-Admin-Key") String adminKey) {
        if (!systemAdminKey.trim().equals(adminKey.trim())) {
            logger.warn("Unauthorized attempt to clear system data.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("Invalid System Admin Key"));
        }
        logger.warn("Clearing ALL SYSTEM DATA requested.");
        try {
            clientService.clearSystemData();
            return ResponseEntity
                    .ok(new ApiResponse("All system data (clients, documents, embeddings) has been deleted."));
        } catch (Exception e) {
            logger.error("Error clearing system data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage()));
        }
    }

    @PutMapping("/{clientId}/settings")
    public ResponseEntity<ApiResponse> updateClientSettings(
            @PathVariable Long clientId,
            @RequestBody ClientSettingsDto settings) {
        logger.info("Updating settings for Client ID: {}", clientId);
        try {
            clientService.updateSettings(clientId, settings);
            return ResponseEntity.ok(new ApiResponse("Settings updated successfully."));
        } catch (Exception e) {
            logger.error("Error updating settings for client ID: {}", clientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage()));
        }
    }
}
