package com.sal.dto;

import java.time.LocalDateTime;

/**
 * Request DTO for searching objects.
 */
public class SearchRequest {

    private String name;
    private String namePattern;
    private String description;
    private String type;
    private String ownerId;
    private String status;
    private Boolean latestOnly;
    private Boolean compressed;
    private String storageType;
    private LocalDateTime modifiedAfter;
    private LocalDateTime modifiedBefore;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private Long minSize;
    private Long maxSize;

    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "lstModTs";
    private String sortDirection = "DESC";

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamePattern() {
        return namePattern;
    }

    public void setNamePattern(String namePattern) {
        this.namePattern = namePattern;
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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getLatestOnly() {
        return latestOnly;
    }

    public void setLatestOnly(Boolean latestOnly) {
        this.latestOnly = latestOnly;
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

    public LocalDateTime getModifiedAfter() {
        return modifiedAfter;
    }

    public void setModifiedAfter(LocalDateTime modifiedAfter) {
        this.modifiedAfter = modifiedAfter;
    }

    public LocalDateTime getModifiedBefore() {
        return modifiedBefore;
    }

    public void setModifiedBefore(LocalDateTime modifiedBefore) {
        this.modifiedBefore = modifiedBefore;
    }

    public LocalDateTime getCreatedAfter() {
        return createdAfter;
    }

    public void setCreatedAfter(LocalDateTime createdAfter) {
        this.createdAfter = createdAfter;
    }

    public LocalDateTime getCreatedBefore() {
        return createdBefore;
    }

    public void setCreatedBefore(LocalDateTime createdBefore) {
        this.createdBefore = createdBefore;
    }

    public Long getMinSize() {
        return minSize;
    }

    public void setMinSize(Long minSize) {
        this.minSize = minSize;
    }

    public Long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Long maxSize) {
        this.maxSize = maxSize;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
