package com.ofw.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for upload session responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadSessionDTO {
    private String sessionId;
    private String status;
    private Integer totalFiles;
    private Integer processedFiles;
    private Integer errorCount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    @Builder.Default
    private List<UploadFileDTO> files = new ArrayList<>();
}

/**
 * DTO for individual file in upload session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class UploadFileDTO {
    private String fileName;
    private String fileType;
    private String status;
    private Integer recordsCreated;
    private String errorMessage;
}

/**
 * Request DTO for uploading debug data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadDebugDataRequest {
    private Long userId;
    private String notes;
}

/**
 * Response DTO for upload result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResultDTO {
    private String sessionId;
    private String status;
    private String message;
    private Integer filesProcessed;
    private Integer recordsCreated;
    private Integer errors;
    @Builder.Default
    private List<String> errorMessages = new ArrayList<>();
}
