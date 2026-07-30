package com.sal.controller;

import com.sal.domain.SalMetadataHist;
import com.sal.dto.*;
import com.sal.service.SalService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * REST API controller for Storage Abstraction Layer.
 */
@RestController
@RequestMapping("/api/v1/sal")
public class SalController {

    private static final Logger logger = LoggerFactory.getLogger(SalController.class);

    private final SalService salService;

    public SalController(SalService salService) {
        this.salService = salService;
    }

    /**
     * Upload new object or new version with file.
     */
    @PostMapping(value = "/objects", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("ownerId") String ownerId,
            @RequestParam(value = "salUuid", required = false) UUID salUuid,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "storageType", required = false) String storageType,
            @RequestParam(value = "compress", required = false) Boolean compress,
            @RequestParam(value = "userId", required = false) String userId) throws IOException {

        logger.info("Upload request: name={}, size={}, owner={}", 
            name, file.getSize(), ownerId);

        UploadRequest request = new UploadRequest();
        request.setSalUuid(salUuid);
        request.setName(name);
        request.setDescription(description);
        request.setType(type);
        request.setSizeInBytes(file.getSize());
        request.setStorageType(storageType);
        request.setCompress(compress);
        request.setOwnerId(ownerId);
        request.setUserId(userId);

        UploadResponse response = salService.upload(request, file.getBytes());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Upload with JSON request and base64 content.
     */
    @PostMapping(value = "/objects/upload", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadResponse> uploadJson(
            @Valid @RequestBody UploadRequestWithContent request) {

        logger.info("JSON upload: name={}, owner={}", request.getName(), request.getOwnerId());

        UploadRequest uploadRequest = new UploadRequest();
        uploadRequest.setSalUuid(request.getSalUuid());
        uploadRequest.setName(request.getName());
        uploadRequest.setDescription(request.getDescription());
        uploadRequest.setType(request.getType());
        uploadRequest.setMetadata(request.getMetadata());
        uploadRequest.setSizeInBytes((long) request.getContent().length);
        uploadRequest.setStorageType(request.getStorageType());
        uploadRequest.setCompress(request.getCompress());
        uploadRequest.setOwnerId(request.getOwnerId());
        uploadRequest.setUserId(request.getUserId());

        UploadResponse response = salService.upload(uploadRequest, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get object info (latest version).
     */
    @GetMapping("/objects/{salUuid}")
    public ResponseEntity<ObjectInfoResponse> getInfo(@PathVariable UUID salUuid) {
        ObjectInfoResponse response = salService.getInfo(salUuid);
        return ResponseEntity.ok(response);
    }

    /**
     * Get specific version info.
     */
    @GetMapping("/objects/{salUuid}/versions/{version}")
    public ResponseEntity<ObjectInfoResponse> getVersionInfo(
            @PathVariable UUID salUuid,
            @PathVariable Integer version) {
        ObjectInfoResponse response = salService.getVersionInfo(salUuid, version);
        return ResponseEntity.ok(response);
    }

    /**
     * List all versions.
     */
    @GetMapping("/objects/{salUuid}/versions")
    public ResponseEntity<List<ObjectInfoResponse>> listVersions(@PathVariable UUID salUuid) {
        List<ObjectInfoResponse> versions = salService.listVersions(salUuid);
        return ResponseEntity.ok(versions);
    }

    /**
     * Download content (latest version).
     */
    @GetMapping("/objects/{salUuid}/content")
    public ResponseEntity<byte[]> download(@PathVariable UUID salUuid) {
        ObjectInfoResponse info = salService.getInfo(salUuid);
        byte[] content = salService.download(salUuid, null);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + info.getName() + "\"")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header("X-SAL-Checksum", info.getChecksum())
            .header("X-SAL-Version", String.valueOf(info.getVersion()))
            .body(content);
    }

    /**
     * Download specific version.
     */
    @GetMapping("/objects/{salUuid}/versions/{version}/content")
    public ResponseEntity<byte[]> downloadVersion(
            @PathVariable UUID salUuid,
            @PathVariable Integer version) {
        ObjectInfoResponse info = salService.getVersionInfo(salUuid, version);
        byte[] content = salService.download(salUuid, version);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + info.getName() + "\"")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header("X-SAL-Checksum", info.getChecksum())
            .header("X-SAL-Version", String.valueOf(version))
            .body(content);
    }

    /**
     * Search objects.
     */
    @PostMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestBody SearchRequest request) {
        SearchResponse response = salService.search(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete version (soft delete).
     */
    @DeleteMapping("/objects/{salUuid}/versions/{version}")
    public ResponseEntity<Void> deleteVersion(
            @PathVariable UUID salUuid,
            @PathVariable Integer version,
            @RequestParam(value = "userId", required = false, defaultValue = "system") String userId) {
        salService.deleteVersion(salUuid, version, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get object history.
     */
    @GetMapping("/objects/{salUuid}/history")
    public ResponseEntity<List<SalMetadataHist>> getHistory(@PathVariable UUID salUuid) {
        List<SalMetadataHist> history = salService.getHistory(salUuid);
        return ResponseEntity.ok(history);
    }

    /**
     * Get version history.
     */
    @GetMapping("/objects/{salUuid}/versions/{version}/history")
    public ResponseEntity<List<SalMetadataHist>> getVersionHistory(
            @PathVariable UUID salUuid,
            @PathVariable Integer version) {
        List<SalMetadataHist> history = salService.getVersionHistory(salUuid, version);
        return ResponseEntity.ok(history);
    }

    /**
     * Health check.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
