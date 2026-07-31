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
    private S3Config s3 = new S3Config();
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

    public S3Config getS3() {
        return s3;
    }

    public void setS3(S3Config s3) {
        this.s3 = s3;
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

    /**
     * S3 storage configuration.
     */
    public static class S3Config {
        private boolean enabled = false;
        private String region = "us-east-1";
        private String endpoint;  // Custom endpoint for S3-compatible storage (e.g., MinIO)
        private String bucket;
        private String accessKeyId;
        private String secretAccessKey;
        private String prefix = "sal/";  // Key prefix for all objects
        private boolean pathStyleAccess = false;  // Use path-style access (required for MinIO)

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getSecretAccessKey() {
            return secretAccessKey;
        }

        public void setSecretAccessKey(String secretAccessKey) {
            this.secretAccessKey = secretAccessKey;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public boolean isPathStyleAccess() {
            return pathStyleAccess;
        }

        public void setPathStyleAccess(boolean pathStyleAccess) {
            this.pathStyleAccess = pathStyleAccess;
        }
    }
}
