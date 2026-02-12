package com.ofw.service.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.ofw.model.entity.Folder;
import com.ofw.model.entity.UploadSession;
import com.ofw.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Processor for folders.json files.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FoldersFileProcessor implements FileProcessor {
    
    private final FolderRepository folderRepository;
    
    @Override
    public boolean canProcess(String fileName) {
        return fileName.contains("folders") && fileName.endsWith(".json");
    }
    
    @Override
    public int process(String fileName, JsonNode jsonData, UploadSession session) throws Exception {
        log.info("Processing folders file: {}", fileName);
        
        int count = 0;
        
        // Process system folders
        JsonNode systemFolders = jsonData.get("systemFolders");
        if (systemFolders != null && systemFolders.isArray()) {
            for (JsonNode folderNode : systemFolders) {
                processFolder(folderNode, true);
                count++;
            }
        }
        
        // Process user folders
        JsonNode userFolders = jsonData.get("userFolders");
        if (userFolders != null && userFolders.isArray()) {
            for (JsonNode folderNode : userFolders) {
                processFolder(folderNode, false);
                count++;
            }
        }
        
        log.info("Processed {} folders", count);
        return count;
    }
    
    private void processFolder(JsonNode folderNode, boolean isSystemFolder) {
        Long folderId = folderNode.get("id").asLong();
        
        // Skip if already exists
        if (folderRepository.existsByFolderId(folderId)) {
            log.debug("Folder {} already exists, skipping", folderId);
            return;
        }
        
        Folder folder = Folder.builder()
            .folderId(folderId)
            .name(folderNode.get("name").asText())
            .folderType(folderNode.has("folderType") ? 
                folderNode.get("folderType").asText() : "USER")
            .folderOrder(folderNode.has("folderOrder") ? 
                folderNode.get("folderOrder").asInt() : 0)
            .isSystemFolder(isSystemFolder)
            .build();
        
        folderRepository.save(folder);
    }
    
    @Override
    public String getFileType() {
        return "FOLDERS";
    }
}
