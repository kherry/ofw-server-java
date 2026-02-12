package com.ofw.controller;

import com.ofw.model.dto.UploadResultDTO;
import com.ofw.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for uploading debug data.
 * Follows RESTful API design principles.
 */
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
@Slf4j
public class UploadController {
    
    private final UploadService uploadService;
    
    /**
     * Upload debug data files from OFW client debug directory.
     * 
     * POST /api/v1/upload/debug
     * 
     * @param files Array of JSON files from debug directory
     * @param userId Optional user ID to associate with upload
     * @param notes Optional notes about the upload
     * @return Upload result with session ID and statistics
     */
    @PostMapping(value = "/debug", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResultDTO> uploadDebugData(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "notes", required = false) String notes) {
        
        log.info("Received upload request: {} files, userId={}", files.length, userId);
        
        try {
            UploadResultDTO result = uploadService.uploadDebugData(files, userId, notes);
            
            if ("FAILED".equals(result.getStatus())) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(result);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error processing upload", e);
            
            UploadResultDTO errorResult = UploadResultDTO.builder()
                .status("ERROR")
                .message("Upload failed: " + e.getMessage())
                .errors(1)
                .build();
            
            errorResult.getErrorMessages().add(e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResult);
        }
    }
    
    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Upload service is running");
    }
}
