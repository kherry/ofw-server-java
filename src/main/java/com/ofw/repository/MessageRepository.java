package com.ofw.repository;

import com.ofw.model.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    Optional<Message> findByMessageId(Long messageId);
    
    Page<Message> findByFolderIdOrderByMessageDateDesc(Long folderId, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.folder.id = :folderId AND m.isRead = false")
    long countUnreadByFolderId(Long folderId);
    
    boolean existsByMessageId(Long messageId);
}
