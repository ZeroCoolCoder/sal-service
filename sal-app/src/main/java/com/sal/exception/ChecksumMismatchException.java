package com.sal.exception;

import java.util.UUID;

/**
 * Thrown when checksum verification fails.
 */
public class ChecksumMismatchException extends RuntimeException {

    private final UUID salUuid;
    private final Integer version;
    private final String expected;
    private final String actual;

    public ChecksumMismatchException(UUID salUuid, Integer version, String expected, String actual) {
        super(String.format("Checksum mismatch for %s v%d: expected %s, got %s",
            salUuid, version, expected, actual));
        this.salUuid = salUuid;
        this.version = version;
        this.expected = expected;
        this.actual = actual;
    }

    public UUID getSalUuid() {
        return salUuid;
    }

    public Integer getVersion() {
        return version;
    }

    public String getExpected() {
        return expected;
    }

    public String getActual() {
        return actual;
    }
}
