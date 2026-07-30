package com.sal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for SAL service.
 */
@Component
@ConfigurationProperties(prefix = "sal")
public class SalProperties {

    private String defaultStorageType = "FILESYSTEM";
    private FileSystemConfig filesystem = new FileSystemConfig();
    private DatabaseConfig database = new DatabaseConfig();
    private CompressionConfig compression = new CompressionConfig();
    private ChecksumConfig checksum = new ChecksumConfig();
    private VersioningConfig versioning = new VersioningConfig();

    // Getters and Setters
    public String getDefaultStorageType() {
        return defaultStorageType;
    }

    public void setDefaultStorageType(String defaultStorageType) {
        this.defaultStorageType = defaultStorageType;
    }

    public FileSystemConfig getFilesystem() {
        return filesystem;
    }

    public void setFilesystem(FileSystemConfig filesystem) {
        this.filesystem = filesystem;
    }

    public DatabaseConfig getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseConfig database) {
        this.database = database;
    }

    public CompressionConfig getCompression() {
        return compression;
    }

    public void setCompression(CompressionConfig compression) {
        this.compression = compression;
    }

    public ChecksumConfig getChecksum() {
        return checksum;
    }

    public void setChecksum(ChecksumConfig checksum) {
        this.checksum = checksum;
    }

    public VersioningConfig getVersioning() {
        return versioning;
    }

    public void setVersioning(VersioningConfig versioning) {
        this.versioning = versioning;
    }

    /**
     * File system storage configuration.
     */
    public static class FileSystemConfig {
        private String basePath = "./storage";
        private boolean createDirectories = true;

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }

        public boolean isCreateDirectories() {
            return createDirectories;
        }

        public void setCreateDirectories(boolean createDirectories) {
            this.createDirectories = createDirectories;
        }
    }

    /**
     * Database storage configuration.
     */
    public static class DatabaseConfig {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * Compression configuration.
     */
    public static class CompressionConfig {
        private boolean enabled = true;
        private String defaultType = "GZIP";
        private long minSizeBytes = 1024;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getDefaultType() {
            return defaultType;
        }

        public void setDefaultType(String defaultType) {
            this.defaultType = defaultType;
        }

        public long getMinSizeBytes() {
            return minSizeBytes;
        }

        public void setMinSizeBytes(long minSizeBytes) {
            this.minSizeBytes = minSizeBytes;
        }
    }

    /**
     * Checksum configuration.
     */
    public static class ChecksumConfig {
        private String algorithm = "SHA-256";
        private boolean verifyOnDownload = true;

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public boolean isVerifyOnDownload() {
            return verifyOnDownload;
        }

        public void setVerifyOnDownload(boolean verifyOnDownload) {
            this.verifyOnDownload = verifyOnDownload;
        }
    }

    /**
     * Versioning configuration.
     */
    public static class VersioningConfig {
        private int maxVersionsPerObject = 100;
        private boolean autoCleanupOldVersions = false;

        public int getMaxVersionsPerObject() {
            return maxVersionsPerObject;
        }

        public void setMaxVersionsPerObject(int maxVersionsPerObject) {
            this.maxVersionsPerObject = maxVersionsPerObject;
        }

        public boolean isAutoCleanupOldVersions() {
            return autoCleanupOldVersions;
        }

        public void setAutoCleanupOldVersions(boolean autoCleanupOldVersions) {
            this.autoCleanupOldVersions = autoCleanupOldVersions;
        }
    }
}
