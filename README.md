# Storage Abstraction Layer (SAL) Service

Enterprise storage abstraction layer providing a unified API for storing, retrieving, versioning, searching, auditing, and governing binary content independently of the underlying storage technology.

## Features

- **Unified API**: Single interface for all storage operations
- **Pluggable Storage Providers**: File System, Database BLOB, S3 (planned), REST (planned)
- **Version Management**: Multiple versions per object, one latest version
- **Checksum Verification**: SHA-256 integrity checks on upload/download
- **Compression**: Automatic GZIP compression for large files
- **Audit History**: Append-only history for all metadata changes
- **Search**: Flexible search with pagination

## Technology Stack

- **Java 17+**
- **Spring Boot 3.x**
- **PostgreSQL** (adaptable to Oracle)
- **JdbcTemplate** (no ORM)
- **Maven**

## Getting Started

### Prerequisites

- Java 17+
- PostgreSQL 14+
- Maven 3.8+

### Database Setup

```bash
# Create database
docker run -d --name sal-pg \
  -e POSTGRES_DB=sal_db \
  -e POSTGRES_USER=sal_user \
  -e POSTGRES_PASSWORD=sal_password \
  -p 5433:5432 \
  postgres:15
```

### Build and Run

```bash
cd ~/Projects/sal-service

# Build
mvn clean package -DskipTests

# Run
java -jar target/sal-service-1.0.0-SNAPSHOT.jar
```

Service runs on **port 8081** (to avoid conflict with bulk-processing-service).

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/sal/objects` | Upload file (multipart) |
| POST | `/api/v1/sal/objects/upload` | Upload with JSON + base64 |
| GET | `/api/v1/sal/objects/{uuid}` | Get object info (latest) |
| GET | `/api/v1/sal/objects/{uuid}/content` | Download content (latest) |
| GET | `/api/v1/sal/objects/{uuid}/versions` | List all versions |
| GET | `/api/v1/sal/objects/{uuid}/versions/{v}` | Get version info |
| GET | `/api/v1/sal/objects/{uuid}/versions/{v}/content` | Download version |
| DELETE | `/api/v1/sal/objects/{uuid}/versions/{v}` | Delete version |
| POST | `/api/v1/sal/search` | Search objects |
| GET | `/api/v1/sal/objects/{uuid}/history` | Get audit history |

### Upload Example

```bash
# File upload
curl -X POST http://localhost:8081/api/v1/sal/objects \
  -F "file=@document.pdf" \
  -F "name=document.pdf" \
  -F "ownerId=user123" \
  -F "type=PDF" \
  -F "storageType=FILESYSTEM"

# JSON upload
curl -X POST http://localhost:8081/api/v1/sal/objects/upload \
  -H "Content-Type: application/json" \
  -d '{
    "name": "test.txt",
    "ownerId": "user123",
    "type": "TEXT",
    "storageType": "FILESYSTEM",
    "content": "SGVsbG8gV29ybGQh"
  }'
```

### Download Example

```bash
# Download latest version
curl http://localhost:8081/api/v1/sal/objects/{uuid}/content -o file.dat

# Download specific version
curl http://localhost:8081/api/v1/sal/objects/{uuid}/versions/1/content -o file.dat
```

### Search Example

```bash
curl -X POST http://localhost:8081/api/v1/sal/search \
  -H "Content-Type: application/json" \
  -d '{
    "ownerId": "user123",
    "latestOnly": true,
    "type": "PDF",
    "page": 0,
    "size": 20
  }'
```

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     REST Controllers                         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      SAL Service                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Checksum   │  │ Compression │  │ Version Orchestration│  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
           ┌──────────────────┼──────────────────┐
           ▼                  ▼                  ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ Object Repo     │  │ Metadata Repo   │  │ History Repo    │
└─────────────────┘  └─────────────────┘  └─────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│                 Storage Handler Factory                      │
└─────────────────────────────────────────────────────────────┘
           │
           ├──────────────┬──────────────┬──────────────┐
           ▼              ▼              ▼              ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  FileSystem  │  │   Database   │  │     S3       │  │    REST      │
│   Handler    │  │   Handler    │  │   (planned)  │  │  (planned)   │
└──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
```

## Database Schema

### SAL_OBJECT
Logical object with current version pointer.

| Column | Type | Description |
|--------|------|-------------|
| sal_uuid | UUID | Primary key |
| current_version | INTEGER | Current version number |
| owner_id | VARCHAR | Owner identifier |
| version_lock | INTEGER | Optimistic locking |

### SAL_METADATA
Version-specific metadata.

| Column | Type | Description |
|--------|------|-------------|
| sal_uuid | UUID | Object UUID |
| version | INTEGER | Version number |
| sal_name | VARCHAR | Object name |
| status | VARCHAR | PENDING_UPLOAD, AVAILABLE, DELETED, etc. |
| is_latest | BOOLEAN | Is this the latest version |
| storage_type | VARCHAR | FILESYSTEM, DATABASE |
| storage_path | VARCHAR | Path to stored content |
| checksum | VARCHAR | SHA-256 checksum |
| is_compressed | BOOLEAN | Is content compressed |

### SAL_METADATA_HIST
Append-only audit history.

### SAL_BINARY_CONTENT
Binary content for DATABASE storage.

## Storage Providers

### FileSystem (Implemented)
- Stores files in directory structure: `{basePath}/{uuid[0:2]}/{uuid[2:4]}/{uuid}/{version}.dat`
- Configurable base path
- Automatic directory cleanup

### Database (Implemented)
- Stores content as BYTEA in SAL_BINARY_CONTENT
- Best for smaller files or when file system access is limited

### S3 (Planned)
- Amazon S3 or S3-compatible storage (MinIO)

### REST (Planned)
- Proxy to external REST storage API

## Configuration

```yaml
sal:
  default-storage-type: FILESYSTEM
  
  filesystem:
    base-path: ./storage
    create-directories: true
  
  compression:
    enabled: true
    default-type: GZIP
    min-size-bytes: 1024
  
  checksum:
    algorithm: SHA-256
    verify-on-download: true
```

## Version Model

- Each object has a UUID (`sal_uuid`)
- Each version is identified by `(sal_uuid, version)`
- Version numbers are never reused
- Only AVAILABLE versions can be set as latest
- New versions don't become latest until upload succeeds

## Integration with Bulk Processing Service

The SAL service can be used by the Bulk Processing Service to store uploaded files:

```java
// In BulkJobSubmissionService
salClient.upload(request.getFileContent(), "job-" + jobId + ".csv", ownerId);
```

## License

MIT License
