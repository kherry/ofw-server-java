package com.ofw.service.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.ofw.model.entity.*;
import com.ofw.repository.FolderRepository;
import com.ofw.repository.MessageRepository;
import com.ofw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

/**
 * Processor for messages.json files.
 * Implements Strategy pattern for message file processing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessagesFileProcessor implements FileProcessor {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    
    private static final DateTimeFormatter ISO_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    @Override
    public boolean canProcess(String fileName) {
        return fileName.contains("messages") && fileName.endsWith(".json");
    }
    
    @Override
    public int process(String fileName, JsonNode jsonData, UploadSession session) throws Exception {
        log.info("Processing messages file: {}", fileName);
        
        int count = 0;
        JsonNode dataArray = jsonData.get("data");
        
        if (dataArray == null || !dataArray.isArray()) {
            throw new IllegalArgumentException("Invalid messages.json format - expected 'data' array");
        }
        
        for (JsonNode msgNode : dataArray) {
            try {
                processMessage(msgNode);
                count++;
            } catch (Exception e) {
                log.error("Error processing message: {}", e.getMessage());
                // Continue with next message
            }
        }
        
        log.info("Processed {} messages", count);
        return count;
    }
    
    private void processMessage(JsonNode msgNode) {
        Long messageId = msgNode.get("id").asLong();
        
        // Skip if already exists
        if (messageRepository.existsByMessageId(messageId)) {
            log.debug("Message {} already exists, skipping", messageId);
            return;
        }
        
        // Get or create author
        User author = getOrCreateUser(msgNode.get("author"));
        
        // Get or create folder
        Long folderId = msgNode.get("folder").asLong();
        Folder folder = folderRepository.findByFolderId(folderId)
            .orElseGet(() -> createDefaultFolder(folderId));
        
        // Parse date
        String dateTimeStr = msgNode.get("date").get("dateTime").asText();
        LocalDateTime messageDate = LocalDateTime.parse(dateTimeStr, ISO_FORMATTER);
        
        // Create message
        Message message = Message.builder()
            .messageId(messageId)
            .folder(folder)
            .subject(msgNode.get("subject").asText())
            .preview(msgNode.get("preview").asText(""))
            .isDraft(msgNode.get("draft").asBoolean(false))
            .isRead(msgNode.get("read").asBoolean(false))
            .isReplied(msgNode.get("replied").asBoolean(false))
            .canReply(msgNode.get("canReply").asBoolean(true))
            .author(author)
            .messageDate(messageDate)
            .recipients(new HashSet<>())
            .build();
        
        // Add recipients
        JsonNode recipients = msgNode.get("recipients");
        if (recipients != null && recipients.isArray()) {
            for (JsonNode recipientNode : recipients) {
                User recipient = getOrCreateUser(recipientNode.get("user"));
                message.addRecipient(recipient);
            }
        }
        
        messageRepository.save(message);
    }
    
    private User getOrCreateUser(JsonNode userNode) {
        Long userId = userNode.get("userId").asLong();
        
        return userRepository.findByUserId(userId).orElseGet(() -> {
            User user = User.builder()
                .userId(userId)
                .username(userNode.get("name").asText())
                .firstName(userNode.get("firstName").asText(""))
                .lastName(userNode.get("lastName").asText(""))
                .displayInitials(userNode.get("displayInitials").asText(""))
                .avatarColor(userNode.get("color").asText("#000000"))
                .isActive(userNode.get("active").asBoolean(true))
                .userType(userNode.get("type").asText(""))
                .build();
            
            return userRepository.save(user);
        });
    }
    
    private Folder createDefaultFolder(Long folderId) {
        Folder folder = Folder.builder()
            .folderId(folderId)
            .name("Folder " + folderId)
            .folderType("USER")
            .isSystemFolder(false)
            .build();
        
        return folderRepository.save(folder);
    }
    
    @Override
    public String getFileType() {
        return "MESSAGES";
    }
}
