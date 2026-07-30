package com.sal.service;

import com.sal.config.SalProperties;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Service for computing and verifying checksums.
 */
@Service
public class ChecksumService {

    private final SalProperties properties;

    public ChecksumService(SalProperties properties) {
        this.properties = properties;
    }

    /**
     * Compute checksum for content.
     */
    public String computeChecksum(byte[] content) {
        return computeChecksum(content, properties.getChecksum().getAlgorithm());
    }

    /**
     * Compute checksum with specific algorithm.
     */
    public String computeChecksum(byte[] content, String algorithm) {
        return switch (algorithm.toUpperCase()) {
            case "SHA-256" -> DigestUtils.sha256Hex(content);
            case "SHA-512" -> DigestUtils.sha512Hex(content);
            case "MD5" -> DigestUtils.md5Hex(content);
            default -> throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        };
    }

    /**
     * Compute checksum from stream (reads entire stream).
     */
    public String computeChecksum(InputStream inputStream) throws IOException {
        return computeChecksum(inputStream, properties.getChecksum().getAlgorithm());
    }

    /**
     * Compute checksum from stream with specific algorithm.
     */
    public String computeChecksum(InputStream inputStream, String algorithm) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm.replace("-", ""));
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm, e);
        }
    }

    /**
     * Verify checksum matches.
     */
    public boolean verifyChecksum(byte[] content, String expectedChecksum) {
        String actualChecksum = computeChecksum(content);
        return actualChecksum.equalsIgnoreCase(expectedChecksum);
    }

    /**
     * Get default algorithm.
     */
    public String getDefaultAlgorithm() {
        return properties.getChecksum().getAlgorithm();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
