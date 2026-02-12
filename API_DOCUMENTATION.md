# OFW Server API Documentation

Complete REST API documentation for OFW Server.

## Base URL

```
http://localhost:8080
```

## Authentication

Currently, the API does not require authentication. In production, add authentication headers:

```
Authorization: Bearer <token>
ofw-client: WebApplication
ofw-version: 1.0.0
```

---

## Folders API

### Get All Folders

Get all message folders (system and user folders).

**Endpoint:** `GET /pub/v1/messageFolders`

**Query Parameters:**
- `includeFolderCounts` (boolean, optional) - Include message counts (default: false)

**Example Request:**
```bash
curl "http://localhost:8080/pub/v1/messageFolders?includeFolderCounts=true"
```

**Example Response:**
```json
{
  "systemFolders": [
    {
      "id": 1,
      "name": "Inbox",
      "folderType": "INBOX",
      "folderOrder": 1,
      "unreadMessageCount": 5,
      "totalMessageCount": 45
    },
    {
      "id": 2,
      "name": "Action Items",
      "folderType": "ACTION_ITEMS",
      "folderOrder": 2,
      "unreadMessageCount": 2,
      "totalMessageCount": 12
    }
  ],
  "userFolders": [
    {
      "id": 100,
      "name": "School",
      "folderType": "USER",
      "folderOrder": 10,
      "unreadMessageCount": 1,
      "totalMessageCount": 8
    }
  ]
}
```

### Get Single Folder

Get details for a specific folder.

**Endpoint:** `GET /pub/v1/messageFolders/{folderId}`

**Path Parameters:**
- `folderId` (number, required) - Folder ID

**Query Parameters:**
- `includeFolderCounts` (boolean, optional) - Include message counts

**Example Request:**
```bash
curl "http://localhost:8080/pub/v1/messageFolders/1?includeFolderCounts=true"
```

**Example Response:**
```json
{
  "id": 1,
  "name": "Inbox",
  "folderType": "INBOX",
  "folderOrder": 1,
  "unreadMessageCount": 5,
  "totalMessageCount": 45
}
```

---

## Messages API

### Get Messages (Paginated)

Get paginated list of messages.

**Endpoint:** `GET /pub/v3/messages`

**Query Parameters:**
- `folder` (number, optional) - Filter by folder ID
- `page` (number, optional) - Page number, 0-indexed (default: 0)
- `size` (number, optional) - Page size (default: 25)
- `sort` (string, optional) - Sort field (default: "messageDate")
- `sortDirection` (string, optional) - "ASC" or "DESC" (default: "DESC")

**Example Request:**
```bash
curl "http://localhost:8080/pub/v3/messages?folder=1&page=0&size=10&sort=messageDate&sortDirection=DESC"
```

**Example Response:**
```json
{
  "data": [
    {
      "id": 123456789,
      "folder": 1,
      "subject": "Re: Doctor appointment",
      "preview": "The appointment is at 3pm...",
      "files": 0,
      "read": false,
      "replied": false,
      "draft": false,
      "canReply": true,
      "author": {
        "userId": 1001101,
        "name": "Jane Smith",
        "firstName": "Jane",
        "lastName": "Smith",
        "displayInitials": "JS",
        "active": true,
        "type": "PARENT",
        "color": "#FF5733"
      },
      "date": {
        "displayDate": "2/12/2026",
        "displayTime": "12:00 AM",
        "dateTime": "2026-02-11T00:00:00",
        "threeCharMonthWeekdayTimeNoYear": "Wed, Feb 12, 12:00 AM"
      },
      "recipients": [
        {
          "user": {
            "userId": 1011010,
            "name": "John Doe",
            "firstName": "John",
            "lastName": "Doe",
            "displayInitials": "JD",
            "active": true,
            "type": "PARENT",
            "color": "#33C4FF"
          }
        }
      ]
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 45,
  "totalPages": 5
}
```

### Get Single Message

Get full message details including body.

**Endpoint:** `GET /pub/v3/messages/{messageId}`

**Path Parameters:**
- `messageId` (number, required) - Message ID

**Example Request:**
```bash
curl "http://localhost:8080/pub/v3/messages/123412345"
```

**Example Response:**
```json
{
  "id": 123456789,
  "folder": 1,
  "subject": "Re: Doctor appointment",
  "body": "The appointment is at 3pm, the doctor requested his medication. Please do not forget it.",
  "preview": "The appointment is at 3pm...",
  "files": 2,
  "read": false,
  "replied": false,
  "draft": false,
  "canReply": true,
  "author": {
    "userId": 1001101,
    "name": "Jane Smith",
    "firstName": "Jane",
    "lastName": "Smith",
    "displayInitials": "JS",
    "active": true,
    "type": "PARENT",
    "color": "#FF5733"
  },
  "date": {
    "displayDate": "2/12/2026",
    "displayTime": "12:00 AM",
    "dateTime": "2026-02-12T00:00:00",
    "threeCharMonthWeekdayTimeNoYear": "Wed, Feb 12, 12:00 AM"
  },
  "recipients": [
    {
      "user": {
        "userId": 1011010,
        "name": "Kherry Zamore"
      }
    }
  ],
  "attachments": [
    {
      "name": "photo.jpg",
      "size": 45321,
      "contentType": "image/jpeg"
    },
    {
      "name": "document.pdf",
      "size": 123456,
      "contentType": "application/pdf"
    }
  ]
}
```

### Mark Message as Read

Mark a message as read.

**Endpoint:** `PUT /pub/v3/messages/{messageId}/read`

