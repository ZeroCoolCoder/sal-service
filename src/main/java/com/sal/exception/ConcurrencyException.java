package com.sal.exception;

import java.util.UUID;

/**
 * Thrown when a concurrent modification is detected.
 */
public class ConcurrencyException extends RuntimeException {

    private final UUID salUuid;

    public ConcurrencyException(UUID salUuid) {
        super("Concurrent modification detected for object: " + salUuid);
        this.salUuid = salUuid;
    }

    public UUID getSalUuid() {
        return salUuid;
    }
}
