package com.ofw.service.processor;

import com.ofw.model.entity.UploadSession;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Strategy interface for processing different types of JSON files.
 * Implements Strategy pattern for extensibility.
 */
public interface FileProcessor {
    
    /**
     * Check if this processor can handle the given file.
     */
    boolean canProcess(String fileName);
    
    /**
     * Process the JSON data from the file.
     * 
     * @param fileName Name of the file
     * @param jsonData Parsed JSON data
     * @param session Upload session for tracking
     * @return Number of records created
     */
    int process(String fileName, JsonNode jsonData, UploadSession session) throws Exception;
    
    /**
     * Get the file type this processor handles.
     */
    String getFileType();
}
