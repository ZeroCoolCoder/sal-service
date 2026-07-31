package com.sal.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for uploading content.
 */
public class UploadRequest {

    /**
     * UUID for existing object (new version) or null for new object.
     */
    private UUID salUuid;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private String type;

    private JsonNode metadata;

    @NotNull(message = "Size is required")
    private Long sizeInBytes;

    private String storageType;

    private Boolean compress;

    @NotBlank(message = "Owner ID is required")
    private String ownerId;

    private String userId;

    // Getters and Setters
    public UUID getSalUuid() {
        return salUuid;
    }

    public void setSalUuid(UUID salUuid) {
        this.salUuid = salUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonNode getMetadata() {
        return metadata;
    }

    public void setMetadata(JsonNode metadata) {
        this.metadata = metadata;
    }

    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public Boolean getCompress() {
        return compress;
    }

    public void setCompress(Boolean compress) {
        this.compress = compress;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isNewObject() {
        return salUuid == null;
    }
}
