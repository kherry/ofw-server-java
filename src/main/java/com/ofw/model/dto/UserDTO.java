package com.ofw.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for User data in API responses.
 * Follows DTO pattern to decouple API from database entities.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String name;
    private String firstName;
    private String lastName;
    private String displayInitials;
    private Boolean active;
    private String type;
    private String color;
}
