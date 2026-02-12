package com.ofw.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * User entity representing an OFW user.
 * Uses Builder pattern for flexible object creation.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long userId;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    private String email;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "display_initials")
    private String displayInitials;
    
    @Column(name = "avatar_color")
    private String avatarColor;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "user_type")
    private String userType;
    
    @Column(name = "language_locale")
    private String languageLocale = "en-US";
    
    @Column(name = "time_zone")
    private String timeZone;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Get full name of user.
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return username;
    }
}
