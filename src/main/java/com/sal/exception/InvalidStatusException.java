package com.sal.exception;

import java.util.UUID;

/**
 * Thrown when an operation is invalid for the current status.
 */
public class InvalidStatusException extends RuntimeException {

    private final UUID salUuid;
    private final Integer version;
    private final String currentStatus;
    private final String operation;

    public InvalidStatusException(UUID salUuid, Integer version, String currentStatus, String operation) {
        super(String.format("Cannot %s object %s v%d in status %s",
            operation, salUuid, version, currentStatus));
        this.salUuid = salUuid;
        this.version = version;
        this.currentStatus = currentStatus;
        this.operation = operation;
    }

    public UUID getSalUuid() {
        return salUuid;
    }

    public Integer getVersion() {
        return version;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getOperation() {
        return operation;
    }
}
