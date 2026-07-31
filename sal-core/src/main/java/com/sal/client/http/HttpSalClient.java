package com.sal.client.http;

import com.sal.client.SalClient;
import com.sal.config.SalClientProperties;
import com.sal.dto.ObjectInfoResponse;
import com.sal.dto.UploadResponse;
import com.sal.exception.SalClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP REST client implementation for SAL.
 * 
 * Communicates with SAL service via REST API.
 */
public class HttpSalClient implements SalClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpSalClient.class);
    private static final String API_PATH = "/api/v1/sal";

    private final WebClient webClient;
    private final SalClientProperties properties;

    public HttpSalClient(SalClientProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        
        logger.info("Initialized HTTP SAL client with base URL: {}", properties.getBaseUrl());
    }

    public HttpSalClient(WebClient webClient, SalClientProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    @Override
    public UploadResponse upload(String name, byte[] content, String ownerId, Map<String, String> metadata) {
        return upload(name, content, ownerId, null, null, metadata);
    }

    @Override
    public UploadResponse upload(String name, byte[] content, String ownerId, 
                                 String type, String description, Map<String, String> metadata) {
        logger.debug("Uploading file: name={}, size={}, owner={}", name, content.length, ownerId);

        try {
            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            formData.add("file", new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return name;
                }
            });
            formData.add("name", name);
            formData.add("ownerId", ownerId);
            
            if (type != null) {
                formData.add("type", type);
            }
            if (description != null) {
                formData.add("description", description);
            }
            if (properties.getDefaultStorageType() != null) {
                formData.add("storageType", properties.getDefaultStorageType());
            }
            formData.add("compress", properties.isCompressFiles());

            UploadResponse response = webClient.post()
                    .uri(API_PATH + "/objects")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(formData))
                    .retrieve()
                    .bodyToMono(UploadResponse.class)
                    .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                    .block();

            logger.info("File uploaded successfully: uuid={}, version={}", 
                    response.getSalUuid(), response.getVersion());
            return response;

        } catch (WebClientResponseException e) {
            throw new SalClientException("upload", e.getResponseBodyAsString(), e.getStatusCode().value());
        } catch (Exception e) {
            throw new SalClientException("upload", e.getMessage(), e);
        }
    }

    @Override
    public UploadResponse uploadNewVersion(UUID salUuid, String name, byte[] content, String ownerId) {
        logger.debug("Uploading new version: uuid={}, name={}, size={}", salUuid, name, content.length);

        try {
            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            formData.add("file", new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return name;
                }
            });
            formData.add("name", name);
            formData.add("ownerId", ownerId);
            formData.add("salUuid", salUuid.toString());
            formData.add("storageType", properties.getDefaultStorageType());
            formData.add("compress", properties.isCompressFiles());

            UploadResponse response = webClient.post()
                    .uri(API_PATH + "/objects")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(formData))
                    .retrieve()
                    .bodyToMono(UploadResponse.class)
                    .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                    .block();

            logger.info("New version uploaded: uuid={}, version={}", salUuid, response.getVersion());
            return response;

        } catch (WebClientResponseException e) {
            throw new SalClientException("uploadNewVersion", e.getResponseBodyAsString(), e.getStatusCode().value());
        } catch (Exception e) {
            throw new SalClientException("uploadNewVersion", e.getMessage(), e);
        }
    }

    @Override
    public byte[] download(UUID salUuid) {
        return download(salUuid, null);
    }

    @Override
    public byte[] download(UUID salUuid, Integer version) {
        logger.debug("Downloading file: uuid={}, version={}", salUuid, version);

        try {
            String uri = version != null
                    ? API_PATH + "/objects/" + salUuid + "/versions/" + version + "/content"
                    : API_PATH + "/objects/" + salUuid + "/content";

            byte[] content = webClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                    .block();

            logger.debug("File downloaded: uuid={}, size={}", salUuid, content != null ? content.length : 0);
            return content;

        } catch (WebClientResponseException e) {
            throw new SalClientException("download", e.getResponseBodyAsString(), e.getStatusCode().value());
        } catch (Exception e) {
            throw new SalClientException("download", e.getMessage(), e);
        }
    }

    @Override
    public ObjectInfoResponse getInfo(UUID salUuid) {
        logger.debug("Getting info: uuid={}", salUuid);

        try {
            return webClient.get()
                    .uri(API_PATH + "/objects/" + salUuid)
                    .retrieve()
                    .bodyToMono(ObjectInfoResponse.class)
                    .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                    .block();

        } catch (WebClientResponseException e) {
            throw new SalClientException("getInfo", e.getResponseBodyAsString(), e.getStatusCode().value());
        } catch (Exception e) {
            throw new SalClientException("getInfo", e.getMessage(), e);
        }
    }

    @Override
    public ObjectInfoResponse getVersionInfo(UUID salUuid, Integer version) {
        logger.debug("Getting version info: uuid={}, version={}", salUuid, version);

        try {
            return webClient.get()
                    .uri(API_PATH + "/objects/" + salUuid + "/versions/" + version)
                    .retrieve()
                    .bodyToMono(ObjectInfoResponse.class)
                    .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                    .block();

        } catch (WebClientResponseException e) {
            throw new SalClientException("getVersionInfo", e.getResponseBodyAsString(), e.getStatusCode().value());
        } catch (Exception e) {
            throw new SalClientException("getVersionInfo", e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(UUID salUuid) {
        try {
            getInfo(salUuid);
            return true;
        } catch (SalClientException e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public void deleteVersion(UUID salUuid, Integer version, String userId) {
        logger.debug("Deleting version: uuid={}, version={}, user={}", salUuid, version, userId);

        try {
            webClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path(API_PATH + "/objects/" + salUuid + "/versions/" + version)
                            .queryParam("userId", userId)
                            .build())
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                    .block();

            logger.info("Version deleted: uuid={}, version={}", salUuid, version);

        } catch (WebClientResponseException e) {
            throw new SalClientException("deleteVersion", e.getResponseBodyAsString(), e.getStatusCode().value());
        } catch (Exception e) {
            throw new SalClientException("deleteVersion", e.getMessage(), e);
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            String response = webClient.get()
                    .uri(API_PATH + "/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                    .block();
            return "OK".equals(response);
        } catch (Exception e) {
            logger.warn("Health check failed: {}", e.getMessage());
            return false;
        }
    }
}
