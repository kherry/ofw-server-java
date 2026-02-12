package com.ofw.service;

import com.ofw.model.dto.MessageDetailDTO;
import com.ofw.model.dto.MessageListItemDTO;
import com.ofw.model.dto.MessagesResponseDTO;
import com.ofw.model.entity.Message;
import com.ofw.repository.MessageRepository;
import com.ofw.service.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Service for message operations.
 * Implements business logic layer between controller and repository.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final EntityMapper mapper;
    
    /**
     * Get paginated messages for a folder.
     * 
     * @param folderId Folder ID (optional, null for all messages)
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param sortField Field to sort by
     * @param sortDirection Sort direction (ASC/DESC)
     * @return Paginated messages response
     */
    @Transactional(readOnly = true)
    public MessagesResponseDTO getMessages(
            Long folderId,
            int page, 
            int size,
            String sortField,
            String sortDirection) {
        
        log.info("Getting messages: folderId={}, page={}, size={}, sort={} {}", 
            folderId, page, size, sortField, sortDirection);
        
        // Create sort
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField != null ? sortField : "messageDate");
        
        // Create pageable
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get messages
        Page<Message> messagePage;
        if (folderId != null) {
            messagePage = messageRepository.findByFolderIdOrderByMessageDateDesc(folderId, pageable);
        } else {
            messagePage = messageRepository.findAll(pageable);
        }
        
        // Convert to DTOs
        MessagesResponseDTO response = MessagesResponseDTO.builder()
            .data(messagePage.getContent().stream()
                .map(mapper::toMessageListItemDTO)
                .collect(Collectors.toList()))
            .page(page)
            .size(size)
            .totalElements(messagePage.getTotalElements())
            .totalPages(messagePage.getTotalPages())
            .build();
        
        log.info("Returning {} messages (page {} of {})", 
            response.getData().size(), 
            page, 
            response.getTotalPages());
        
        return response;
    }
    
    /**
     * Get a single message by ID with full details.
     * 
     * @param messageId Message ID
     * @return Message detail DTO
     */
    @Transactional(readOnly = true)
    public MessageDetailDTO getMessage(Long messageId) {
        log.info("Getting message {}", messageId);
        
        Message message = messageRepository.findByMessageId(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        
        return mapper.toMessageDetailDTO(message);
    }
    
    /**
     * Mark a message as read.
     * 
     * @param messageId Message ID
     */
    @Transactional
    public void markAsRead(Long messageId) {
        log.info("Marking message {} as read", messageId);
        
        Message message = messageRepository.findByMessageId(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        
        message.setIsRead(true);
        messageRepository.save(message);
    }
    
    /**
     * Mark a message as unread.
     * 
     * @param messageId Message ID
     */
    @Transactional
    public void markAsUnread(Long messageId) {
        log.info("Marking message {} as unread", messageId);
        
        Message message = messageRepository.findByMessageId(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        
        message.setIsRead(false);
        messageRepository.save(message);
    }
    
    /**
     * Delete a message.
     * 
     * @param messageId Message ID
     */
    @Transactional
    public void deleteMessage(Long messageId) {
        log.info("Deleting message {}", messageId);
        
        Message message = messageRepository.findByMessageId(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        
        messageRepository.delete(message);
    }
}
