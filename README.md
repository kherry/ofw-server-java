# OFW Server (Java Spring Boot)

Mock server for Our Family Wizard API with database persistence.

## Architecture

### Design Patterns Used

1. **Repository Pattern** - Data access abstraction layer
   - `UserRepository`, `MessageRepository`, `FolderRepository`
   - Decouples business logic from data access

2. **Strategy Pattern** - Pluggable file processors
   - `FileProcessor` interface
   - `MessagesFileProcessor`, `FoldersFileProcessor`
   - Easily extensible for new file types

3. **Facade Pattern** - Simplified upload operations
   - `UploadService` provides simple interface to complex operations
   - Coordinates multiple processors and repositories

4. **Builder Pattern** - Flexible object construction
   - All entities use `@Builder` annotation
   - Clean, readable object creation

5. **DTO Pattern** - API/Domain separation
   - Separate DTOs for API responses
   - Protects domain model from API changes

### Layer Architecture

```
┌─────────────────────────────────────────┐
│         Controller Layer                │
│  (REST API Endpoints)                   │
│  - UploadController                     │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Service Layer                   │
│  (Business Logic)                       │
│  - UploadService (Facade)               │
│  - FileProcessors (Strategy)            │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│       Repository Layer                  │
│  (Data Access)                          │
│  - Spring Data JPA Repositories         │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Entity Layer                    │
│  (Domain Model)                         │
│  - User, Message, Folder, etc.          │
└─────────────────────────────────────────┘
```

## Prerequisites

- Docker and Docker Compose
- OR: Java 17+, Maven 3.8+, MySQL 8.0+

## Quick Start (Docker)

### 1. Start the services

```bash
cd ofw-server-java
docker-compose up -d
```

This will:
- Start MySQL database
- Run schema.sql to create tables
- Build and start the OFW server
- Expose API on http://localhost:8080

### 2. Verify services are running

```bash
# Check containers
docker-compose ps

# Check logs
docker-compose logs ofw-server

# Test health endpoint
curl http://localhost:8080/api/v1/upload/health
```

### 3. Upload debug data

```bash
# From your Python client debug directory
curl -X POST http://localhost:8080/api/v1/upload/debug \
  -F "files=@debug/messages.json" \
  -F "files=@debug/folders.json" \
  -F "userId=1011010" \
  -F "notes=Test upload from Python client"
```

### 4. View uploaded data

```bash
# Connect to MySQL
docker exec -it ofw-mysql mysql -u ofw_user -pofw_password ofw_db

# Query data
SELECT COUNT(*) FROM messages;
SELECT * FROM users;
SELECT * FROM folders;
```

## Manual Setup (Without Docker)

### 1. Setup MySQL

```bash
# Install MySQL 8.0
# Create database and user
mysql -u root -p

CREATE DATABASE ofw_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ofw_user'@'localhost' IDENTIFIED BY 'ofw_password';
GRANT ALL PRIVILEGES ON ofw_db.* TO 'ofw_user'@'localhost';
FLUSH PRIVILEGES;

# Import schema
mysql -u ofw_user -pofw_password ofw_db < schema.sql
```

### 2. Build and run application

```bash
# Build with Maven
mvn clean package

# Run
java -jar target/ofw-server-1.0.0.jar
```

Or use Maven Spring Boot plugin:

```bash
mvn spring-boot:run
```

## API Endpoints

### Upload Debug Data

**POST** `/api/v1/upload/debug`

Upload JSON files from Python client debug directory.

**Request:**
- Content-Type: `multipart/form-data`
- Fields:
  - `files` (required): One or more JSON files
  - `userId` (optional): User ID to associate with upload
  - `notes` (optional): Notes about the upload

**Example:**

```bash
curl -X POST http://localhost:8080/api/v1/upload/debug \
  -F "files=@debug/messages.json" \
  -F "files=@debug/folders.json" \
  -F "files=@debug/localstorage_data.json" \
  -F "userId=1011010" \
  -F "notes=Initial data load"
```

**Response:**

```json
{
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "COMPLETED",
  "message": "Processed 2 files, created 45 records",
  "filesProcessed": 2,
  "recordsCreated": 45,
  "errors": 0,
  "errorMessages": []
}
```

### Health Check

**GET** `/api/v1/upload/health`

Returns service status.

## Supported Files

The server can process these JSON files from the Python client debug directory:

| File | Processor | Creates |
|------|-----------|---------|
| `messages.json` | `MessagesFileProcessor` | Messages, Users, Recipients |
| `folders.json` | `FoldersFileProcessor` | Folders |
| `all_messages.json` | `MessagesFileProcessor` | Messages (all pages) |

## Database Schema

See `schema.sql` for complete schema.

**Main Tables:**
- `users` - User accounts
- `folders` - Message folders (system and user)
- `messages` - Messages
- `message_recipients` - Message recipients (many-to-many)
- `attachments` - File attachments
- `upload_sessions` - Upload tracking
- `upload_files` - Individual file tracking

## Configuration

Edit `src/main/resources/application.yml` or use environment variables:

```yaml
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ofw_db
DB_USER=ofw_user
DB_PASSWORD=ofw_password

# Server
SERVER_PORT=8080

# Uploads
UPLOAD_TEMP_DIR=/tmp/ofw-uploads

# Logging
SHOW_SQL=false
```

## Adding New File Processors

To support additional file types:

1. Create a new processor class:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MyFileProcessor implements FileProcessor {
    
    @Override
    public boolean canProcess(String fileName) {
        return fileName.contains("myfile") && fileName.endsWith(".json");
    }
    
    @Override
    public int process(String fileName, JsonNode jsonData, UploadSession session) {
        // Process the data
        return recordsCreated;
    }
    
    @Override
    public String getFileType() {
        return "MY_FILE_TYPE";
    }
}
```

2. Spring will automatically detect and register it via `@Component`

3. The `UploadService` will use it via the Strategy pattern

## Development

### Running tests

```bash
mvn test
```

### Building

```bash
# Build JAR
mvn clean package

# Skip tests
mvn clean package -DskipTests
```

### IDE Setup

Import as Maven project in:
- IntelliJ IDEA
- Eclipse
- VS Code (with Java extensions)

## Docker Commands

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f ofw-server

# Stop services
docker-compose down

# Rebuild after code changes
docker-compose up -d --build

# Reset database (removes all data)
docker-compose down -v
docker-compose up -d
```

## Troubleshooting

### "Table doesn't exist" errors

Schema may not have loaded. Run manually:

```bash
docker exec -i ofw-mysql mysql -u ofw_user -pofw_password ofw_db < schema.sql
```

### Connection refused

Wait for MySQL to be ready:

```bash
docker-compose logs mysql | grep "ready for connections"
```

### Port already in use

Change port in docker-compose.yml:

```yaml
services:
  ofw-server:
    ports:
      - "8081:8080"  # Use port 8081 instead
```

## Production Considerations

For production deployment:

1. **Security**
   - Change default passwords
   - Use environment variables for secrets
   - Add authentication/authorization
   - Enable HTTPS

2. **Performance**
   - Configure connection pooling
   - Add database indexes
   - Enable query caching
   - Use CDN for static assets

3. **Monitoring**
   - Add Spring Boot Actuator
   - Configure logging aggregation
   - Set up health checks
   - Monitor database performance

4. **Scaling**
   - Use managed database (RDS, Cloud SQL)
   - Add load balancer
   - Container orchestration (Kubernetes)
   - Distributed caching (Redis)

## License

MIT License

## Support

For issues, create a GitHub issue or contact the development team.
