package com.ofw.repository;

import com.ofw.model.entity.UploadSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UploadSessionRepository extends JpaRepository<UploadSession, Long> {
    
    Optional<UploadSession> findBySessionId(String sessionId);
}
