package knu.database.musicbase.dao.manager;

import knu.database.musicbase.dao.BasicDataAccessObjectImpl;
import knu.database.musicbase.data.SongRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class SongRequestDAO extends BasicDataAccessObjectImpl<SongRequest, Long> {

    @Override
    public SongRequest save(SongRequest songRequest) {
        String sql = "INSERT INTO SONG_REQUESTS (" +
                "request_song_title, request_at, request_song_artist, " +
                "user_id, manager_id) VALUES (?, ?, ?, ?, ?)";

        Connection connection = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false); // 1. 트랜잭션 시작
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, songRequest.getTitle());
                pstmt.setTimestamp(2, songRequest.getRequestAt());
                pstmt.setString(3, songRequest.getArtist());
                pstmt.setLong(4, songRequest.getUserId());
                pstmt.setString(5, songRequest.getManagerUsername()); // manager_id는 String 타입으로 가정

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating song request failed, no rows affected.");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        connection.commit();
                        return new SongRequest(
                                id,
                                songRequest.getTitle(),
                                songRequest.getRequestAt(),
                                songRequest.getArtist(),
                                songRequest.getUserId(),
                                songRequest.getManagerUsername());
                    } else {
                        throw new SQLException("Creating song request failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException ex) {
            log.error("Error saving song request: " + ex.getMessage(), ex);
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

    public long deleteById(Long id) {
        String sql = "DELETE FROM SONG_REQUESTS WHERE request_id = ?";
        return executeUpdate(sql, id);
    }

    @Override
    public Optional<SongRequest> findById(Long id) {
        String sql = "SELECT * FROM SONG_REQUESTS WHERE request_id = ?";
        return executeQueryOne(sql, this::mapToSongRequest, id);
    }

    @Override
    public List<SongRequest> findAll() {
        String sql = "SELECT * FROM SONG_REQUESTS";
        return executeQuery(sql, this::mapToSongRequest);
    }

    /**
     * 'save' 메서드의 구현을 바탕으로 manager_id가 String(username)이라고 가정합니다.
     * 
     * @param managerUsername 매니저의 username
     * @return 해당 매니저에게 할당된 악곡 요청 목록
     */
    public List<SongRequest> findByManagerId(String managerUsername) {
        String sql = "SELECT * FROM SONG_REQUESTS WHERE manager_id = ?";
        return executeQuery(sql, this::mapToSongRequest, managerUsername);
    }

    /**
     * ResultSet의 현재 행을 SongRequest 객체로 매핑하는 헬퍼 메서드
     */
    private SongRequest mapToSongRequest(ResultSet rs) throws SQLException {
        return new SongRequest(
                rs.getLong("request_id"),
                rs.getString("request_song_title"),
                rs.getTimestamp("request_at"),
                rs.getString("request_song_artist"),
                rs.getLong("user_id"),
                rs.getString("manager_id"));
    }
}