package com.sal.client;

import com.sal.client.http.HttpSalClient;
import com.sal.config.SalClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating SAL client instances.
 */
public class SalClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(SalClientFactory.class);

    private final SalClientProperties properties;

    public SalClientFactory(SalClientProperties properties) {
        this.properties = properties;
    }

    /**
     * Create a SAL client based on the configured mode.
     *
     * @return SalClient instance (HTTP or embedded based on configuration)
     */
    public SalClient createClient() {
        if (properties.isHttpMode()) {
            logger.info("Creating HTTP SAL client for base URL: {}", properties.getBaseUrl());
            return new HttpSalClient(properties);
        } else if (properties.isEmbeddedMode()) {
            throw new UnsupportedOperationException(
                    "Embedded mode requires EmbeddedSalClient which needs additional dependencies. " +
                    "Use HTTP mode or configure embedded client separately.");
        } else {
            throw new IllegalArgumentException("Unknown SAL client mode: " + properties.getMode());
        }
    }

    /**
     * Create an HTTP SAL client explicitly.
     *
     * @return HttpSalClient instance
     */
    public HttpSalClient createHttpClient() {
        return new HttpSalClient(properties);
    }

    public SalClientProperties getProperties() {
        return properties;
    }
}
