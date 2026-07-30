package com.sal.service;

import com.sal.config.SalProperties;
import com.sal.domain.CompressionType;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Service for compressing and decompressing content.
 */
@Service
public class CompressionService {

    private final SalProperties properties;

    public CompressionService(SalProperties properties) {
        this.properties = properties;
    }

    /**
     * Check if content should be compressed based on size.
     */
    public boolean shouldCompress(long sizeInBytes) {
        if (!properties.getCompression().isEnabled()) {
            return false;
        }
        return sizeInBytes >= properties.getCompression().getMinSizeBytes();
    }

    /**
     * Compress content using default compression type.
     */
    public byte[] compress(byte[] content) {
        return compress(content, getDefaultCompressionType());
    }

    /**
     * Compress content with specific type.
     */
    public byte[] compress(byte[] content, CompressionType type) {
        if (type == CompressionType.NONE) {
            return content;
        }
        
        return switch (type) {
            case GZIP -> compressGzip(content);
            case ZSTD, LZ4 -> throw new UnsupportedOperationException(
                type + " compression not yet implemented");
            default -> content;
        };
    }

    /**
     * Decompress content.
     */
    public byte[] decompress(byte[] content, CompressionType type) {
        if (type == CompressionType.NONE || type == null) {
            return content;
        }
        
        return switch (type) {
            case GZIP -> decompressGzip(content);
            case ZSTD, LZ4 -> throw new UnsupportedOperationException(
                type + " decompression not yet implemented");
            default -> content;
        };
    }

    /**
     * Decompress content by type name.
     */
    public byte[] decompress(byte[] content, String compressionTypeName) {
        if (compressionTypeName == null || compressionTypeName.isBlank()) {
            return content;
        }
        return decompress(content, CompressionType.valueOf(compressionTypeName));
    }

    /**
     * Get default compression type.
     */
    public CompressionType getDefaultCompressionType() {
        String defaultType = properties.getCompression().getDefaultType();
        return CompressionType.valueOf(defaultType.toUpperCase());
    }

    private byte[] compressGzip(byte[] content) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
            gzos.write(content);
            gzos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("GZIP compression failed", e);
        }
    }

    private byte[] decompressGzip(byte[] content) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(content);
             GZIPInputStream gzis = new GZIPInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = gzis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("GZIP decompression failed", e);
        }
    }
}
