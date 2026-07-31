package com.sal.config;

import com.sal.client.SalClient;
import com.sal.client.SalClientFactory;
import com.sal.client.http.HttpSalClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for SAL client.
 * 
 * This auto-configuration is only enabled when sal.client.enabled=true.
 * This prevents it from being activated in the SAL service itself.
 */
@Configuration
@EnableConfigurationProperties(SalClientProperties.class)
@ConditionalOnProperty(name = "sal.client.enabled", havingValue = "true", matchIfMissing = false)
public class SalClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SalClientFactory salClientFactory(SalClientProperties properties) {
        return new SalClientFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "sal.client.mode", havingValue = "http", matchIfMissing = true)
    public SalClient salClient(SalClientFactory factory) {
        return factory.createHttpClient();
    }
}
