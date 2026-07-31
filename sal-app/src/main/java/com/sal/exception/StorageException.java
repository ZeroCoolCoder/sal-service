package com.sal.exception;

/**
 * Thrown when a storage operation fails.
 */
public class StorageException extends RuntimeException {

    private final String storageType;
    private final String operation;

    public StorageException(String message) {
        super(message);
        this.storageType = null;
        this.operation = null;
    }

    public StorageException(String storageType, String operation, String message) {
        super(String.format("Storage error [%s/%s]: %s", storageType, operation, message));
        this.storageType = storageType;
        this.operation = operation;
    }

    public StorageException(String storageType, String operation, String message, Throwable cause) {
        super(String.format("Storage error [%s/%s]: %s", storageType, operation, message), cause);
        this.storageType = storageType;
        this.operation = operation;
    }

    public String getStorageType() {
        return storageType;
    }

    public String getOperation() {
        return operation;
    }
}
