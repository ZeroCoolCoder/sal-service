package com.sal.domain;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents metadata for a specific version of a storage object.
 * One row per version with full metadata including status and storage location.
 */
public class SalMetadata {

    // Primary Key
    private UUID salUuid;
    private Integer version;

    // Object metadata
    private String salName;
    private String salDescription;
    private String salType;
    private JsonNode salMetadata;

    // Size information
    private Long sizeInBytes;
    private Long storedSizeInBytes;

    // Integrity
    private String checksum;
    private String checksumAlgorithm;

    // Status
    private String status;
    private Boolean isLatest;

    // Compression
    private Boolean isCompressed;
    private String compressionType;

    // Storage location
    private String storageType;
    private String storagePath;

    // Ownership
    private String ownerId;

    // Audit fields
    private String lstModChgCd;
    private String lstModUser;
    private LocalDateTime lstModTs;
    private LocalDateTime createdTs;
    private String createdBy;

    public SalMetadata() {
        this.status = ObjectStatus.PENDING_UPLOAD.name();
        this.isLatest = false;
        this.isCompressed = false;
        this.checksumAlgorithm = "SHA-256";
        this.createdTs = LocalDateTime.now();
        this.lstModTs = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getSalUuid() {
        return salUuid;
    }

    public void setSalUuid(UUID salUuid) {
        this.salUuid = salUuid;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getSalName() {
        return salName;
    }

    public void setSalName(String salName) {
        this.salName = salName;
    }

    public String getSalDescription() {
        return salDescription;
    }

    public void setSalDescription(String salDescription) {
        this.salDescription = salDescription;
    }

    public String getSalType() {
        return salType;
    }

    public void setSalType(String salType) {
        this.salType = salType;
    }

    public JsonNode getSalMetadata() {
        return salMetadata;
    }

    public void setSalMetadata(JsonNode salMetadata) {
        this.salMetadata = salMetadata;
    }

    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public Long getStoredSizeInBytes() {
        return storedSizeInBytes;
    }

    public void setStoredSizeInBytes(Long storedSizeInBytes) {
        this.storedSizeInBytes = storedSizeInBytes;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    public void setChecksumAlgorithm(String checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatus(ObjectStatus status) {
        this.status = status.name();
    }

    public ObjectStatus getStatusEnum() {
        return ObjectStatus.valueOf(status);
    }

    public Boolean getIsLatest() {
        return isLatest;
    }

    public void setIsLatest(Boolean isLatest) {
        this.isLatest = isLatest;
    }

    public Boolean getIsCompressed() {
        return isCompressed;
    }

    public void setIsCompressed(Boolean isCompressed) {
        this.isCompressed = isCompressed;
    }

    public String getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType.name();
    }

    public StorageType getStorageTypeEnum() {
        return StorageType.valueOf(storageType);
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getLstModChgCd() {
        return lstModChgCd;
    }

    public void setLstModChgCd(String lstModChgCd) {
        this.lstModChgCd = lstModChgCd;
    }

    public String getLstModUser() {
        return lstModUser;
    }

    public void setLstModUser(String lstModUser) {
        this.lstModUser = lstModUser;
    }

    public LocalDateTime getLstModTs() {
        return lstModTs;
    }

    public void setLstModTs(LocalDateTime lstModTs) {
        this.lstModTs = lstModTs;
    }

    public LocalDateTime getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(LocalDateTime createdTs) {
        this.createdTs = createdTs;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Check if this version can be downloaded.
     */
    public boolean canDownload() {
        return ObjectStatus.valueOf(status).canDownload();
    }

    /**
     * Check if this version can be set as latest.
     */
    public boolean canBeLatest() {
        return ObjectStatus.valueOf(status).canBeLatest();
    }
}
