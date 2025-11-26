package knu.database.musicbase.dao;

import knu.database.musicbase.infra.ConnectionManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BasicDataAccessObjectImpl<T, K extends Serializable> implements BasicDataAccessObject<T, K> {
    private final ConnectionManager connectionManager = ConnectionManager.getInstance();

    protected Connection getConnection() throws SQLException {
        try {
            return connectionManager.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("커넥션 풀로부터 커넥션 객체를 가져오는 중 오류가 발생했습니다.");
        }
    }

    @FunctionalInterface
    protected interface RowMapper<T> {
        T mapRow(java.sql.ResultSet rs) throws SQLException;
    }

    protected <R> java.util.List<R> executeQuery(String sql, RowMapper<R> mapper, Object... params) {
        java.util.List<R> results = new java.util.ArrayList<>();
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query: " + sql, e);
        }
        return results;
    }

    protected <R> java.util.Optional<R> executeQueryOne(String sql, RowMapper<R> mapper, Object... params) {
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return java.util.Optional.ofNullable(mapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query one: " + sql, e);
        }
        return java.util.Optional.empty();
    }

    protected int executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error executing update: " + sql, e);
        }
    }

    protected int executeCount(String sql, Object... params) {
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing count: " + sql, e);
        }
        return 0;
    }
}
