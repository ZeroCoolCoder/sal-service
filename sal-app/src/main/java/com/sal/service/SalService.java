package com.sal.service;

import com.sal.config.SalProperties;
import com.sal.domain.*;
import com.sal.dto.*;
import com.sal.exception.*;
import com.sal.handler.spi.StorageHandler;
import com.sal.handler.spi.StorageHandlerFactory;
import com.sal.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Core SAL Service - orchestrates all storage operations.
 * 
 * Responsibilities:
 * - Metadata management
 * - Version orchestration
 * - Storage handler delegation
 * - History tracking
 */
@Service
public class SalService {

    private static final Logger logger = LoggerFactory.getLogger(SalService.class);

    private final SalObjectRepository objectRepository;
    private final SalMetadataRepository metadataRepository;
    private final SalMetadataHistRepository histRepository;
    private final StorageHandlerFactory handlerFactory;
    private final ChecksumService checksumService;
    private final CompressionService compressionService;
    private final SalProperties properties;

    public SalService(SalObjectRepository objectRepository,
                      SalMetadataRepository metadataRepository,
                      SalMetadataHistRepository histRepository,
                      StorageHandlerFactory handlerFactory,
                      ChecksumService checksumService,
                      CompressionService compressionService,
                      SalProperties properties) {
        this.objectRepository = objectRepository;
        this.metadataRepository = metadataRepository;
        this.histRepository = histRepository;
        this.handlerFactory = handlerFactory;
        this.checksumService = checksumService;
        this.compressionService = compressionService;
        this.properties = properties;
    }

    /**
     * Upload content - creates new object or new version.
     */
    @Transactional
    public UploadResponse upload(UploadRequest request, byte[] content) {
        logger.info("Upload request: name={}, size={}, owner={}", 
            request.getName(), request.getSizeInBytes(), request.getOwnerId());

        String user = request.getUserId() != null ? request.getUserId() : request.getOwnerId();
        
        // Determine storage type
        StorageType storageType = resolveStorageType(request.getStorageType());
        StorageHandler handler = handlerFactory.getHandler(storageType);

        // Handle compression
        boolean compress = shouldCompress(request, content.length);
        byte[] contentToStore = content;
        String compressionType = null;
        
        if (compress) {
            CompressionType compType = compressionService.getDefaultCompressionType();
            contentToStore = compressionService.compress(content, compType);
            compressionType = compType.name();
            logger.debug("Compressed {} bytes to {} bytes", content.length, contentToStore.length);
        }

        // Compute checksum on original content
        String checksum = checksumService.computeChecksum(content);

        UUID salUuid;
        Integer version;

        if (request.isNewObject()) {
            // Create new object
            SalObject obj = new SalObject();
            obj.setOwnerId(request.getOwnerId());
            obj.setCreatedBy(user);
            obj.setLstModUser(user);
            obj = objectRepository.create(obj);
            salUuid = obj.getSalUuid();
            version = 1;
            logger.info("Created new object: {}", salUuid);
        } else {
            // New version of existing object
            salUuid = request.getSalUuid();
            if (!objectRepository.exists(salUuid)) {
                throw new ObjectNotFoundException(salUuid);
            }
            version = metadataRepository.getNextVersion(salUuid);
            logger.info("Creating version {} for object {}", version, salUuid);
        }

        // Create metadata with PENDING_UPLOAD status
        SalMetadata metadata = new SalMetadata();
        metadata.setSalUuid(salUuid);
        metadata.setVersion(version);
        metadata.setSalName(request.getName());
        metadata.setSalDescription(request.getDescription());
        metadata.setSalType(request.getType());
        metadata.setSalMetadata(request.getMetadata());
        metadata.setSizeInBytes((long) content.length);
        metadata.setChecksumAlgorithm(checksumService.getDefaultAlgorithm());
        metadata.setStatus(ObjectStatus.PENDING_UPLOAD);
        metadata.setIsLatest(false);
        metadata.setIsCompressed(compress);
        metadata.setCompressionType(compressionType);
        metadata.setStorageType(storageType);
        metadata.setOwnerId(request.getOwnerId());
        metadata.setCreatedBy(user);
        metadata.setLstModUser(user);

        metadataRepository.create(metadata);

        // Record history
        recordHistory(metadata, HistoryAction.CREATE, user, "Version created");

        // Store content
        try {
            String storagePath = handler.store(salUuid, version, 
                new ByteArrayInputStream(contentToStore), contentToStore.length);
            
            // Update metadata with storage info
            metadataRepository.updateAfterUpload(salUuid, version, checksum, 
                (long) contentToStore.length, storagePath, user);

            // Reload and record history
            metadata = metadataRepository.findByUuidAndVersion(salUuid, version)
                .orElseThrow(() -> new ObjectNotFoundException(salUuid, version));
            recordHistory(metadata, HistoryAction.STATUS_CHANGE, user, "Upload completed");

            logger.info("Upload completed: {} v{}", salUuid, version);

            return UploadResponse.success(salUuid, version, checksum,
                (long) content.length, (long) contentToStore.length,
                compress, storageType.name());

        } catch (Exception e) {
            // Mark as failed
            metadataRepository.updateStatus(salUuid, version, ObjectStatus.FAILED.name(), user);
            throw new StorageException(storageType.name(), "upload", 
                "Failed to store content: " + e.getMessage(), e);
        }
    }

