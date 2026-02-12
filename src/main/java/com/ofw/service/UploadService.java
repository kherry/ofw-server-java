package com.ofw.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofw.model.dto.UploadResultDTO;
import com.ofw.model.entity.UploadFile;
import com.ofw.model.entity.UploadSession;
import com.ofw.model.entity.User;
import com.ofw.repository.UploadSessionRepository;
import com.ofw.repository.UserRepository;
import com.ofw.service.processor.FileProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling debug data uploads.
 * Uses Facade pattern to simplify complex upload operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {
    
    private final UploadSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final List<FileProcessor> fileProcessors;
    private final ObjectMapper objectMapper;
    
    /**
     * Upload and process debug data files.
     * Uses Template Method pattern via FileProcessor strategies.
     */
    @Transactional
    public UploadResultDTO uploadDebugData(
            MultipartFile[] files, 
            Long userId, 
            String notes) throws IOException {
        
        log.info("Starting upload session for {} files", files.length);
        
        // Create upload session
        UploadSession session = createSession(userId, files.length, notes);
        
        List<String> errorMessages = new ArrayList<>();
        int totalRecords = 0;
        int processedFiles = 0;
        int errors = 0;
        
        // Process each file
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            log.info("Processing file: {}", fileName);
            
            UploadFile uploadFile = UploadFile.builder()
                .session(session)
                .fileName(fileName)
                .fileType(determineFileType(fileName))
                .build();
            
            session.getFiles().add(uploadFile);
            
            try {
                // Find appropriate processor
                FileProcessor processor = findProcessor(fileName);
                
                if (processor == null) {
                    String error = "No processor found for file: " + fileName;
                    log.warn(error);
                    uploadFile.markFailed(error);
                    errorMessages.add(error);
                    errors++;
                    continue;
                }
                
                // Parse JSON
                JsonNode jsonData = objectMapper.readTree(file.getInputStream());
                
                // Process file
                int recordsCreated = processor.process(fileName, jsonData, session);
                
                uploadFile.markSuccess(recordsCreated);
                totalRecords += recordsCreated;
                processedFiles++;
                
                log.info("Successfully processed {} - created {} records", 
                    fileName, recordsCreated);
                
            } catch (Exception e) {
                String error = "Error processing " + fileName + ": " + e.getMessage();
                log.error(error, e);
                uploadFile.markFailed(error);
                errorMessages.add(error);
                errors++;
                session.incrementErrors();
            }
            
            session.incrementProcessed();
        }
        
        // Complete session
        if (errors == 0) {
            session.complete();
        } else if (processedFiles == 0) {
            session.fail("All files failed to process");
        } else {
            session.complete(); // Partial success
        }
        
        sessionRepository.save(session);
        
        return UploadResultDTO.builder()
            .sessionId(session.getSessionId())
            .status(session.getStatus())
            .message(String.format("Processed %d files, created %d records", 
                processedFiles, totalRecords))
            .filesProcessed(processedFiles)
            .recordsCreated(totalRecords)
            .errors(errors)
            .errorMessages(errorMessages)
            .build();
    }
    
    private UploadSession createSession(Long userId, int totalFiles, String notes) {
        User user = userId != null ? 
            userRepository.findByUserId(userId).orElse(null) : null;
        
        UploadSession session = UploadSession.builder()
            .sessionId(UUID.randomUUID().toString())
            .uploadedBy(user)
            .totalFiles(totalFiles)
            .notes(notes)
            .build();
        
        return sessionRepository.save(session);
    }
    
    private FileProcessor findProcessor(String fileName) {
        return fileProcessors.stream()
            .filter(processor -> processor.canProcess(fileName))
            .findFirst()
            .orElse(null);
    }
    
    private String determineFileType(String fileName) {
        if (fileName.contains("messages")) return "MESSAGES";
        if (fileName.contains("folders")) return "FOLDERS";
        if (fileName.contains("cookies")) return "COOKIES";
        if (fileName.contains("localstorage")) return "LOCALSTORAGE";
        return "UNKNOWN";
    }
    
    /**
     * Get upload session by ID.
     */
    public UploadSession getSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Session not found: " + sessionId));
    }
}
