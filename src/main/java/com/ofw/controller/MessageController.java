package com.ofw.controller;

import com.ofw.model.dto.MessageDetailDTO;
import com.ofw.model.dto.MessagesResponseDTO;
import com.ofw.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Message operations.
 * Implements OFW API endpoints for messages.
 */
@RestController
@RequestMapping("/pub/v3")
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    
    private final MessageService messageService;
    
    /**
     * Get paginated messages.
     * 
     * GET /pub/v3/messages?folder={folderId}&page={page}&size={size}&sort={field}&sortDirection={dir}
     * 
     * @param folderId Folder ID (optional)
     * @param page Page number (default: 0)
     * @param size Page size (default: 25)
     * @param sort Sort field (default: messageDate)
     * @param sortDirection Sort direction (default: DESC)
     * @return Paginated messages response
     */
    @GetMapping("/messages")
    public ResponseEntity<MessagesResponseDTO> getMessages(
            @RequestParam(value = "folder", required = false) Long folderId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "25") int size,
            @RequestParam(value = "sort", defaultValue = "messageDate") String sort,
            @RequestParam(value = "sortDirection", defaultValue = "DESC") String sortDirection) {
        
        log.info("GET /pub/v3/messages?folder={}&page={}&size={}&sort={}&sortDirection={}", 
            folderId, page, size, sort, sortDirection);
        
        MessagesResponseDTO response = messageService.getMessages(
            folderId, page, size, sort, sortDirection);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get a single message by ID.
     * 
     * GET /pub/v3/messages/{messageId}
     * 
     * @param messageId Message ID
     * @return Message details with body
     */
    @GetMapping("/messages/{messageId}")
    public ResponseEntity<MessageDetailDTO> getMessage(@PathVariable Long messageId) {
        
        log.info("GET /pub/v3/messages/{}", messageId);
        
        MessageDetailDTO message = messageService.getMessage(messageId);
        
        return ResponseEntity.ok(message);
    }
    
    /**
     * Mark a message as read.
     * 
     * PUT /pub/v3/messages/{messageId}/read
     * 
     * @param messageId Message ID
     * @return Success response
     */
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long messageId) {
        
        log.info("PUT /pub/v3/messages/{}/read", messageId);
        
        messageService.markAsRead(messageId);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Mark a message as unread.
     * 
     * PUT /pub/v3/messages/{messageId}/unread
     * 
     * @param messageId Message ID
     * @return Success response
     */
    @PutMapping("/messages/{messageId}/unread")
    public ResponseEntity<Void> markAsUnread(@PathVariable Long messageId) {
        
        log.info("PUT /pub/v3/messages/{}/unread", messageId);
        
        messageService.markAsUnread(messageId);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Delete a message.
     * 
     * DELETE /pub/v3/messages/{messageId}
     * 
     * @param messageId Message ID
     * @return Success response
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        
        log.info("DELETE /pub/v3/messages/{}", messageId);
        
        messageService.deleteMessage(messageId);
        
        return ResponseEntity.noContent().build();
    }
}