**Path Parameters:**
- `messageId` (number, required) - Message ID

**Example Request:**
```bash
curl -X PUT "http://localhost:8080/pub/v3/messages/123412345/read"
```

**Response:** `200 OK` (empty body)

### Mark Message as Unread

Mark a message as unread.

**Endpoint:** `PUT /pub/v3/messages/{messageId}/unread`

**Path Parameters:**
- `messageId` (number, required) - Message ID

**Example Request:**
```bash
curl -X PUT "http://localhost:8080/pub/v3/messages/123412345/unread"
```

**Response:** `200 OK` (empty body)

### Delete Message

Delete a message.

**Endpoint:** `DELETE /pub/v3/messages/{messageId}`

**Path Parameters:**
- `messageId` (number, required) - Message ID

**Example Request:**
```bash
curl -X DELETE "http://localhost:8080/pub/v3/messages/123412345"
```

**Response:** `204 No Content`

---

## Upload API

### Upload Debug Data

Upload debug data files from Python client.

**Endpoint:** `POST /api/v1/upload/debug`

**Content-Type:** `multipart/form-data`

**Form Fields:**
- `files` (file[], required) - One or more JSON files
- `userId` (number, optional) - User ID to associate with upload
- `notes` (string, optional) - Notes about the upload

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/upload/debug" \
  -F "files=@debug/messages.json" \
  -F "files=@debug/folders.json" \
  -F "userId=1011010" \
  -F "notes=Initial data load"
```

**Example Response:**
```json
{
  "sessionId": "12345678-1234-1234-1234-123456789abc",
  "status": "COMPLETED",
  "message": "Processed 2 files, created 45 records",
  "filesProcessed": 2,
  "recordsCreated": 45,
  "errors": 0,
  "errorMessages": []
}
```

### Health Check

Check service status.

**Endpoint:** `GET /api/v1/upload/health`

**Example Request:**
```bash
curl "http://localhost:8080/api/v1/upload/health"
```

**Response:** `200 OK` with text "Upload service is running"

---

## Error Responses

All errors follow a standard format:

```json
{
  "timestamp": "2026-02-12T10:30:45",
  "status": 404,
  "error": "Not Found",
  "message": "Message not found: 999999",
  "path": "/pub/v3/messages/999999"
}
```

**Common HTTP Status Codes:**
- `200 OK` - Success
- `204 No Content` - Success (no body)
- `400 Bad Request` - Invalid request
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## Testing with Python

### Get Folders
```python
import requests

response = requests.get(
    'http://localhost:8080/pub/v1/messageFolders',
    params={'includeFolderCounts': 'true'}
)

folders = response.json()
print(f"System folders: {len(folders['systemFolders'])}")
```

### Get Messages
```python
import requests

response = requests.get(
    'http://localhost:8080/pub/v3/messages',
    params={
        'folder': 1,
        'page': 0,
        'size': 10,
        'sort': 'messageDate',
        'sortDirection': 'DESC'
    }
)

messages = response.json()
print(f"Total messages: {messages['totalElements']}")
for msg in messages['data']:
    print(f"- {msg['subject']} (from {msg['author']['name']})")
```

### Get Single Message
```python
import requests

message_id = 123412345
response = requests.get(
    f'http://localhost:8080/pub/v3/messages/{message_id}'
)

message = response.json()
print(f"Subject: {message['subject']}")
print(f"Body: {message['body']}")
print(f"From: {message['author']['name']}")
```

### Mark as Read
```python
import requests

message_id = 123412345
response = requests.put(
    f'http://localhost:8080/pub/v3/messages/{message_id}/read'
)

print(f"Status: {response.status_code}")  # Should be 200
```

---

## Complete Workflow Example

```python
import requests

base_url = 'http://localhost:8080'

# 1. Get all folders with counts
folders = requests.get(
    f'{base_url}/pub/v1/messageFolders',
    params={'includeFolderCounts': 'true'}
).json()

inbox = folders['systemFolders'][0]  # First folder (Inbox)
print(f"Inbox has {inbox['unreadMessageCount']} unread messages")

# 2. Get messages from Inbox
messages = requests.get(
    f'{base_url}/pub/v3/messages',
    params={
        'folder': inbox['id'],
        'page': 0,
        'size': 10
    }
).json()

print(f"\nShowing {len(messages['data'])} of {messages['totalElements']} messages:")

# 3. Display messages
for msg in messages['data']:
    status = '✓' if msg['read'] else '✗'
    print(f"{status} {msg['subject']} - {msg['author']['name']}")

# 4. Read first unread message
unread = next((m for m in messages['data'] if not m['read']), None)

if unread:
    # Get full message
    full_msg = requests.get(
        f'{base_url}/pub/v3/messages/{unread["id"]}'
    ).json()
    
    print(f"\n--- Message ---")
    print(f"From: {full_msg['author']['name']}")
    print(f"Subject: {full_msg['subject']}")
    print(f"Body: {full_msg['body']}")
    
    # Mark as read
    requests.put(f'{base_url}/pub/v3/messages/{unread["id"]}/read')
    print("\nMarked as read!")
```

---

## Rate Limiting

Currently no rate limiting is implemented. For production, consider adding rate limiting to prevent abuse.

## Versioning

API versions are indicated in the URL path:
- `/pub/v1/` - Version 1 (folders)
- `/pub/v3/` - Version 3 (messages)
- `/api/v1/` - Version 1 (upload/admin)

## CORS

CORS is not currently configured. Add CORS configuration for frontend access:

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
```

---

## Support

For issues or questions, consult the main README.md or contact the development team.
