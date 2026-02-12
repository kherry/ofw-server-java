package com.ofw.repository;

import com.ofw.model.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    
    Optional<Folder> findByFolderId(Long folderId);
    
    List<Folder> findByIsSystemFolderTrue();
    
    List<Folder> findByOwnerIdOrderByFolderOrderAsc(Long ownerId);
    
    boolean existsByFolderId(Long folderId);
}
