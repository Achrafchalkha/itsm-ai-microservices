package com.itsm.ticket.infrastructure.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents information about an attached file
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachedFileInfo {
    
    private String fileName;        // Generated filename
    private String originalName;    // Original filename from user
    private long size;             // File size in bytes
    private String mimeType;       // MIME type
    private String uploadDate;     // Upload timestamp
    private String url;            // Base64 data or file path
    private String filePath;       // Local file path (after saving)
}
