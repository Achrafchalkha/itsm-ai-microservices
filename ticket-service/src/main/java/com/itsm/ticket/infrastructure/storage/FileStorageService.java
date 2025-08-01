package com.itsm.ticket.infrastructure.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for storing files locally in the project
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${app.file-storage.base-path:uploads/tickets}")
    private String basePath;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Save a base64 encoded file to local storage
     */
    public String saveFileFromBase64(String base64Data, String originalFileName, UUID ticketId) {
        try {
            log.info("Saving file: {} for ticket: {}", originalFileName, ticketId);

            // Validate input
            if (base64Data == null || base64Data.trim().isEmpty()) {
                throw new IllegalArgumentException("Base64 data is null or empty");
            }

            if (originalFileName == null || originalFileName.trim().isEmpty()) {
                originalFileName = "unknown_file";
            }

            // Create directory structure: uploads/tickets/YYYY/MM/DD/ticketId/
            LocalDateTime now = LocalDateTime.now();
            String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path ticketDir = Paths.get(basePath, datePath, ticketId.toString());

            // Create directories if they don't exist
            Files.createDirectories(ticketDir);
            log.info("Created directory: {}", ticketDir.toAbsolutePath());

            // Generate unique filename
            String timestamp = now.format(DateTimeFormatter.ofPattern("HHmmss"));
            String fileExtension = getFileExtension(originalFileName);
            String fileName = timestamp + "_" + sanitizeFileName(originalFileName);

            Path filePath = ticketDir.resolve(fileName);
            log.info("Target file path: {}", filePath.toAbsolutePath());

            // Decode base64 and save file
            byte[] fileBytes;
            if (base64Data.contains(",")) {
                // Remove data:image/png;base64, prefix if present
                String base64Content = base64Data.split(",")[1];
                log.info("Extracting base64 content after comma, length: {}", base64Content.length());
                fileBytes = Base64.getDecoder().decode(base64Content);
            } else {
                // Direct base64 data
                log.info("Using direct base64 data, length: {}", base64Data.length());
                fileBytes = Base64.getDecoder().decode(base64Data);
            }

            // Write file to disk
            Files.write(filePath, fileBytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

            String relativePath = basePath + "/" + datePath + "/" + ticketId + "/" + fileName;
            log.info("File saved successfully: {} -> {} ({} bytes)", originalFileName, relativePath, fileBytes.length);

            // Verify file was created
            if (Files.exists(filePath)) {
                log.info("File verification successful: {}", filePath);
            } else {
                log.error("File verification failed: {}", filePath);
            }

            return relativePath;

        } catch (Exception e) {
            log.error("Error saving file {}: {}", originalFileName, e.getMessage(), e);
            throw new RuntimeException("Failed to save file: " + originalFileName, e);
        }
    }
    
    /**
     * Process attached files JSON and save files locally
     */
    public String processAttachedFiles(String fichiersAttachesJson, UUID ticketId) {
        if (fichiersAttachesJson == null || fichiersAttachesJson.trim().isEmpty()) {
            log.info("No attached files to process for ticket {}", ticketId);
            return null;
        }

        try {
            log.info("Processing attached files for ticket {}: {} characters", ticketId, fichiersAttachesJson.length());
            log.info("Raw JSON: {}", fichiersAttachesJson.substring(0, Math.min(200, fichiersAttachesJson.length())));

            // Parse JSON as generic list first to handle different formats
            List<Map<String, Object>> rawFiles = objectMapper.readValue(
                fichiersAttachesJson,
                new TypeReference<List<Map<String, Object>>>() {}
            );

            List<AttachedFileInfo> savedFiles = new ArrayList<>();

            // Process each file
            for (Map<String, Object> rawFile : rawFiles) {
                try {
                    // Extract file information from the map
                    String fileName = (String) rawFile.get("fileName");
                    String originalName = (String) rawFile.get("originalName");
                    Object sizeObj = rawFile.get("size");
                    long size = sizeObj instanceof Number ? ((Number) sizeObj).longValue() : 0L;
                    String mimeType = (String) rawFile.get("mimeType");
                    String uploadDate = (String) rawFile.get("uploadDate");
                    String url = (String) rawFile.get("url");

                    log.info("Processing file: {} ({})", originalName, mimeType);

                    if (url != null && !url.trim().isEmpty()) {
                        // Save file locally and get the path
                        String savedPath = saveFileFromBase64(url, originalName, ticketId);

                        // Create new file info with the saved path
                        AttachedFileInfo savedFileInfo = AttachedFileInfo.builder()
                                .fileName(fileName)
                                .originalName(originalName)
                                .size(size)
                                .mimeType(mimeType)
                                .uploadDate(uploadDate)
                                .filePath(savedPath)  // Store the local file path
                                .url(null)  // Remove base64 data to save space
                                .build();

                        savedFiles.add(savedFileInfo);
                        log.info("File saved: {} -> {}", originalName, savedPath);
                    } else {
                        log.warn("No URL/base64 data found for file: {}", originalName);
                        // Still add file info but mark as error
                        AttachedFileInfo errorFileInfo = AttachedFileInfo.builder()
                                .fileName(fileName)
                                .originalName(originalName)
                                .size(size)
                                .mimeType(mimeType)
                                .uploadDate(uploadDate)
                                .filePath("ERROR: No file data")
                                .url(null)
                                .build();
                        savedFiles.add(errorFileInfo);
                    }

                } catch (Exception e) {
                    log.error("Failed to save individual file: {}", e.getMessage(), e);
                    // Add error entry
                    AttachedFileInfo errorFileInfo = AttachedFileInfo.builder()
                            .fileName("unknown")
                            .originalName("unknown")
                            .size(0L)
                            .mimeType("unknown")
                            .uploadDate(new java.util.Date().toString())
                            .filePath("ERROR: " + e.getMessage())
                            .url(null)
                            .build();
                    savedFiles.add(errorFileInfo);
                }
            }

            // Return JSON with file paths instead of base64 data
            String result = objectMapper.writeValueAsString(savedFiles);
            log.info("Successfully processed {} files for ticket {}", savedFiles.size(), ticketId);
            return result;

        } catch (Exception e) {
            log.error("Error processing attached files for ticket {}: {}", ticketId, e.getMessage(), e);
            // Return a simple error JSON instead of the original data
            return "[{\"fileName\":\"error\",\"originalName\":\"Processing Error\",\"size\":0,\"mimeType\":\"text/plain\",\"uploadDate\":\"" + new java.util.Date() + "\",\"filePath\":\"ERROR: " + e.getMessage() + "\"}]";
        }
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
    
    /**
     * Sanitize filename to remove invalid characters
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unknown";
        }
        
        // Remove or replace invalid characters
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_")
                      .replaceAll("_{2,}", "_") // Replace multiple underscores with single
                      .substring(0, Math.min(fileName.length(), 100)); // Limit length
    }
    
    /**
     * Create upload directory if it doesn't exist
     */
    public void ensureUploadDirectoryExists() {
        try {
            Path uploadPath = Paths.get(basePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", e.getMessage(), e);
        }
    }
}
