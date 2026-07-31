package com.sal.exception;

import java.util.UUID;

/**
 * Thrown when a storage object is not found.
 */
public class ObjectNotFoundException extends RuntimeException {

    private final UUID salUuid;
    private final Integer version;

    public ObjectNotFoundException(UUID salUuid) {
        super("Object not found: " + salUuid);
        this.salUuid = salUuid;
        this.version = null;
    }

    public ObjectNotFoundException(UUID salUuid, Integer version) {
        super("Object not found: " + salUuid + " version " + version);
        this.salUuid = salUuid;
        this.version = version;
    }

    public UUID getSalUuid() {
        return salUuid;
    }

    public Integer getVersion() {
        return version;
    }
}
