package com.sal.repository;

import com.sal.domain.SalBinaryContent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SAL_BINARY_CONTENT table.
 * Used only for DATABASE storage provider.
 */
@Repository
public class SalBinaryContentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SalBinaryContentRowMapper rowMapper = new SalBinaryContentRowMapper();

    public SalBinaryContentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Create binary content entry.
     */
    public void create(SalBinaryContent content) {
        String sql = """
            INSERT INTO sal_binary_content (
                sal_uuid, version, content_data, content_size, created_by
            ) VALUES (?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(sql,
            content.getSalUuid(),
            content.getVersion(),
            content.getContentData(),
            content.getContentSize(),
            content.getCreatedBy()
        );
    }

    /**
     * Find binary content by UUID and version.
     */
    public Optional<SalBinaryContent> findByUuidAndVersion(UUID salUuid, Integer version) {
        String sql = "SELECT * FROM sal_binary_content WHERE sal_uuid = ? AND version = ?";
        List<SalBinaryContent> results = jdbcTemplate.query(sql, rowMapper, salUuid, version);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Get content data only (for streaming).
     */
    public Optional<byte[]> getContentData(UUID salUuid, Integer version) {
        String sql = "SELECT content_data FROM sal_binary_content WHERE sal_uuid = ? AND version = ?";
        List<byte[]> results = jdbcTemplate.query(sql, 
            (rs, rowNum) -> rs.getBytes("content_data"), salUuid, version);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Delete binary content.
     */
    public boolean delete(UUID salUuid, Integer version) {
        String sql = "DELETE FROM sal_binary_content WHERE sal_uuid = ? AND version = ?";
        int rows = jdbcTemplate.update(sql, salUuid, version);
        return rows > 0;
    }

    /**
     * Check if content exists.
     */
    public boolean exists(UUID salUuid, Integer version) {
        String sql = "SELECT COUNT(*) FROM sal_binary_content WHERE sal_uuid = ? AND version = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, salUuid, version);
        return count != null && count > 0;
    }

    private static class SalBinaryContentRowMapper implements RowMapper<SalBinaryContent> {
        @Override
        public SalBinaryContent mapRow(ResultSet rs, int rowNum) throws SQLException {
            SalBinaryContent c = new SalBinaryContent();
            c.setSalUuid(UUID.fromString(rs.getString("sal_uuid")));
            c.setVersion(rs.getInt("version"));
            c.setContentData(rs.getBytes("content_data"));
            c.setContentSize(rs.getLong("content_size"));
            c.setCreatedTs(rs.getTimestamp("created_ts").toLocalDateTime());
            c.setCreatedBy(rs.getString("created_by"));
            return c;
        }
    }
}
