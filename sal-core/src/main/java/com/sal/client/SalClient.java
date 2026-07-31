package com.sal.client;

import com.sal.dto.ObjectInfoResponse;
import com.sal.dto.UploadResponse;

import java.util.Map;
import java.util.UUID;

/**
 * Client interface for SAL (Storage Abstraction Layer) operations.
 * 
 * This interface provides a unified way to interact with SAL regardless of the
 * underlying implementation (HTTP REST API or embedded library).
 */
public interface SalClient {

    /**
     * Upload a new file to SAL.
     *
     * @param name    The name of the file
     * @param content The file content as bytes
     * @param ownerId The owner identifier
     * @param metadata Optional metadata key-value pairs
     * @return Upload response containing the SAL UUID and version
     */
    UploadResponse upload(String name, byte[] content, String ownerId, Map<String, String> metadata);

    /**
     * Upload a new file to SAL with type and description.
     *
     * @param name        The name of the file
     * @param content     The file content as bytes
     * @param ownerId     The owner identifier
     * @param type        The file type (e.g., "CSV", "JSON", "PDF")
     * @param description Optional description
     * @param metadata    Optional metadata key-value pairs
     * @return Upload response containing the SAL UUID and version
     */
    UploadResponse upload(String name, byte[] content, String ownerId, 
                          String type, String description, Map<String, String> metadata);

    /**
     * Upload a new version of an existing file.
     *
     * @param salUuid The UUID of the existing object
     * @param name    The name of the file
     * @param content The file content as bytes
     * @param ownerId The owner identifier
     * @return Upload response containing the new version number
     */
    UploadResponse uploadNewVersion(UUID salUuid, String name, byte[] content, String ownerId);

    /**
     * Download the latest version of a file.
     *
     * @param salUuid The UUID of the object
     * @return The file content as bytes
     */
    byte[] download(UUID salUuid);

    /**
     * Download a specific version of a file.
     *
     * @param salUuid The UUID of the object
     * @param version The version number
     * @return The file content as bytes
     */
    byte[] download(UUID salUuid, Integer version);

    /**
     * Get information about the latest version of an object.
     *
     * @param salUuid The UUID of the object
     * @return Object information response
     */
    ObjectInfoResponse getInfo(UUID salUuid);

    /**
     * Get information about a specific version of an object.
     *
     * @param salUuid The UUID of the object
     * @param version The version number
     * @return Object information response
     */
    ObjectInfoResponse getVersionInfo(UUID salUuid, Integer version);

    /**
     * Check if an object exists.
     *
     * @param salUuid The UUID of the object
     * @return true if the object exists
     */
    boolean exists(UUID salUuid);

    /**
     * Delete a specific version of an object (soft delete).
     *
     * @param salUuid The UUID of the object
     * @param version The version number
     * @param userId  The user performing the deletion
     */
    void deleteVersion(UUID salUuid, Integer version, String userId);

    /**
     * Check if the SAL service is healthy and available.
     *
     * @return true if healthy
     */
    boolean isHealthy();
}
