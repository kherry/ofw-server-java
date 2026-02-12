package com.ofw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Main Spring Boot application class for OFW Server.
 * 
 * Architecture:
 * - Entity Layer: JPA entities (User, Message, Folder, etc.)
 * - Repository Layer: Spring Data JPA repositories
 * - Service Layer: Business logic (UploadService, FileProcessors)
 * - Controller Layer: REST API endpoints
 * 
 * Design Patterns Used:
 * - Repository Pattern: Data access abstraction
 * - Strategy Pattern: FileProcessor implementations
 * - Facade Pattern: UploadService simplifies complex operations
 * - Builder Pattern: Entity and DTO construction
 * - DTO Pattern: Separation of API and domain models
 */
@SpringBootApplication
public class OfwServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OfwServerApplication.class, args);
    }
    
    /**
     * Configure ObjectMapper for JSON processing.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
