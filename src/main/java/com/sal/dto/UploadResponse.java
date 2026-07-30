package com.sal.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for upload operations.
 */
public class UploadResponse {

    private UUID salUuid;
    private Integer version;
    private String status;
    private String checksum;
    private Long sizeInBytes;
    private Long storedSizeInBytes;
    private Boolean compressed;
    private String storageType;
    private LocalDateTime createdAt;

    public static UploadResponse success(UUID salUuid, Integer version, String checksum,
                                         Long sizeInBytes, Long storedSizeInBytes,
                                         Boolean compressed, String storageType) {
        UploadResponse response = new UploadResponse();
        response.salUuid = salUuid;
        response.version = version;
        response.status = "AVAILABLE";
        response.checksum = checksum;
        response.sizeInBytes = sizeInBytes;
        response.storedSizeInBytes = storedSizeInBytes;
        response.compressed = compressed;
        response.storageType = storageType;
        response.createdAt = LocalDateTime.now();
        return response;
    }

    public static UploadResponse pending(UUID salUuid, Integer version) {
        UploadResponse response = new UploadResponse();
        response.salUuid = salUuid;
        response.version = version;
        response.status = "PENDING_UPLOAD";
        response.createdAt = LocalDateTime.now();
        return response;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
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

    public Boolean getCompressed() {
        return compressed;
    }

    public void setCompressed(Boolean compressed) {
        this.compressed = compressed;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
