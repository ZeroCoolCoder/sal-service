package com.sal.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sal.domain.SalMetadataHist;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Repository for SAL_METADATA_HIST table (append-only).
 */
@Repository
public class SalMetadataHistRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final SalMetadataHistRowMapper rowMapper;

    public SalMetadataHistRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.rowMapper = new SalMetadataHistRowMapper(objectMapper);
    }

    /**
     * Create history entry (append-only).
     */
    public Long create(SalMetadataHist hist) {
        String sql = """
            INSERT INTO sal_metadata_hist (
                hist_action, hist_user, hist_reason,
                sal_uuid, version, sal_name, sal_description, sal_type, sal_metadata,
                size_in_bytes, stored_size_in_bytes, checksum, checksum_algorithm,
                status, is_latest, is_compressed, compression_type,
                storage_type, storage_path, owner_id,
                lst_mod_chg_cd, lst_mod_user, lst_mod_ts, created_ts, created_by
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING hist_id
            """;

        return jdbcTemplate.queryForObject(sql, Long.class,
            hist.getHistAction(),
            hist.getHistUser(),
            hist.getHistReason(),
            hist.getSalUuid(),
            hist.getVersion(),
            hist.getSalName(),
            hist.getSalDescription(),
            hist.getSalType(),
            toJsonString(hist.getSalMetadata()),
            hist.getSizeInBytes(),
            hist.getStoredSizeInBytes(),
            hist.getChecksum(),
            hist.getChecksumAlgorithm(),
            hist.getStatus(),
            hist.getIsLatest(),
            hist.getIsCompressed(),
            hist.getCompressionType(),
            hist.getStorageType(),
            hist.getStoragePath(),
            hist.getOwnerId(),
            hist.getLstModChgCd(),
            hist.getLstModUser(),
            hist.getLstModTs(),
            hist.getCreatedTs(),
            hist.getCreatedBy()
        );
    }

    /**
     * Find history by UUID.
     */
    public List<SalMetadataHist> findByUuid(UUID salUuid) {
        String sql = "SELECT * FROM sal_metadata_hist WHERE sal_uuid = ? ORDER BY hist_ts DESC";
        return jdbcTemplate.query(sql, rowMapper, salUuid);
    }

    /**
     * Find history by UUID and version.
     */
    public List<SalMetadataHist> findByUuidAndVersion(UUID salUuid, Integer version) {
        String sql = """
            SELECT * FROM sal_metadata_hist 
            WHERE sal_uuid = ? AND version = ? 
            ORDER BY hist_ts DESC
            """;
        return jdbcTemplate.query(sql, rowMapper, salUuid, version);
    }

    private String toJsonString(JsonNode node) {
        if (node == null) return null;
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }

    private static class SalMetadataHistRowMapper implements RowMapper<SalMetadataHist> {
        private final ObjectMapper objectMapper;

        SalMetadataHistRowMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public SalMetadataHist mapRow(ResultSet rs, int rowNum) throws SQLException {
            SalMetadataHist h = new SalMetadataHist();
            h.setHistId(rs.getLong("hist_id"));
            h.setHistTs(rs.getTimestamp("hist_ts").toLocalDateTime());
            h.setHistAction(rs.getString("hist_action"));
            h.setHistUser(rs.getString("hist_user"));
            h.setHistReason(rs.getString("hist_reason"));
            h.setSalUuid(UUID.fromString(rs.getString("sal_uuid")));
            h.setVersion(rs.getInt("version"));
            h.setSalName(rs.getString("sal_name"));
            h.setSalDescription(rs.getString("sal_description"));
            h.setSalType(rs.getString("sal_type"));
            h.setSalMetadata(parseJson(rs.getString("sal_metadata")));
            h.setSizeInBytes(rs.getLong("size_in_bytes"));
            h.setStoredSizeInBytes(rs.getObject("stored_size_in_bytes") != null ? 
                rs.getLong("stored_size_in_bytes") : null);
            h.setChecksum(rs.getString("checksum"));
            h.setChecksumAlgorithm(rs.getString("checksum_algorithm"));
            h.setStatus(rs.getString("status"));
            h.setIsLatest(rs.getBoolean("is_latest"));
            h.setIsCompressed(rs.getBoolean("is_compressed"));
            h.setCompressionType(rs.getString("compression_type"));
            h.setStorageType(rs.getString("storage_type"));
            h.setStoragePath(rs.getString("storage_path"));
            h.setOwnerId(rs.getString("owner_id"));
            h.setLstModChgCd(rs.getString("lst_mod_chg_cd"));
            h.setLstModUser(rs.getString("lst_mod_user"));
            h.setLstModTs(rs.getTimestamp("lst_mod_ts").toLocalDateTime());
            h.setCreatedTs(rs.getTimestamp("created_ts").toLocalDateTime());
            h.setCreatedBy(rs.getString("created_by"));
            return h;
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
