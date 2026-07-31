package com.sal.exception;

/**
 * Exception thrown by SAL client operations.
 */
public class SalClientException extends RuntimeException {

    private final String operation;
    private final int statusCode;

    public SalClientException(String message) {
        super(message);
        this.operation = null;
        this.statusCode = 0;
    }

    public SalClientException(String message, Throwable cause) {
        super(message, cause);
        this.operation = null;
        this.statusCode = 0;
    }

    public SalClientException(String operation, String message) {
        super(String.format("SAL %s failed: %s", operation, message));
        this.operation = operation;
        this.statusCode = 0;
    }

    public SalClientException(String operation, String message, int statusCode) {
        super(String.format("SAL %s failed with status %d: %s", operation, statusCode, message));
        this.operation = operation;
        this.statusCode = statusCode;
    }

    public SalClientException(String operation, String message, Throwable cause) {
        super(String.format("SAL %s failed: %s", operation, message), cause);
        this.operation = operation;
        this.statusCode = 0;
    }

    public String getOperation() {
        return operation;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
