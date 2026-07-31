package com.sal.domain;

/**
 * Supported compression types.
 */
public enum CompressionType {
    NONE("No compression"),
    GZIP("GZIP compression"),
    ZSTD("Zstandard compression"),
    LZ4("LZ4 compression");

    private final String description;

    CompressionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
