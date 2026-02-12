package com.ofw.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for message date information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDateDTO {
    private String displayDate;
    private String displayTime;
    private String dateTime;
    private String threeCharMonthWeekdayTimeNoYear;
}

/**
 * DTO for message list item (used in message list views).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageListItemDTO {
    private Long id;
    private Long folder;
    private String subject;
    private String preview;
    private Integer files;
    private Boolean read;
    private Boolean replied;
    private Boolean draft;
    private Boolean canReply;
    private UserDTO author;
    private MessageDateDTO date;
    @Builder.Default
    private List<RecipientDTO> recipients = new ArrayList<>();
}

/**
 * DTO for recipient information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class RecipientDTO {
    private UserDTO user;
}

/**
 * DTO for full message details (includes body).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDetailDTO {
    private Long id;
    private Long folder;
    private String subject;
    private String body;
    private String preview;
    private Integer files;
    private Boolean read;
    private Boolean replied;
    private Boolean draft;
    private Boolean canReply;
    private UserDTO author;
    private MessageDateDTO date;
    @Builder.Default
    private List<RecipientDTO> recipients = new ArrayList<>();
    @Builder.Default
    private List<AttachmentDTO> attachments = new ArrayList<>();
}

/**
 * DTO for attachment information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class AttachmentDTO {
    private String name;
    private Long size;
    private String contentType;
}

/**
 * DTO for paginated messages response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagesResponseDTO {
    @Builder.Default
    private List<MessageListItemDTO> data = new ArrayList<>();
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
}
