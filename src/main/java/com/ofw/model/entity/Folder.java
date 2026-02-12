package com.ofw.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "folders", indexes = {
    @Index(name = "idx_folder_id", columnList = "folderId"),
    @Index(name = "idx_owner", columnList = "owner_id"),
    @Index(name = "idx_type", columnList = "folderType")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Folder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long folderId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "folder_type")
    private String folderType;
    
    @Column(name = "folder_order")
    private Integer folderOrder = 0;
    
    @Column(name = "is_system_folder")
    private Boolean isSystemFolder = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
