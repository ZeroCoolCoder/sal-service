package com.sal.handler.impl;

import com.sal.domain.SalBinaryContent;
import com.sal.domain.StorageType;
import com.sal.exception.StorageException;
import com.sal.handler.spi.StorageHandler;
import com.sal.repository.SalBinaryContentRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.UUID;

/**
 * Database storage handler.
 * Stores content in SAL_BINARY_CONTENT table as BYTEA.
 */
@Component
public class DatabaseStorageHandler implements StorageHandler {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseStorageHandler.class);

    private final SalBinaryContentRepository binaryContentRepository;

    public DatabaseStorageHandler(SalBinaryContentRepository binaryContentRepository) {
        this.binaryContentRepository = binaryContentRepository;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.DATABASE;
    }

    @Override
    public String store(UUID salUuid, Integer version, InputStream inputStream, long contentSize) {
        try {
            // Read all content into byte array
            byte[] content = IOUtils.toByteArray(inputStream);
            
            // Create binary content entry
            SalBinaryContent binaryContent = new SalBinaryContent();
            binaryContent.setSalUuid(salUuid);
            binaryContent.setVersion(version);
            binaryContent.setContentData(content);
            binaryContent.setCreatedBy("system");
            
            binaryContentRepository.create(binaryContent);
            
            // Return a virtual path (used for reference only)
            String storagePath = "db://" + salUuid + "/" + version;
            logger.debug("Stored {} bytes in database for {} v{}", content.length, salUuid, version);
            return storagePath;
            
        } catch (IOException e) {
            throw new StorageException("DATABASE", "store",
                "Failed to read content for " + salUuid + " v" + version, e);
        }
    }

    @Override
    public InputStream retrieve(UUID salUuid, Integer version, String storagePath) {
        return binaryContentRepository.getContentData(salUuid, version)
            .map(ByteArrayInputStream::new)
            .orElseThrow(() -> new StorageException("DATABASE", "retrieve",
                "Content not found for " + salUuid + " v" + version));
    }

    @Override
    public void streamTo(UUID salUuid, Integer version, String storagePath, OutputStream outputStream) {
        byte[] content = binaryContentRepository.getContentData(salUuid, version)
            .orElseThrow(() -> new StorageException("DATABASE", "streamTo",
                "Content not found for " + salUuid + " v" + version));
        
        try {
            outputStream.write(content);
        } catch (IOException e) {
            throw new StorageException("DATABASE", "streamTo",
                "Failed to write content to output stream", e);
        }
    }

    @Override
    public boolean delete(UUID salUuid, Integer version, String storagePath) {
        boolean deleted = binaryContentRepository.delete(salUuid, version);
        if (deleted) {
            logger.debug("Deleted database content for {} v{}", salUuid, version);
        }
        return deleted;
    }

    @Override
    public boolean exists(UUID salUuid, Integer version, String storagePath) {
        return binaryContentRepository.exists(salUuid, version);
    }

    @Override
    public long getStoredSize(UUID salUuid, Integer version, String storagePath) {
        return binaryContentRepository.findByUuidAndVersion(salUuid, version)
            .map(SalBinaryContent::getContentSize)
            .orElse(0L);
    }
}
