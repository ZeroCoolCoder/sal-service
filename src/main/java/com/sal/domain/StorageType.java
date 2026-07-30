package com.sal.domain;

/**
 * Supported storage provider types.
 */
public enum StorageType {
    FILESYSTEM("File system storage"),
    DATABASE("Database BLOB storage"),
    S3("Amazon S3 or compatible storage"),
    REST("External REST API storage");

    private final String description;

    StorageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
