package com.sal.domain;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a history record for metadata changes.
 * Append-only audit trail with complete metadata snapshots.
 */
public class SalMetadataHist {

    // History Primary Key
    private Long histId;

    // History metadata
    private LocalDateTime histTs;
    private String histAction;
    private String histUser;
    private String histReason;

    // Snapshot of sal_metadata fields
    private UUID salUuid;
    private Integer version;
    private String salName;
    private String salDescription;
    private String salType;
    private JsonNode salMetadata;
    private Long sizeInBytes;
    private Long storedSizeInBytes;
    private String checksum;
    private String checksumAlgorithm;
    private String status;
    private Boolean isLatest;
    private Boolean isCompressed;
    private String compressionType;
    private String storageType;
    private String storagePath;
    private String ownerId;
    private String lstModChgCd;
    private String lstModUser;
    private LocalDateTime lstModTs;
    private LocalDateTime createdTs;
    private String createdBy;

    public SalMetadataHist() {
        this.histTs = LocalDateTime.now();
    }

    /**
     * Create history record from metadata.
     */
    public static SalMetadataHist fromMetadata(SalMetadata metadata, HistoryAction action, 
                                                String user, String reason) {
        SalMetadataHist hist = new SalMetadataHist();
        hist.histAction = action.name();
        hist.histUser = user;
        hist.histReason = reason;
        
        // Copy all fields from metadata
        hist.salUuid = metadata.getSalUuid();
        hist.version = metadata.getVersion();
        hist.salName = metadata.getSalName();
        hist.salDescription = metadata.getSalDescription();
        hist.salType = metadata.getSalType();
        hist.salMetadata = metadata.getSalMetadata();
        hist.sizeInBytes = metadata.getSizeInBytes();
        hist.storedSizeInBytes = metadata.getStoredSizeInBytes();
        hist.checksum = metadata.getChecksum();
        hist.checksumAlgorithm = metadata.getChecksumAlgorithm();
        hist.status = metadata.getStatus();
        hist.isLatest = metadata.getIsLatest();
        hist.isCompressed = metadata.getIsCompressed();
        hist.compressionType = metadata.getCompressionType();
        hist.storageType = metadata.getStorageType();
        hist.storagePath = metadata.getStoragePath();
        hist.ownerId = metadata.getOwnerId();
        hist.lstModChgCd = metadata.getLstModChgCd();
        hist.lstModUser = metadata.getLstModUser();
        hist.lstModTs = metadata.getLstModTs();
        hist.createdTs = metadata.getCreatedTs();
        hist.createdBy = metadata.getCreatedBy();
        
        return hist;
    }

    // Getters and Setters
    public Long getHistId() {
        return histId;
    }

    public void setHistId(Long histId) {
        this.histId = histId;
    }

    public LocalDateTime getHistTs() {
        return histTs;
    }

    public void setHistTs(LocalDateTime histTs) {
        this.histTs = histTs;
    }

    public String getHistAction() {
        return histAction;
    }

    public void setHistAction(String histAction) {
        this.histAction = histAction;
    }

    public void setHistAction(HistoryAction action) {
        this.histAction = action.name();
    }

    public String getHistUser() {
        return histUser;
    }

    public void setHistUser(String histUser) {
        this.histUser = histUser;
    }

    public String getHistReason() {
        return histReason;
    }

    public void setHistReason(String histReason) {
        this.histReason = histReason;
    }

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
}
