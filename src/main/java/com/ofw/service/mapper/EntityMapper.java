package com.ofw.service.mapper;

import com.ofw.model.dto.*;
import com.ofw.model.entity.Attachment;
import com.ofw.model.entity.Folder;
import com.ofw.model.entity.Message;
import com.ofw.model.entity.User;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Mapper service to convert entities to DTOs.
 * Uses Mapper pattern for clean separation of concerns.
 */
@Service
public class EntityMapper {
    
    private static final DateTimeFormatter ISO_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter THREE_CHAR_FORMATTER = 
        DateTimeFormatter.ofPattern("EEE, MMM d, h:mm a");
    
    /**
     * Convert User entity to UserDTO.
     */
    public UserDTO toUserDTO(User user) {
        if (user == null) return null;
        
        return UserDTO.builder()
            .userId(user.getUserId())
            .name(user.getFullName())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .displayInitials(user.getDisplayInitials())
            .active(user.getIsActive())
            .type(user.getUserType())
            .color(user.getAvatarColor())
            .build();
    }
    
    /**
     * Convert Folder entity to FolderDTO.
     */
    public FolderDTO toFolderDTO(Folder folder, Integer unreadCount, Integer totalCount) {
        if (folder == null) return null;
        
        return FolderDTO.builder()
            .id(folder.getFolderId())
            .name(folder.getName())
            .folderType(folder.getFolderType())
            .folderOrder(folder.getFolderOrder())
            .unreadMessageCount(unreadCount != null ? unreadCount : 0)
            .totalMessageCount(totalCount != null ? totalCount : 0)
            .build();
    }
    
    /**
     * Convert Message entity to MessageListItemDTO.
     */
    public MessageListItemDTO toMessageListItemDTO(Message message) {
        if (message == null) return null;
        
        return MessageListItemDTO.builder()
            .id(message.getMessageId())
            .folder(message.getFolder().getFolderId())
            .subject(message.getSubject())
            .preview(message.getPreview())
            .files(message.getAttachments() != null ? message.getAttachments().size() : 0)
            .read(message.getIsRead())
            .replied(message.getIsReplied())
            .draft(message.getIsDraft())
            .canReply(message.getCanReply())
            .author(toUserDTO(message.getAuthor()))
            .date(toMessageDateDTO(message))
            .recipients(message.getRecipients().stream()
                .map(user -> {
                    RecipientDTO recipient = new RecipientDTO();
                    recipient.setUser(toUserDTO(user));
                    return recipient;
                })
                .collect(Collectors.toList()))
            .build();
    }
    
    /**
     * Convert Message entity to MessageDetailDTO (includes body).
     */
    public MessageDetailDTO toMessageDetailDTO(Message message) {
        if (message == null) return null;
        
        return MessageDetailDTO.builder()
            .id(message.getMessageId())
            .folder(message.getFolder().getFolderId())
            .subject(message.getSubject())
            .body(message.getBody())
            .preview(message.getPreview())
            .files(message.getAttachments() != null ? message.getAttachments().size() : 0)
            .read(message.getIsRead())
            .replied(message.getIsReplied())
            .draft(message.getIsDraft())
            .canReply(message.getCanReply())
            .author(toUserDTO(message.getAuthor()))
            .date(toMessageDateDTO(message))
            .recipients(message.getRecipients().stream()
                .map(user -> {
                    RecipientDTO recipient = new RecipientDTO();
                    recipient.setUser(toUserDTO(user));
                    return recipient;
                })
                .collect(Collectors.toList()))
            .attachments(message.getAttachments().stream()
                .map(this::toAttachmentDTO)
                .collect(Collectors.toList()))
            .build();
    }
    
    /**
     * Convert message date to MessageDateDTO.
     */
    private MessageDateDTO toMessageDateDTO(Message message) {
        if (message.getMessageDate() == null) return null;
        
        return MessageDateDTO.builder()
            .displayDate(message.getMessageDate().format(DISPLAY_DATE_FORMATTER))
            .displayTime(message.getMessageDate().format(DISPLAY_TIME_FORMATTER))
            .dateTime(message.getMessageDate().format(ISO_FORMATTER))
            .threeCharMonthWeekdayTimeNoYear(message.getMessageDate().format(THREE_CHAR_FORMATTER))
            .build();
    }
    
    /**
     * Convert Attachment entity to AttachmentDTO.
     */
    private AttachmentDTO toAttachmentDTO(Attachment attachment) {
        if (attachment == null) return null;
        
        return AttachmentDTO.builder()
            .name(attachment.getFileName())
            .size(attachment.getFileSize())
            .contentType(attachment.getContentType())
            .build();
    }
}
