package knu.database.musicbase.dao.manager;

import knu.database.musicbase.dao.BasicDataAccessObjectImpl;
import knu.database.musicbase.data.Manager;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        return executeQueryOne(sql, this::mapResultSetToManager, id);
    }

    @Override
    public List<Manager> findAll() {
        String sql = "SELECT * FROM MANAGERS";
        return executeQuery(sql, this::mapResultSetToManager);
    }

    public Optional<Manager> findByUsername(String username) {
        String sql = "SELECT * FROM MANAGERS WHERE MANAGER_ID = ?";
        return executeQueryOne(sql, this::mapResultSetToManager, username);
    }

    private Manager mapResultSetToManager(ResultSet rs) throws SQLException {
        return new Manager(
                rs.getString("MANAGER_ID"),
                rs.getString("PASSWORD"),
                rs.getString("NAME"));
    }
}