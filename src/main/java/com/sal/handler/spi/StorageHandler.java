package com.sal.handler.spi;

import com.sal.domain.StorageType;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Storage Provider Interface (SPI).
 * 
 * All storage providers must implement this interface.
 * Implementations handle the actual storage/retrieval of binary content.
 */
public interface StorageHandler {

    /**
     * Get the storage type this handler supports.
     */
    StorageType getStorageType();

    /**
     * Store content and return the storage path.
     * 
     * @param salUuid Object UUID
     * @param version Version number
     * @param inputStream Content to store
     * @param contentSize Size of content in bytes
     * @return Storage path for retrieval
     */
    String store(UUID salUuid, Integer version, InputStream inputStream, long contentSize);

    /**
     * Retrieve content as InputStream.
     * 
     * @param salUuid Object UUID
     * @param version Version number
     * @param storagePath Path from store operation
     * @return InputStream of content
     */
    InputStream retrieve(UUID salUuid, Integer version, String storagePath);

    /**
     * Stream content to OutputStream.
     * 
     * @param salUuid Object UUID
     * @param version Version number
     * @param storagePath Path from store operation
     * @param outputStream Target output stream
     */
    void streamTo(UUID salUuid, Integer version, String storagePath, OutputStream outputStream);

    /**
     * Delete stored content.
     * 
     * @param salUuid Object UUID
     * @param version Version number
     * @param storagePath Path from store operation
     * @return true if deleted
     */
    boolean delete(UUID salUuid, Integer version, String storagePath);

    /**
     * Check if content exists.
     * 
     * @param salUuid Object UUID
     * @param version Version number
     * @param storagePath Path from store operation
     * @return true if exists
     */
    boolean exists(UUID salUuid, Integer version, String storagePath);

    /**
     * Get actual stored size (may differ from original if compressed).
     * 
     * @param salUuid Object UUID
     * @param version Version number
     * @param storagePath Path from store operation
     * @return Size in bytes
     */
    long getStoredSize(UUID salUuid, Integer version, String storagePath);
}
