package com.sal.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sal.domain.SalMetadata;
import com.sal.dto.SearchRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Repository for SAL_METADATA table.
 */
@Repository
public class SalMetadataRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final SalMetadataRowMapper rowMapper;

    public SalMetadataRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.rowMapper = new SalMetadataRowMapper(objectMapper);
    }

    /**
     * Create new metadata entry.
     */
    public void create(SalMetadata metadata) {
        String sql = """
            INSERT INTO sal_metadata (
                sal_uuid, version, sal_name, sal_description, sal_type, sal_metadata,
                size_in_bytes, stored_size_in_bytes, checksum, checksum_algorithm,
                status, is_latest, is_compressed, compression_type,
                storage_type, storage_path, owner_id,
                lst_mod_chg_cd, lst_mod_user, created_by
            ) VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(sql,
            metadata.getSalUuid(),
            metadata.getVersion(),
            metadata.getSalName(),
            metadata.getSalDescription(),
            metadata.getSalType(),
            toJsonString(metadata.getSalMetadata()),
            metadata.getSizeInBytes(),
            metadata.getStoredSizeInBytes(),
            metadata.getChecksum(),
            metadata.getChecksumAlgorithm(),
            metadata.getStatus(),
            metadata.getIsLatest(),
            metadata.getIsCompressed(),
            metadata.getCompressionType(),
            metadata.getStorageType(),
            metadata.getStoragePath(),
            metadata.getOwnerId(),
            metadata.getLstModChgCd(),
            metadata.getLstModUser(),
            metadata.getCreatedBy()
        );
    }

    /**
     * Find metadata by UUID and version.
     */
    public Optional<SalMetadata> findByUuidAndVersion(UUID salUuid, Integer version) {
        String sql = "SELECT * FROM sal_metadata WHERE sal_uuid = ? AND version = ?";
        List<SalMetadata> results = jdbcTemplate.query(sql, rowMapper, salUuid, version);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Find latest version metadata for UUID.
     */
    public Optional<SalMetadata> findLatestByUuid(UUID salUuid) {
        String sql = "SELECT * FROM sal_metadata WHERE sal_uuid = ? AND is_latest = TRUE";
        List<SalMetadata> results = jdbcTemplate.query(sql, rowMapper, salUuid);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Find all versions for UUID.
     */
    public List<SalMetadata> findAllVersionsByUuid(UUID salUuid) {
        String sql = "SELECT * FROM sal_metadata WHERE sal_uuid = ? ORDER BY version DESC";
        return jdbcTemplate.query(sql, rowMapper, salUuid);
    }

    /**
     * Get next version number.
     */
    public Integer getNextVersion(UUID salUuid) {
        String sql = "SELECT COALESCE(MAX(version), 0) + 1 FROM sal_metadata WHERE sal_uuid = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, salUuid);
    }

    /**
     * Update status.
     */
    public boolean updateStatus(UUID salUuid, Integer version, String status, String user) {
        String sql = """
            UPDATE sal_metadata 
            SET status = ?, lst_mod_user = ?, lst_mod_ts = CURRENT_TIMESTAMP, lst_mod_chg_cd = 'STATUS'
            WHERE sal_uuid = ? AND version = ?
            """;
        int rows = jdbcTemplate.update(sql, status, user, salUuid, version);
        return rows > 0;
    }

    /**
     * Set as latest version (clears other latest flags via trigger).
     */
    public boolean setLatest(UUID salUuid, Integer version, String user) {
        String sql = """
            UPDATE sal_metadata 
            SET is_latest = TRUE, lst_mod_user = ?, lst_mod_ts = CURRENT_TIMESTAMP, lst_mod_chg_cd = 'LATEST'
            WHERE sal_uuid = ? AND version = ? AND status = 'AVAILABLE'
            """;
        int rows = jdbcTemplate.update(sql, user, salUuid, version);
        return rows > 0;
    }

    /**
     * Update after successful upload.
     */
    public boolean updateAfterUpload(UUID salUuid, Integer version, String checksum, 
                                     Long storedSize, String storagePath, String user) {
        String sql = """
            UPDATE sal_metadata 
            SET checksum = ?, stored_size_in_bytes = ?, storage_path = ?,
                status = 'AVAILABLE', is_latest = TRUE,
                lst_mod_user = ?, lst_mod_ts = CURRENT_TIMESTAMP, lst_mod_chg_cd = 'UPLOAD'
            WHERE sal_uuid = ? AND version = ?
            """;
        int rows = jdbcTemplate.update(sql, checksum, storedSize, storagePath, user, salUuid, version);
        return rows > 0;
    }

    /**
     * Search metadata with filters.
     */
    public List<SalMetadata> search(SearchRequest request) {
        StringBuilder sql = new StringBuilder("SELECT * FROM sal_metadata WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (request.getName() != null) {
            sql.append(" AND sal_name = ?");
            params.add(request.getName());
        }
        if (request.getNamePattern() != null) {
            sql.append(" AND sal_name LIKE ?");
            params.add("%" + request.getNamePattern() + "%");
        }
        if (request.getOwnerId() != null) {
            sql.append(" AND owner_id = ?");
            params.add(request.getOwnerId());
        }
        if (request.getType() != null) {
            sql.append(" AND sal_type = ?");
            params.add(request.getType());
        }
        if (request.getStatus() != null) {
            sql.append(" AND status = ?");
            params.add(request.getStatus());
        }
        if (Boolean.TRUE.equals(request.getLatestOnly())) {
            sql.append(" AND is_latest = TRUE");
        }
        if (request.getStorageType() != null) {
            sql.append(" AND storage_type = ?");
            params.add(request.getStorageType());
        }
        if (request.getCompressed() != null) {
            sql.append(" AND is_compressed = ?");
            params.add(request.getCompressed());
        }
        if (request.getMinSize() != null) {
            sql.append(" AND size_in_bytes >= ?");
            params.add(request.getMinSize());
        }
        if (request.getMaxSize() != null) {
            sql.append(" AND size_in_bytes <= ?");
            params.add(request.getMaxSize());
        }
        if (request.getModifiedAfter() != null) {
            sql.append(" AND lst_mod_ts >= ?");
            params.add(Timestamp.valueOf(request.getModifiedAfter()));
        }
        if (request.getModifiedBefore() != null) {
            sql.append(" AND lst_mod_ts <= ?");
            params.add(Timestamp.valueOf(request.getModifiedBefore()));
        }

        // Sorting
        String sortColumn = mapSortColumn(request.getSortBy());
        String sortDir = "DESC".equalsIgnoreCase(request.getSortDirection()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(sortColumn).append(" ").append(sortDir);

        // Pagination
        sql.append(" LIMIT ? OFFSET ?");
        params.add(request.getSize());
        params.add(request.getPage() * request.getSize());

        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    /**
     * Count search results.
     */
    public long countSearch(SearchRequest request) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sal_metadata WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (request.getName() != null) {
            sql.append(" AND sal_name = ?");
            params.add(request.getName());
        }
        if (request.getNamePattern() != null) {
            sql.append(" AND sal_name LIKE ?");
            params.add("%" + request.getNamePattern() + "%");
        }
        if (request.getOwnerId() != null) {
            sql.append(" AND owner_id = ?");
            params.add(request.getOwnerId());
        }
        if (Boolean.TRUE.equals(request.getLatestOnly())) {
            sql.append(" AND is_latest = TRUE");
        }
        if (request.getStatus() != null) {
            sql.append(" AND status = ?");
            params.add(request.getStatus());
        }

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0L;
    }

    private String mapSortColumn(String sortBy) {
        return switch (sortBy) {
            case "name" -> "sal_name";
            case "size" -> "size_in_bytes";
            case "createdAt" -> "created_ts";
            default -> "lst_mod_ts";
        };
    }

    private String toJsonString(JsonNode node) {
        if (node == null) return null;
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }

    private static class SalMetadataRowMapper implements RowMapper<SalMetadata> {
        private final ObjectMapper objectMapper;

        SalMetadataRowMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public SalMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
            SalMetadata m = new SalMetadata();
            m.setSalUuid(UUID.fromString(rs.getString("sal_uuid")));
            m.setVersion(rs.getInt("version"));
            m.setSalName(rs.getString("sal_name"));
            m.setSalDescription(rs.getString("sal_description"));
            m.setSalType(rs.getString("sal_type"));
            m.setSalMetadata(parseJson(rs.getString("sal_metadata")));
            m.setSizeInBytes(rs.getLong("size_in_bytes"));
            m.setStoredSizeInBytes(rs.getObject("stored_size_in_bytes") != null ? 
                rs.getLong("stored_size_in_bytes") : null);
            m.setChecksum(rs.getString("checksum"));
            m.setChecksumAlgorithm(rs.getString("checksum_algorithm"));
            m.setStatus(rs.getString("status"));
            m.setIsLatest(rs.getBoolean("is_latest"));
            m.setIsCompressed(rs.getBoolean("is_compressed"));
            m.setCompressionType(rs.getString("compression_type"));
            m.setStorageType(rs.getString("storage_type"));
            m.setStoragePath(rs.getString("storage_path"));
            m.setOwnerId(rs.getString("owner_id"));
            m.setLstModChgCd(rs.getString("lst_mod_chg_cd"));
            m.setLstModUser(rs.getString("lst_mod_user"));
            m.setLstModTs(rs.getTimestamp("lst_mod_ts").toLocalDateTime());
            m.setCreatedTs(rs.getTimestamp("created_ts").toLocalDateTime());
            m.setCreatedBy(rs.getString("created_by"));
            return m;
        }

        private JsonNode parseJson(String json) {
            if (json == null) return null;
            try {
                return objectMapper.readTree(json);
            } catch (JsonProcessingException e) {
                return null;
            }
        }
    }
}
