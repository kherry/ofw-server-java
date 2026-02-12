-- OFW Server Database Schema
-- MySQL 8.0+

DROP DATABASE IF EXISTS ofw_db;
CREATE DATABASE ofw_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ofw_db;

-- Users table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    display_initials VARCHAR(10),
    avatar_color VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    user_type VARCHAR(50),
    language_locale VARCHAR(10) DEFAULT 'en-US',
    time_zone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB;

-- Folders table
CREATE TABLE folders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    folder_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    folder_type VARCHAR(50),
    folder_order INT DEFAULT 0,
    is_system_folder BOOLEAN DEFAULT FALSE,
    owner_user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_folder_id (folder_id),
    INDEX idx_owner (owner_user_id),
    INDEX idx_type (folder_type),
    FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Messages table
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id BIGINT NOT NULL UNIQUE,
    folder_id BIGINT NOT NULL,
    subject VARCHAR(500),
    preview TEXT,
    body LONGTEXT,
    is_draft BOOLEAN DEFAULT FALSE,
    is_read BOOLEAN DEFAULT FALSE,
    is_replied BOOLEAN DEFAULT FALSE,
    can_reply BOOLEAN DEFAULT TRUE,
    author_user_id BIGINT NOT NULL,
    message_date DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_message_id (message_id),
    INDEX idx_folder (folder_id),
    INDEX idx_author (author_user_id),
    INDEX idx_date (message_date),
    INDEX idx_read (is_read),
    FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE CASCADE,
    FOREIGN KEY (author_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Message recipients table (many-to-many)
CREATE TABLE message_recipients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id BIGINT NOT NULL,
    recipient_user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_message (message_id),
    INDEX idx_recipient (recipient_user_id),
    UNIQUE KEY unique_message_recipient (message_id, recipient_user_id),
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Attachments table
CREATE TABLE attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    file_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_message (message_id),
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Authentication tokens table
CREATE TABLE auth_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(1000) NOT NULL UNIQUE,
    token_type VARCHAR(50) DEFAULT 'BEARER',
    expires_at DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_token (token),
    INDEX idx_user (user_id),
    INDEX idx_expires (expires_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Upload sessions table (for tracking bulk uploads)
CREATE TABLE upload_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(100) NOT NULL UNIQUE,
    uploaded_by_user_id BIGINT,
    status VARCHAR(50) DEFAULT 'IN_PROGRESS',
    total_files INT DEFAULT 0,
    processed_files INT DEFAULT 0,
    error_count INT DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    INDEX idx_session_id (session_id),
    INDEX idx_status (status),
    FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Upload file records (track individual files in upload)
CREATE TABLE upload_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    status VARCHAR(50) DEFAULT 'PENDING',
    records_created INT DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    INDEX idx_session (session_id),
    INDEX idx_status (status),
    FOREIGN KEY (session_id) REFERENCES upload_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Folder statistics (denormalized for performance)
CREATE TABLE folder_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    folder_id BIGINT NOT NULL UNIQUE,
    total_message_count INT DEFAULT 0,
    unread_message_count INT DEFAULT 0,
    last_message_date DATETIME NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Insert default system folders
INSERT INTO users (user_id, username, first_name, last_name, display_initials, avatar_color, user_type) 
VALUES (0, 'system', 'System', 'User', 'SYS', '#000000', 'SYSTEM');

INSERT INTO folders (folder_id, name, folder_type, folder_order, is_system_folder, owner_user_id) VALUES
(1, 'Inbox', 'INBOX', 1, TRUE, 1),
(2, 'Action Items', 'ACTION_ITEMS', 2, TRUE, 1),
(3, 'Notifications', 'SYSTEM_MESSAGES', 3, TRUE, 1),
(4, 'Sent', 'SENT', 4, TRUE, 1),
(5, 'Drafts', 'DRAFTS', 5, TRUE, 1),
(6, 'Trash', 'TRASH', 6, TRUE, 1);

-- Initialize folder statistics
INSERT INTO folder_statistics (folder_id, total_message_count, unread_message_count)
SELECT id, 0, 0 FROM folders;
