package knu.database.musebase.dao.manager;

import knu.database.musebase.dao.BasicDataAccessObjectImpl;
import knu.database.musebase.data.Manager;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement; // PreparedStatement 임포트 추가
import java.sql.ResultSet; // ResultSet 임포트 추가
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ManagerDAO extends BasicDataAccessObjectImpl<Manager, String> {

    @Override
    public Manager save(Manager entity) {
        String sql = "INSERT INTO MANAGERS (MANAGER_ID, PASSWORD, NAME) VALUES (?, ?, ?)";
        Connection connection = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, entity.getManagerUsername());
                pstmt.setString(2, entity.getManagerPassword());
                pstmt.setString(3, entity.getManagerName());

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating manager failed, no rows affected.");
                }
            }

            connection.commit();
            return entity;

        } catch (SQLException ex) {
            log.error("Error saving manager: " + ex.getMessage(), ex);
            if (connection != null) {
                try {
                    connection.rollback();
                    log.info("Transaction rolled back.");
                } catch (SQLException e) {
                    log.error("Error during transaction rollback: " + e.getMessage(), e);
                }
            }
            return null;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    log.error("Error closing connection: " + e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public Optional<Manager> findById(String id) {
        String sql = "SELECT * FROM MANAGERS WHERE MANAGER_ID = ?";

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Manager(
                            rs.getString("MANAGER_ID"),
                            rs.getString("PASSWORD"),
                            rs.getString("NAME"))
                    );
                }
            }
            return Optional.empty();

        } catch (SQLException ex) {
            log.error("Error finding manager by id {}: {}", id, ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    @Override
    public List<Manager> findAll() {
        String sql = "SELECT * FROM MANAGERS";
        List<Manager> managers = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                managers.add(new Manager(
                        rs.getString("MANAGER_ID"),
                        rs.getString("PASSWORD"),
                        rs.getString("NAME")));
            }
            return managers;

        } catch (SQLException ex) {
            log.error("Error finding all managers: " + ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }

    public Optional<Manager> findByUsername(String username) {
        String sql = "SELECT * FROM MANAGERS WHERE MANAGER_ID = ?";

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 2. 컬럼명으로 접근
                    return Optional.of(new Manager(
                            rs.getString("MANAGER_ID"),
                            rs.getString("PASSWORD"),
                            rs.getString("NAME"))
                    );
                }
            }
            return Optional.empty();

        } catch (SQLException ex) {
            log.error("Error finding manager by username {}: {}", username, ex.getMessage(), ex);
            return Optional.empty();
        }
    }
}