package com.sal.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents binary content stored in the database.
 * Only used when storage_type = 'DATABASE'.
 */
public class SalBinaryContent {

    // Composite Primary Key
    private UUID salUuid;
    private Integer version;

    // Binary content
    private byte[] contentData;
    private Long contentSize;

    // Audit
    private LocalDateTime createdTs;
    private String createdBy;

    public SalBinaryContent() {
        this.createdTs = LocalDateTime.now();
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

    public byte[] getContentData() {
        return contentData;
    }

    public void setContentData(byte[] contentData) {
        this.contentData = contentData;
        if (contentData != null) {
            this.contentSize = (long) contentData.length;
        }
    }

    public Long getContentSize() {
        return contentSize;
    }

    public void setContentSize(Long contentSize) {
        this.contentSize = contentSize;
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
}
