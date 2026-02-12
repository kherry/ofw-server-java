package com.ofw.service;

import com.ofw.model.dto.FolderDTO;
import com.ofw.model.dto.FoldersResponseDTO;
import com.ofw.model.entity.Folder;
import com.ofw.repository.FolderRepository;
import com.ofw.repository.MessageRepository;
import com.ofw.service.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for folder operations.
 * Implements business logic layer between controller and repository.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FolderService {
    
    private final FolderRepository folderRepository;
    private final MessageRepository messageRepository;
    private final EntityMapper mapper;
    
    /**
     * Get all folders with optional counts.
     * 
     * @param includeCounts Whether to include message counts
     * @return Folders response with system and user folders
     */
    public FoldersResponseDTO getFolders(boolean includeCounts) {
        log.info("Getting folders, includeCounts={}", includeCounts);
        
        List<Folder> systemFolders = folderRepository.findByIsSystemFolderTrue();
        List<Folder> userFolders = folderRepository.findAll().stream()
            .filter(f -> !f.getIsSystemFolder())
            .collect(Collectors.toList());
        
        FoldersResponseDTO response = FoldersResponseDTO.builder()
            .systemFolders(systemFolders.stream()
                .map(folder -> toFolderDTO(folder, includeCounts))
                .collect(Collectors.toList()))
            .userFolders(userFolders.stream()
                .map(folder -> toFolderDTO(folder, includeCounts))
                .collect(Collectors.toList()))
            .build();
        
        log.info("Returning {} system folders and {} user folders", 
            response.getSystemFolders().size(), 
            response.getUserFolders().size());
        
        return response;
    }
    
    /**
     * Get a single folder by ID.
     */
    public FolderDTO getFolder(Long folderId, boolean includeCounts) {
        log.info("Getting folder {}", folderId);
        
        Folder folder = folderRepository.findByFolderId(folderId)
            .orElseThrow(() -> new IllegalArgumentException("Folder not found: " + folderId));
        
        return toFolderDTO(folder, includeCounts);
    }
    
    /**
     * Convert folder entity to DTO with optional counts.
     */
    private FolderDTO toFolderDTO(Folder folder, boolean includeCounts) {
        Integer unreadCount = null;
        Integer totalCount = null;
        
        if (includeCounts) {
            unreadCount = (int) messageRepository.countUnreadByFolderId(folder.getId());
            totalCount = (int) messageRepository.countByFolderId(folder.getId());
        }
        
        return mapper.toFolderDTO(folder, unreadCount, totalCount);
    }
}