    /**
     * Download content.
     */
    public byte[] download(UUID salUuid, Integer version) {
        SalMetadata metadata = version != null 
            ? metadataRepository.findByUuidAndVersion(salUuid, version)
                .orElseThrow(() -> new ObjectNotFoundException(salUuid, version))
            : metadataRepository.findLatestByUuid(salUuid)
                .orElseThrow(() -> new ObjectNotFoundException(salUuid));

        if (!metadata.canDownload()) {
            throw new InvalidStatusException(salUuid, metadata.getVersion(), 
                metadata.getStatus(), "download");
        }

        StorageHandler handler = handlerFactory.getHandler(metadata.getStorageTypeEnum());
        
        try (InputStream in = handler.retrieve(salUuid, metadata.getVersion(), metadata.getStoragePath());
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            byte[] content = out.toByteArray();

            // Decompress if needed
            if (Boolean.TRUE.equals(metadata.getIsCompressed())) {
                content = compressionService.decompress(content, metadata.getCompressionType());
            }

            // Verify checksum if configured
            if (properties.getChecksum().isVerifyOnDownload() && metadata.getChecksum() != null) {
                String actualChecksum = checksumService.computeChecksum(content);
                if (!actualChecksum.equalsIgnoreCase(metadata.getChecksum())) {
                    throw new ChecksumMismatchException(salUuid, metadata.getVersion(),
                        metadata.getChecksum(), actualChecksum);
                }
            }

            return content;

        } catch (Exception e) {
            if (e instanceof StorageException || e instanceof ChecksumMismatchException) {
                throw (RuntimeException) e;
            }
            throw new StorageException(metadata.getStorageType(), "download",
                "Failed to retrieve content: " + e.getMessage(), e);
        }
    }

    /**
     * Get object info (latest version).
     */
    public ObjectInfoResponse getInfo(UUID salUuid) {
        SalMetadata metadata = metadataRepository.findLatestByUuid(salUuid)
            .orElseThrow(() -> new ObjectNotFoundException(salUuid));
        return ObjectInfoResponse.fromMetadata(metadata);
    }

    /**
     * Get specific version info.
     */
    public ObjectInfoResponse getVersionInfo(UUID salUuid, Integer version) {
        SalMetadata metadata = metadataRepository.findByUuidAndVersion(salUuid, version)
            .orElseThrow(() -> new ObjectNotFoundException(salUuid, version));
        return ObjectInfoResponse.fromMetadata(metadata);
    }

    /**
     * List all versions.
     */
    public List<ObjectInfoResponse> listVersions(UUID salUuid) {
        List<SalMetadata> versions = metadataRepository.findAllVersionsByUuid(salUuid);
        if (versions.isEmpty()) {
            throw new ObjectNotFoundException(salUuid);
        }
        return versions.stream()
            .map(ObjectInfoResponse::fromMetadata)
            .toList();
    }

    /**
     * Search objects.
     */
    public SearchResponse search(SearchRequest request) {
        List<SalMetadata> results = metadataRepository.search(request);
        long total = metadataRepository.countSearch(request);
        
        List<ObjectInfoResponse> items = results.stream()
            .map(ObjectInfoResponse::fromMetadata)
            .toList();
        
        return SearchResponse.of(items, request.getPage(), request.getSize(), total);
    }

    /**
     * Delete version (soft delete).
     */
    @Transactional
    public boolean deleteVersion(UUID salUuid, Integer version, String user) {
        SalMetadata metadata = metadataRepository.findByUuidAndVersion(salUuid, version)
            .orElseThrow(() -> new ObjectNotFoundException(salUuid, version));

        metadataRepository.updateStatus(salUuid, version, ObjectStatus.DELETED.name(), user);
        
        metadata = metadataRepository.findByUuidAndVersion(salUuid, version).orElseThrow();
        recordHistory(metadata, HistoryAction.DELETE, user, "Version deleted");

        logger.info("Deleted {} v{}", salUuid, version);
        return true;
    }

    /**
     * Get history for object.
     */
    public List<SalMetadataHist> getHistory(UUID salUuid) {
        return histRepository.findByUuid(salUuid);
    }

    /**
     * Get history for specific version.
     */
    public List<SalMetadataHist> getVersionHistory(UUID salUuid, Integer version) {
        return histRepository.findByUuidAndVersion(salUuid, version);
    }

    private StorageType resolveStorageType(String requestedType) {
        if (requestedType != null && !requestedType.isBlank()) {
            return StorageType.valueOf(requestedType.toUpperCase());
        }
        return StorageType.valueOf(properties.getDefaultStorageType().toUpperCase());
    }

    private boolean shouldCompress(UploadRequest request, long contentSize) {
        if (Boolean.FALSE.equals(request.getCompress())) {
            return false;
        }
        if (Boolean.TRUE.equals(request.getCompress())) {
            return true;
        }
        return compressionService.shouldCompress(contentSize);
    }

    private void recordHistory(SalMetadata metadata, HistoryAction action, String user, String reason) {
        SalMetadataHist hist = SalMetadataHist.fromMetadata(metadata, action, user, reason);
        histRepository.create(hist);
    }
}
