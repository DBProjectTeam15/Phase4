package knu.database.musicbase.dao;

import knu.database.musicbase.data.Provider;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CREATE TABLE PROVIDERS (
 * Provider_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 * Provider_name VARCHAR2(30) NOT NULL,
 * Provider_link VARCHAR2(255) NOT NULL
 * );
 */
@Slf4j
public class ProviderDAO extends BasicDataAccessObjectImpl<Provider, Long> {

    @Override
    public Provider save(Provider entity) {
        String sql = "INSERT INTO PROVIDERS (Provider_name, Provider_link) VALUES (?, ?)";

        try (Connection connection = getConnection();

            PreparedStatement pstmt = connection.prepareStatement(sql, new String[] { "PROVIDER_ID" })) {

            pstmt.setString(1, entity.getName());
            pstmt.setString(2, entity.getLink());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating provider failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);

                    return new Provider(id, entity.getName(), entity.getLink());
                } else {
                    throw new SQLException("Creating provider failed, no ID obtained.");
                }
            }

        } catch (SQLException ex) {
            log.error("Error saving provider: " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public Optional<Provider> findById(Long id) {
        String sql = "SELECT * FROM PROVIDERS WHERE Provider_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToProvider(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException ex) {
            log.error("Error finding provider by id {}: {}", id, ex.getMessage(), ex);
            return Optional.empty(); // 오류 발생 시 null 대신 empty() 반환
        }
    }

    @Override
    public List<Provider> findAll() {
        String sql = "SELECT * FROM PROVIDERS";
        List<Provider> providers = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                providers.add(mapToProvider(rs));
            }

        } catch (SQLException ex) {
            log.error("Error finding all providers: " + ex.getMessage(), ex);
            // 오류 발생 시 null 대신 비어있는 리스트 반환
        }
        return providers;
    }

    public long deleteById(long id) {
        String sql = "DELETE FROM PROVIDERS WHERE PROVIDER_ID = ?";

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {


            pstmt.setLong(1, id);


            return pstmt.executeUpdate();

        } catch (SQLException ex) {

            log.error("Error deleting provider by id {}: {}", id, ex.getMessage(), ex);
            return 0;
        }
    }

    /**
     * ResultSet -> Provider 객체 매핑 헬퍼
     */
    private Provider mapToProvider(ResultSet rs) throws SQLException {
        return new Provider(
                rs.getLong("Provider_id"),
                rs.getString("Provider_name"),
                rs.getString("Provider_link")
        );
    }
}