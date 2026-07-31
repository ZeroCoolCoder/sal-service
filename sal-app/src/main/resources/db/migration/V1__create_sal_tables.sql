-- ============================================================================
-- Storage Abstraction Layer (SAL) - Database Schema
-- Version: 1.0
-- Database: PostgreSQL (adaptable to Oracle)
-- ============================================================================

-- ============================================================================
-- SAL_OBJECT
-- ============================================================================
-- Purpose: Logical object representing a stored item.
-- Contains current version pointer and concurrency control.
-- ============================================================================

CREATE TABLE sal_object (
    -- Primary Key (UUID)
    sal_uuid                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Current version pointer
    current_version         INTEGER NOT NULL DEFAULT 1,
    
    -- Ownership
    owner_id                VARCHAR(100) NOT NULL,
    
    -- Audit fields
    created_ts              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(100) NOT NULL,
    lst_mod_ts              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lst_mod_user            VARCHAR(100) NOT NULL,
    
    -- Optimistic locking
    version_lock            INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_sal_object_owner ON sal_object(owner_id);
CREATE INDEX idx_sal_object_created ON sal_object(created_ts);

COMMENT ON TABLE sal_object IS 'Logical storage object with version tracking';
COMMENT ON COLUMN sal_object.sal_uuid IS 'Unique identifier for the storage object';
COMMENT ON COLUMN sal_object.current_version IS 'Pointer to the current/latest version number';
COMMENT ON COLUMN sal_object.version_lock IS 'Optimistic locking version for concurrency control';


-- ============================================================================
-- SAL_METADATA
-- ============================================================================
-- Purpose: Stores metadata for each version of an object.
-- One row per version with full metadata including status and storage location.
-- ============================================================================

CREATE TABLE sal_metadata (
    -- Composite Primary Key
    sal_uuid                UUID NOT NULL,
    version                 INTEGER NOT NULL,
    
    -- Object metadata
    sal_name                VARCHAR(500) NOT NULL,
    sal_description         VARCHAR(2000),
    sal_type                VARCHAR(100),
    sal_metadata            JSONB,
    
    -- Size information
    size_in_bytes           BIGINT NOT NULL,
    stored_size_in_bytes    BIGINT,
    
    -- Integrity
    checksum                VARCHAR(128),
    checksum_algorithm      VARCHAR(20) DEFAULT 'SHA-256',
    
    -- Status
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING_UPLOAD',
    is_latest               BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Compression
    is_compressed           BOOLEAN NOT NULL DEFAULT FALSE,
    compression_type        VARCHAR(20),
    
    -- Storage location
    storage_type            VARCHAR(20) NOT NULL,
    storage_path            VARCHAR(1000),
    
    -- Ownership
    owner_id                VARCHAR(100) NOT NULL,
    
    -- Audit fields
    lst_mod_chg_cd          VARCHAR(20),
    lst_mod_user            VARCHAR(100) NOT NULL,
    lst_mod_ts              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_ts              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(100) NOT NULL,
    
    -- Primary Key
    PRIMARY KEY (sal_uuid, version),
    
    -- Foreign Key
    CONSTRAINT fk_metadata_object FOREIGN KEY (sal_uuid) 
        REFERENCES sal_object(sal_uuid) ON DELETE CASCADE,
    
    -- Status must be valid
    CONSTRAINT chk_metadata_status CHECK (
        status IN ('PENDING_UPLOAD', 'UPLOADING', 'AVAILABLE', 'FAILED', 'DELETED', 'ARCHIVED')
    ),
    
    -- Storage type must be valid
    CONSTRAINT chk_storage_type CHECK (
        storage_type IN ('FILESYSTEM', 'DATABASE', 'S3', 'REST')
    ),
    
    -- Compression consistency
    CONSTRAINT chk_compression CHECK (
        (is_compressed = FALSE AND compression_type IS NULL) OR
        (is_compressed = TRUE AND compression_type IS NOT NULL)
    ),
    
    -- Latest must be AVAILABLE
    CONSTRAINT chk_latest_available CHECK (
        is_latest = FALSE OR status = 'AVAILABLE'
    )
);

-- Indexes for common queries
CREATE INDEX idx_metadata_uuid_latest ON sal_metadata(sal_uuid, is_latest) WHERE is_latest = TRUE;
CREATE INDEX idx_metadata_name ON sal_metadata(sal_name);
CREATE INDEX idx_metadata_owner ON sal_metadata(owner_id);
CREATE INDEX idx_metadata_status ON sal_metadata(status);
CREATE INDEX idx_metadata_type ON sal_metadata(sal_type);
CREATE INDEX idx_metadata_modified ON sal_metadata(lst_mod_ts);
CREATE INDEX idx_metadata_storage_type ON sal_metadata(storage_type);

COMMENT ON TABLE sal_metadata IS 'Version-specific metadata for storage objects';
COMMENT ON COLUMN sal_metadata.status IS 'PENDING_UPLOAD, UPLOADING, AVAILABLE, FAILED, DELETED, ARCHIVED';
COMMENT ON COLUMN sal_metadata.is_latest IS 'Only one version per object can be latest, must be AVAILABLE';
COMMENT ON COLUMN sal_metadata.storage_type IS 'FILESYSTEM, DATABASE, S3, REST';
COMMENT ON COLUMN sal_metadata.storage_path IS 'Provider-specific path to the stored content';


-- ============================================================================
-- SAL_METADATA_HIST
-- ============================================================================
-- Purpose: Append-only history table for audit trail.
-- Stores complete metadata snapshots on every change.
-- ============================================================================

CREATE TABLE sal_metadata_hist (
    -- History Primary Key
    hist_id                 BIGSERIAL PRIMARY KEY,
    
    -- History metadata
    hist_ts                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hist_action             VARCHAR(20) NOT NULL,
    hist_user               VARCHAR(100) NOT NULL,
    hist_reason             VARCHAR(500),
    
    -- Snapshot of sal_metadata fields
    sal_uuid                UUID NOT NULL,
    version                 INTEGER NOT NULL,
    sal_name                VARCHAR(500) NOT NULL,
    sal_description         VARCHAR(2000),
    sal_type                VARCHAR(100),
    sal_metadata            JSONB,
    size_in_bytes           BIGINT NOT NULL,
    stored_size_in_bytes    BIGINT,
    checksum                VARCHAR(128),
    checksum_algorithm      VARCHAR(20),
    status                  VARCHAR(20) NOT NULL,
    is_latest               BOOLEAN NOT NULL,
    is_compressed           BOOLEAN NOT NULL,
    compression_type        VARCHAR(20),
    storage_type            VARCHAR(20) NOT NULL,
    storage_path            VARCHAR(1000),
    owner_id                VARCHAR(100) NOT NULL,
    lst_mod_chg_cd          VARCHAR(20),
    lst_mod_user            VARCHAR(100) NOT NULL,
    lst_mod_ts              TIMESTAMP NOT NULL,
    created_ts              TIMESTAMP NOT NULL,
    created_by              VARCHAR(100) NOT NULL,
    
    -- History action must be valid
    CONSTRAINT chk_hist_action CHECK (
        hist_action IN ('CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', 'SET_LATEST')
    )
);

CREATE INDEX idx_hist_uuid ON sal_metadata_hist(sal_uuid);
CREATE INDEX idx_hist_uuid_version ON sal_metadata_hist(sal_uuid, version);
CREATE INDEX idx_hist_ts ON sal_metadata_hist(hist_ts);
CREATE INDEX idx_hist_user ON sal_metadata_hist(hist_user);
CREATE INDEX idx_hist_action ON sal_metadata_hist(hist_action);

COMMENT ON TABLE sal_metadata_hist IS 'Append-only audit history for metadata changes';
COMMENT ON COLUMN sal_metadata_hist.hist_action IS 'CREATE, UPDATE, DELETE, STATUS_CHANGE, SET_LATEST';


-- ============================================================================
-- SAL_BINARY_CONTENT
-- ============================================================================
-- Purpose: Stores binary content for DATABASE storage provider.
-- Only used when storage_type = 'DATABASE'.
-- ============================================================================

CREATE TABLE sal_binary_content (
    -- Composite Primary Key (matches sal_metadata)
    sal_uuid                UUID NOT NULL,
    version                 INTEGER NOT NULL,
    
    -- Binary content
    content_data            BYTEA NOT NULL,
    content_size            BIGINT NOT NULL,
    
    -- Audit
    created_ts              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(100) NOT NULL,
    
    -- Primary Key
    PRIMARY KEY (sal_uuid, version),
    
    -- Foreign Key to metadata
    CONSTRAINT fk_binary_metadata FOREIGN KEY (sal_uuid, version) 
        REFERENCES sal_metadata(sal_uuid, version) ON DELETE CASCADE
);

COMMENT ON TABLE sal_binary_content IS 'Binary content storage for DATABASE storage provider';
COMMENT ON COLUMN sal_binary_content.content_data IS 'Actual binary content (may be compressed)';


-- ============================================================================
-- Functions for version management
-- ============================================================================

-- Function to get next version number for an object
CREATE OR REPLACE FUNCTION get_next_version(p_sal_uuid UUID)
RETURNS INTEGER AS $$
DECLARE
    v_next_version INTEGER;
BEGIN
    SELECT COALESCE(MAX(version), 0) + 1 INTO v_next_version
    FROM sal_metadata
    WHERE sal_uuid = p_sal_uuid;
    
    RETURN v_next_version;
END;
$$ LANGUAGE plpgsql;

-- Function to ensure only one latest version
CREATE OR REPLACE FUNCTION ensure_single_latest()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_latest = TRUE THEN
        -- Clear any existing latest flag for this object
        UPDATE sal_metadata 
        SET is_latest = FALSE 
        WHERE sal_uuid = NEW.sal_uuid 
          AND version != NEW.version 
          AND is_latest = TRUE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ensure_single_latest
    BEFORE INSERT OR UPDATE OF is_latest ON sal_metadata
    FOR EACH ROW
    WHEN (NEW.is_latest = TRUE)
    EXECUTE FUNCTION ensure_single_latest();
