package com.ofw.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_message_id", columnList = "messageId"),
    @Index(name = "idx_folder", columnList = "folder_id"),
    @Index(name = "idx_author", columnList = "author_id"),
    @Index(name = "idx_date", columnList = "messageDate"),
    @Index(name = "idx_read", columnList = "isRead")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long messageId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;
    
    @Column(length = 500)
    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String preview;
    
    @Column(columnDefinition = "LONGTEXT")
    private String body;
    
    @Column(name = "is_draft")
    private Boolean isDraft = false;
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @Column(name = "is_replied")
    private Boolean isReplied = false;
    
    @Column(name = "can_reply")
    private Boolean canReply = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "message_recipients",
        joinColumns = @JoinColumn(name = "message_id"),
        inverseJoinColumns = @JoinColumn(name = "recipient_user_id")
    )
    @Builder.Default
    private Set<User> recipients = new HashSet<>();
    
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Attachment> attachments = new HashSet<>();
    
    @Column(name = "message_date", nullable = false)
    private LocalDateTime messageDate;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Add a recipient to this message.
     */
    public void addRecipient(User user) {
        recipients.add(user);
    }
    
    /**
     * Add an attachment to this message.
     */
    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
        attachment.setMessage(this);
    }
}
