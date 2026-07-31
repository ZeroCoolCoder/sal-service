package com.sal.handler.impl;

import com.sal.config.SalProperties;
import com.sal.domain.StorageType;
import com.sal.exception.StorageException;
import com.sal.handler.spi.StorageHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.UUID;

/**
 * Amazon S3 (or S3-compatible) storage handler.
 * 
 * Stores content in S3 buckets with versioned keys based on UUID.
 * Supports AWS S3, MinIO, and other S3-compatible storage systems.
 */
@Component
@ConditionalOnProperty(name = "sal.s3.enabled", havingValue = "true")
public class S3StorageHandler implements StorageHandler {

    private static final Logger logger = LoggerFactory.getLogger(S3StorageHandler.class);

    private final SalProperties properties;
    private S3Client s3Client;

    public S3StorageHandler(SalProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        SalProperties.S3Config s3Config = properties.getS3();
        
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(s3Config.getRegion()));

        // Configure credentials
        AwsCredentialsProvider credentialsProvider;
        if (s3Config.getAccessKeyId() != null && s3Config.getSecretAccessKey() != null) {
            credentialsProvider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                            s3Config.getAccessKeyId(),
                            s3Config.getSecretAccessKey()
                    )
            );
        } else {
            credentialsProvider = DefaultCredentialsProvider.create();
        }
        builder.credentialsProvider(credentialsProvider);

        // Configure custom endpoint (for MinIO, LocalStack, etc.)
        if (s3Config.getEndpoint() != null && !s3Config.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(s3Config.getEndpoint()));
        }

        // Configure path-style access (required for MinIO)
        if (s3Config.isPathStyleAccess()) {
            builder.forcePathStyle(true);
        }

        this.s3Client = builder.build();
        
        logger.info("S3 storage handler initialized: region={}, bucket={}, endpoint={}", 
                s3Config.getRegion(), 
                s3Config.getBucket(),
                s3Config.getEndpoint() != null ? s3Config.getEndpoint() : "AWS default");
        
        // Verify bucket exists
        verifyBucket(s3Config.getBucket());
    }

    @PreDestroy
    public void cleanup() {
        if (s3Client != null) {
            s3Client.close();
            logger.info("S3 client closed");
        }
    }

    private void verifyBucket(String bucket) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            logger.info("S3 bucket verified: {}", bucket);
        } catch (NoSuchBucketException e) {
            logger.error("S3 bucket does not exist: {}", bucket);
            throw new StorageException("S3", "init", "Bucket does not exist: " + bucket);
        } catch (S3Exception e) {
            logger.warn("Could not verify S3 bucket (may still work): {}", e.getMessage());
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.S3;
    }

    @Override
    public String store(UUID salUuid, Integer version, InputStream inputStream, long contentSize) {
        String key = buildKey(salUuid, version);
        String bucket = properties.getS3().getBucket();
        
        try {
            // Read content into byte array (required for S3 SDK with known content length)
            byte[] content = IOUtils.toByteArray(inputStream);
            
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentLength((long) content.length)
                    .build();
            
            s3Client.putObject(request, RequestBody.fromBytes(content));
            
            String storagePath = "s3://" + bucket + "/" + key;
            logger.debug("Stored content in S3: {}", storagePath);
            return storagePath;
            
        } catch (IOException e) {
            throw new StorageException("S3", "store",
                    "Failed to read input stream for " + salUuid + " v" + version, e);
        } catch (S3Exception e) {
            throw new StorageException("S3", "store",
                    "Failed to store content in S3 for " + salUuid + " v" + version + ": " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream retrieve(UUID salUuid, Integer version, String storagePath) {
        String key = extractKey(storagePath);
        String bucket = properties.getS3().getBucket();
        
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            
            byte[] content = s3Client.getObject(request, ResponseTransformer.toBytes()).asByteArray();
            return new ByteArrayInputStream(content);
            
        } catch (NoSuchKeyException e) {
            throw new StorageException("S3", "retrieve",
                    "Object not found in S3: " + storagePath);
        } catch (S3Exception e) {
            throw new StorageException("S3", "retrieve",
                    "Failed to retrieve content from S3: " + storagePath + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void streamTo(UUID salUuid, Integer version, String storagePath, OutputStream outputStream) {
        try (InputStream in = retrieve(salUuid, version, storagePath)) {
            IOUtils.copy(in, outputStream);
        } catch (IOException e) {
            throw new StorageException("S3", "streamTo",
                    "Failed to stream content from S3: " + storagePath, e);
        }
    }

    @Override
    public boolean delete(UUID salUuid, Integer version, String storagePath) {
        String key = extractKey(storagePath);
        String bucket = properties.getS3().getBucket();
        
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            
            s3Client.deleteObject(request);
            logger.debug("Deleted object from S3: {}", storagePath);
            return true;
            
        } catch (NoSuchKeyException e) {
            logger.debug("Object already deleted or not found: {}", storagePath);
            return false;
        } catch (S3Exception e) {
            throw new StorageException("S3", "delete",
                    "Failed to delete object from S3: " + storagePath + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(UUID salUuid, Integer version, String storagePath) {
        String key = extractKey(storagePath);
        String bucket = properties.getS3().getBucket();
        
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            
            s3Client.headObject(request);
            return true;
            
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            throw new StorageException("S3", "exists",
                    "Failed to check existence in S3: " + storagePath + ": " + e.getMessage(), e);
        }
    }

    @Override
    public long getStoredSize(UUID salUuid, Integer version, String storagePath) {
        String key = extractKey(storagePath);
        String bucket = properties.getS3().getBucket();
        
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            
            HeadObjectResponse response = s3Client.headObject(request);
            return response.contentLength();
            
        } catch (NoSuchKeyException e) {
            throw new StorageException("S3", "getStoredSize",
                    "Object not found in S3: " + storagePath);
        } catch (S3Exception e) {
            throw new StorageException("S3", "getStoredSize",
                    "Failed to get object size from S3: " + storagePath + ": " + e.getMessage(), e);
        }
    }

    /**
     * Build S3 key using UUID-based path structure.
     * Format: prefix / uuid[0:2] / uuid[2:4] / uuid / version.dat
     */
    private String buildKey(UUID salUuid, Integer version) {
        String uuid = salUuid.toString().replace("-", "");
        String dir1 = uuid.substring(0, 2);
        String dir2 = uuid.substring(2, 4);
        String prefix = properties.getS3().getPrefix();
        
        return prefix + dir1 + "/" + dir2 + "/" + salUuid.toString() + "/" + version + ".dat";
    }

    /**
     * Extract key from storage path.
     * Removes the s3://bucket/ prefix.
     */
    private String extractKey(String storagePath) {
        if (storagePath.startsWith("s3://")) {
            String withoutPrefix = storagePath.substring(5);
            int slashIndex = withoutPrefix.indexOf('/');
            if (slashIndex > 0) {
                return withoutPrefix.substring(slashIndex + 1);
            }
        }
        // Fallback: assume it's already just a key
        return storagePath;
    }
}
