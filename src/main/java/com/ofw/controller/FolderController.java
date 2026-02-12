package com.ofw.controller;

import com.ofw.model.dto.FolderDTO;
import com.ofw.model.dto.FoldersResponseDTO;
import com.ofw.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Folder operations.
 * Implements OFW API endpoints for folders.
 */
@RestController
@RequestMapping("/pub/v1")
@RequiredArgsConstructor
@Slf4j
public class FolderController {
    
    private final FolderService folderService;
    
    /**
     * Get all message folders.
     * 
     * GET /pub/v1/messageFolders?includeFolderCounts=true
     * 
     * @param includeFolderCounts Whether to include message counts (default: false)
     * @return Folders response with system and user folders
     */
    @GetMapping("/messageFolders")
    public ResponseEntity<FoldersResponseDTO> getFolders(
            @RequestParam(value = "includeFolderCounts", defaultValue = "false") boolean includeFolderCounts) {
        
        log.info("GET /pub/v1/messageFolders?includeFolderCounts={}", includeFolderCounts);
        
        FoldersResponseDTO response = folderService.getFolders(includeFolderCounts);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get a single folder by ID.
     * 
     * GET /pub/v1/messageFolders/{folderId}
     * 
     * @param folderId Folder ID
     * @return Folder details
     */
    @GetMapping("/messageFolders/{folderId}")
    public ResponseEntity<FolderDTO> getFolder(
            @PathVariable Long folderId,
            @RequestParam(value = "includeFolderCounts", defaultValue = "false") boolean includeFolderCounts) {
        
        log.info("GET /pub/v1/messageFolders/{}", folderId);
        
        FolderDTO folder = folderService.getFolder(folderId, includeFolderCounts);
        
        return ResponseEntity.ok(folder);
    }
}
