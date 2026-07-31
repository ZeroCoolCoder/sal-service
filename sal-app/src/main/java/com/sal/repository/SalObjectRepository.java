package com.sal.repository;

import com.sal.domain.SalObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SAL_OBJECT table.
 */
@Repository
public class SalObjectRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SalObjectRowMapper rowMapper = new SalObjectRowMapper();

    public SalObjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Create a new object and return it with generated UUID.
     */
    public SalObject create(SalObject obj) {
        String sql = """
            INSERT INTO sal_object (
                owner_id, current_version, created_by, lst_mod_user
            ) VALUES (?, ?, ?, ?)
            RETURNING sal_uuid, created_ts, lst_mod_ts
            """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            obj.setSalUuid(UUID.fromString(rs.getString("sal_uuid")));
            obj.setCreatedTs(rs.getTimestamp("created_ts").toLocalDateTime());
            obj.setLstModTs(rs.getTimestamp("lst_mod_ts").toLocalDateTime());
            return obj;
        }, obj.getOwnerId(), obj.getCurrentVersion(), obj.getCreatedBy(), obj.getLstModUser());
    }

    /**
     * Find object by UUID.
     */
    public Optional<SalObject> findByUuid(UUID salUuid) {
        String sql = "SELECT * FROM sal_object WHERE sal_uuid = ?";
        List<SalObject> results = jdbcTemplate.query(sql, rowMapper, salUuid);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Update current version with optimistic locking.
     */
    public boolean updateCurrentVersion(UUID salUuid, Integer newVersion, 
                                        String user, Integer expectedVersionLock) {
        String sql = """
            UPDATE sal_object 
            SET current_version = ?, lst_mod_user = ?, lst_mod_ts = CURRENT_TIMESTAMP,
                version_lock = version_lock + 1
            WHERE sal_uuid = ? AND version_lock = ?
            """;
        int rows = jdbcTemplate.update(sql, newVersion, user, salUuid, expectedVersionLock);
        return rows > 0;
    }

    /**
     * Find objects by owner.
     */
    public List<SalObject> findByOwner(String ownerId) {
        String sql = "SELECT * FROM sal_object WHERE owner_id = ?";
        return jdbcTemplate.query(sql, rowMapper, ownerId);
    }

    /**
     * Delete object.
     */
    public boolean delete(UUID salUuid) {
        String sql = "DELETE FROM sal_object WHERE sal_uuid = ?";
        int rows = jdbcTemplate.update(sql, salUuid);
        return rows > 0;
    }

    /**
     * Check if object exists.
     */
    public boolean exists(UUID salUuid) {
        String sql = "SELECT COUNT(*) FROM sal_object WHERE sal_uuid = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, salUuid);
        return count != null && count > 0;
    }

    private static class SalObjectRowMapper implements RowMapper<SalObject> {
        @Override
        public SalObject mapRow(ResultSet rs, int rowNum) throws SQLException {
            SalObject obj = new SalObject();
            obj.setSalUuid(UUID.fromString(rs.getString("sal_uuid")));
            obj.setCurrentVersion(rs.getInt("current_version"));
            obj.setOwnerId(rs.getString("owner_id"));
            obj.setCreatedTs(rs.getTimestamp("created_ts").toLocalDateTime());
            obj.setCreatedBy(rs.getString("created_by"));
            obj.setLstModTs(rs.getTimestamp("lst_mod_ts").toLocalDateTime());
            obj.setLstModUser(rs.getString("lst_mod_user"));
            obj.setVersionLock(rs.getInt("version_lock"));
            return obj;
        }
    }
}
