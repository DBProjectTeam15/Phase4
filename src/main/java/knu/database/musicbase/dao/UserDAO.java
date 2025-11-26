package knu.database.musicbase.dao;

import knu.database.musicbase.data.User;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * User DTO는 @AllArgsConstructor를 사용하며 Setter가 없습니다.
 * 따라서 save 메서드는 생성된 ID를 포함한 새 User 객체를 반환합니다.
 */
// CREATE TABLE USERS (
// User_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
// Nickname VARCHAR2(30) NOT NULL,
// Password VARCHAR2(30) NOT NULL,
// Email VARCHAR2(50) NOT NULL
// );
@Slf4j
public class UserDAO extends BasicDataAccessObjectImpl<User, Long> {

    /**
     * User를 저장하고, Oracle DB에 의해 생성된 ID를 포함한
     * *새로운* User 객체를 반환합니다.
     * (User_id가 IDENTITY 컬럼이라고 가정)
     */
    @Override
    public User save(User entity) throws SQLException {
        String sql = "INSERT INTO USERS (Nickname, Password, Email) VALUES (?, ?, ?)";

        // Oracle에서 생성된 키(User_id)를 반환받기 위해 설정
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, new String[] { "User_id" })) {

            pstmt.setString(1, entity.getNickname());
            pstmt.setString(2, entity.getPassword());
            pstmt.setString(3, entity.getEmail());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            // 생성된 ID (User_id)를 가져옵니다.
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long generatedId = generatedKeys.getLong(1);

                    // DTO가 불변(immutable)하므로, 새 ID로 새 객체를 생성하여 반환
                    return new User(
                            generatedId,
                            entity.getNickname(),
                            entity.getPassword(),
                            entity.getEmail());
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * 쿼리 1.1 사용 -> 수정사항 : 모든 데이터를 가져오도록 수정.
     * 기본 키(User_id)로 사용자를 찾습니다.
     */
    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT User_id, Nickname, Password, Email FROM USERS WHERE User_id = ?";
        return executeQueryOne(sql, this::mapResultSetToUser, id);
    }

    /**
     * 모든 사용자를 조회합니다.
     */
    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM USERS";
        return executeQuery(sql, this::mapResultSetToUser);
    }

    /**
     * Email (UNIQUE하다고 가정)로 사용자를 찾습니다.
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM USERS WHERE Email = ?";
        return executeQueryOne(sql, this::mapResultSetToUser, email);
    }

    /**
     * @param entity 업데이트할 데이터를 담은 User 객체
     * @return 영향을 받은 행의 수 (보통 1 또는 0)
     * @throws SQLException
     */
    public int update(User entity) throws SQLException {
        String sql = "UPDATE USERS SET Nickname = ?, Password = ?, Email = ? WHERE User_id = ?";
        return executeUpdate(sql, entity.getNickname(), entity.getPassword(), entity.getEmail(), entity.getUserId());
    }

    /**
     * 기본 키(User_id)로 사용자를 삭제합니다.
     */
    public void deleteByID(long loggedInId) {
        String sql = "DELETE FROM USERS WHERE User_id = ?";
        executeUpdate(sql, loggedInId);
    }

    /**
     * ResultSet의 현재 행을 User 객체로 매핑합니다.
     * (@AllArgsConstructor 생성자 사용)
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("User_id"),
                rs.getString("Nickname"),
                rs.getString("Password"),
                rs.getString("Email"));
    }
}