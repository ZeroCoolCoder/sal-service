package com.sal.handler.impl;

import com.sal.config.SalProperties;
import com.sal.domain.StorageType;
import com.sal.exception.StorageException;
import com.sal.handler.spi.StorageHandler;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * File system storage handler.
 * Stores content in local file system with directory structure based on UUID.
 */
@Component
public class FileSystemStorageHandler implements StorageHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageHandler.class);

    private final SalProperties properties;
    private Path basePath;

    public FileSystemStorageHandler(SalProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        this.basePath = Paths.get(properties.getFilesystem().getBasePath()).toAbsolutePath();
        
        if (properties.getFilesystem().isCreateDirectories()) {
            try {
                Files.createDirectories(basePath);
                logger.info("FileSystem storage initialized at: {}", basePath);
            } catch (IOException e) {
                throw new StorageException("FILESYSTEM", "init", 
                    "Failed to create base directory: " + basePath, e);
            }
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.FILESYSTEM;
    }

    @Override
    public String store(UUID salUuid, Integer version, InputStream inputStream, long contentSize) {
        Path filePath = getFilePath(salUuid, version);
        
        try {
            // Create parent directories
            Files.createDirectories(filePath.getParent());
            
            // Write content to file
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(filePath))) {
                IOUtils.copy(inputStream, out);
            }
            
            String storagePath = filePath.toString();
            logger.debug("Stored content at: {}", storagePath);
            return storagePath;
            
        } catch (IOException e) {
            throw new StorageException("FILESYSTEM", "store", 
                "Failed to store content for " + salUuid + " v" + version, e);
        }
    }

    @Override
    public InputStream retrieve(UUID salUuid, Integer version, String storagePath) {
        Path filePath = Paths.get(storagePath);
        
        if (!Files.exists(filePath)) {
            throw new StorageException("FILESYSTEM", "retrieve",
                "File not found: " + storagePath);
        }
        
        try {
            return new BufferedInputStream(Files.newInputStream(filePath));
        } catch (IOException e) {
            throw new StorageException("FILESYSTEM", "retrieve",
                "Failed to open file: " + storagePath, e);
        }
    }

    @Override
    public void streamTo(UUID salUuid, Integer version, String storagePath, OutputStream outputStream) {
        try (InputStream in = retrieve(salUuid, version, storagePath)) {
            IOUtils.copy(in, outputStream);
        } catch (IOException e) {
            throw new StorageException("FILESYSTEM", "streamTo",
                "Failed to stream content: " + storagePath, e);
        }
    }

    @Override
    public boolean delete(UUID salUuid, Integer version, String storagePath) {
        Path filePath = Paths.get(storagePath);
        
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                logger.debug("Deleted file: {}", storagePath);
                // Clean up empty parent directories
                cleanupEmptyDirectories(filePath.getParent());
            }
            return deleted;
        } catch (IOException e) {
            throw new StorageException("FILESYSTEM", "delete",
                "Failed to delete file: " + storagePath, e);
        }
    }

    @Override
    public boolean exists(UUID salUuid, Integer version, String storagePath) {
        return Files.exists(Paths.get(storagePath));
    }

    @Override
    public long getStoredSize(UUID salUuid, Integer version, String storagePath) {
        try {
            return Files.size(Paths.get(storagePath));
        } catch (IOException e) {
            throw new StorageException("FILESYSTEM", "getStoredSize",
                "Failed to get file size: " + storagePath, e);
        }
    }

    /**
     * Build file path using UUID-based directory structure.
     * Format: basePath / uuid[0:2] / uuid[2:4] / uuid / version.dat
     */
    private Path getFilePath(UUID salUuid, Integer version) {
        String uuid = salUuid.toString().replace("-", "");
        String dir1 = uuid.substring(0, 2);
        String dir2 = uuid.substring(2, 4);
        
        return basePath
            .resolve(dir1)
            .resolve(dir2)
            .resolve(salUuid.toString())
            .resolve(version + ".dat");
    }

    /**
     * Clean up empty parent directories up to base path.
     */
    private void cleanupEmptyDirectories(Path directory) {
        try {
            while (directory != null && !directory.equals(basePath)) {
                if (Files.isDirectory(directory) && isDirectoryEmpty(directory)) {
                    Files.delete(directory);
                    directory = directory.getParent();
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to clean up empty directories", e);
        }
    }

    private boolean isDirectoryEmpty(Path directory) throws IOException {
        try (var entries = Files.list(directory)) {
            return entries.findFirst().isEmpty();
        }
    }
}
