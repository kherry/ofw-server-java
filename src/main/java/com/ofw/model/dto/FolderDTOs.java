package com.ofw.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for folder information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderDTO {
    private Long id;
    private String name;
    private String folderType;
    private Integer folderOrder;
    private Integer unreadMessageCount;
    private Integer totalMessageCount;
}

/**
 * DTO for folders response (system and user folders).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoldersResponseDTO {
    @Builder.Default
    private List<FolderDTO> systemFolders = new ArrayList<>();
    @Builder.Default
    private List<FolderDTO> userFolders = new ArrayList<>();
}
