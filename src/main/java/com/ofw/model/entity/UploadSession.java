package com.ofw.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "upload_sessions", indexes = {
    @Index(name = "idx_session_id", columnList = "sessionId"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private User uploadedBy;
    
    @Column(nullable = false)
    @Builder.Default
    private String status = "IN_PROGRESS";
    
    @Column(name = "total_files")
    @Builder.Default
    private Integer totalFiles = 0;
    
    @Column(name = "processed_files")
    @Builder.Default
    private Integer processedFiles = 0;
    
    @Column(name = "error_count")
    @Builder.Default
    private Integer errorCount = 0;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UploadFile> files = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    /**
     * Mark session as completed.
     */
    public void complete() {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Mark session as failed.
     */
    public void fail(String reason) {
        this.status = "FAILED";
        this.notes = reason;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Increment processed files counter.
     */
    public void incrementProcessed() {
        this.processedFiles++;
    }
    
    /**
     * Increment error counter.
     */
    public void incrementErrors() {
        this.errorCount++;
    }
}
