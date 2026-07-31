package com.sal.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a logical storage object.
 * Contains current version pointer and concurrency control.
 */
public class SalObject {

    private UUID salUuid;
    private Integer currentVersion;
    private String ownerId;
    private LocalDateTime createdTs;
    private String createdBy;
    private LocalDateTime lstModTs;
    private String lstModUser;
    private Integer versionLock;

    public SalObject() {
        this.currentVersion = 1;
        this.versionLock = 0;
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

    public Integer getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(Integer currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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

    public LocalDateTime getLstModTs() {
        return lstModTs;
    }

    public void setLstModTs(LocalDateTime lstModTs) {
        this.lstModTs = lstModTs;
    }

    public String getLstModUser() {
        return lstModUser;
    }

    public void setLstModUser(String lstModUser) {
        this.lstModUser = lstModUser;
    }

    public Integer getVersionLock() {
        return versionLock;
    }

    public void setVersionLock(Integer versionLock) {
        this.versionLock = versionLock;
    }

    /**
     * Increment version lock for optimistic locking.
     */
    public void incrementVersionLock() {
        this.versionLock = (this.versionLock == null ? 0 : this.versionLock) + 1;
    }
}
