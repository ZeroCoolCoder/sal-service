package com.sal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for SAL client.
 */
@ConfigurationProperties(prefix = "sal.client")
public class SalClientProperties {

    /**
     * Client mode: "http" for REST API, "embedded" for direct library access.
     */
    private String mode = "http";

    /**
     * Base URL for HTTP mode (e.g., "http://localhost:8081").
     */
    private String baseUrl = "http://localhost:8081";

    /**
     * Default owner ID to use for uploads.
     */
    private String defaultOwnerId;

    /**
     * Default storage type (FILESYSTEM, DATABASE).
     */
    private String defaultStorageType = "FILESYSTEM";

    /**
     * Whether to compress files by default.
     */
    private boolean compressFiles = true;

    /**
     * Connection timeout in milliseconds.
     */
    private int connectTimeoutMs = 5000;

    /**
     * Read timeout in milliseconds.
     */
    private int readTimeoutMs = 30000;

    /**
     * Maximum retry attempts for failed operations.
     */
    private int maxRetries = 3;

    /**
     * Retry delay in milliseconds.
     */
    private long retryDelayMs = 1000;

    // Getters and Setters
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDefaultOwnerId() {
        return defaultOwnerId;
    }

    public void setDefaultOwnerId(String defaultOwnerId) {
        this.defaultOwnerId = defaultOwnerId;
    }

    public String getDefaultStorageType() {
        return defaultStorageType;
    }

    public void setDefaultStorageType(String defaultStorageType) {
        this.defaultStorageType = defaultStorageType;
    }

    public boolean isCompressFiles() {
        return compressFiles;
    }

    public void setCompressFiles(boolean compressFiles) {
        this.compressFiles = compressFiles;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    public boolean isHttpMode() {
        return "http".equalsIgnoreCase(mode);
    }

    public boolean isEmbeddedMode() {
        return "embedded".equalsIgnoreCase(mode);
    }
}
