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
import java.util.ArrayList;
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
                                songRequest.getManagerUsername()
                        );
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

        // try-with-resources 구문을 사용하여 Connection과 PreparedStatement를 자동으로 닫습니다.
        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            // executeUpdate()는 쿼리에 의해 영향을 받은 행의 수를 반환합니다.
            return pstmt.executeUpdate();

        } catch (SQLException ex) {
            log.error("Error deleting song request with id {}: {}", id, ex.getMessage(), ex);
            // 오류 발생 시 0을 반환
            return 0;
        }
    }

    @Override
    public Optional<SongRequest> findById(Long id) {
        String sql = "SELECT * FROM SONG_REQUESTS WHERE request_id = ?";

        // 1. Connection, PreparedStatement, ResultSet을 모두 try-with-resources로 관리
        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                // 2. 결과가 있다면
                if (rs.next()) {
                    // 3. 헬퍼 메서드를 사용해 객체로 매핑하고 Optional.of()로 감싸 반환
                    return Optional.of(mapToSongRequest(rs));
                } else {
                    // 4. 결과가 없다면
                    return Optional.empty();
                }
            }
        } catch (SQLException ex) {
            log.error("Error finding song request by id {}: {}", id, ex.getMessage(), ex);
            return Optional.empty(); // 5. 예외 발생 시에도 Optional.empty() 반환
        }
    }

    @Override
    public List<SongRequest> findAll() {
        List<SongRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM SONG_REQUESTS";

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // 1. 결과가 없을 때까지 while 루프
            while (rs.next()) {
                // 2. 헬퍼 메서드를 사용해 객체로 매핑하고 리스트에 추가
                requests.add(mapToSongRequest(rs));
            }
        } catch (SQLException ex) {
            log.error("Error finding all song requests: {}", ex.getMessage(), ex);
            // 3. 예외 발생 시 비어있는 리스트 반환
        }
        return requests;
    }

    /**
     * 'save' 메서드의 구현을 바탕으로 manager_id가 String(username)이라고 가정합니다.
     * @param managerUsername 매니저의 username
     * @return 해당 매니저에게 할당된 악곡 요청 목록
     */
    public List<SongRequest> findByManagerId(String managerUsername) {
        List<SongRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM SONG_REQUESTS WHERE manager_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, managerUsername); // String 타입으로 설정

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapToSongRequest(rs));
                }
            }
        } catch (SQLException ex) {
            log.error("Error finding song requests by manager id {}: {}", managerUsername, ex.getMessage(), ex);
        }
        return requests;
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
                rs.getString("manager_id")
        );
    }
}