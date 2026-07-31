package com.sal.domain;

/**
 * Status of a storage object version.
 */
public enum ObjectStatus {
    PENDING_UPLOAD("Version reserved, upload not started"),
    UPLOADING("Upload in progress"),
    AVAILABLE("Upload complete, available for download"),
    FAILED("Upload failed"),
    DELETED("Soft deleted"),
    ARCHIVED("Archived to cold storage");

    private final String description;

    ObjectStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this status allows the version to be set as latest.
     */
    public boolean canBeLatest() {
        return this == AVAILABLE;
    }

    /**
     * Check if this status allows download.
     */
    public boolean canDownload() {
        return this == AVAILABLE;
    }
}
