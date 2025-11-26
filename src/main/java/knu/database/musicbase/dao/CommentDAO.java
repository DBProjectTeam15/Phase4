package knu.database.musicbase.dao;

import knu.database.musicbase.data.Comment;
import knu.database.musicbase.data.CommentKey;
import knu.database.musicbase.data.CommentWithAuthor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

// ID 타입을 CommentKey로 되돌립니다.
// ID 타입을 CommentKey로 되돌립니다.
public class CommentDAO extends BasicDataAccessObjectImpl<Comment, CommentKey> {

    /**
     * Weak Entity 저장을 구현합니다.
     * 이제 변경된 스키마를 따르도록 변경했습니다.
     */
    @Override
    public Comment save(Comment entity) throws SQLException {
        String insertSql = "INSERT INTO COMMENTS (User_id, Playlist_id, Content, Commented_at) " +
                "VALUES (?, ?, ?, ?)";

        Connection conn = getConnection();
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            conn.setAutoCommit(false); // 트랜잭션 시작

            CommentKey key = entity.getId();

            long userId = key.getUserId();
            long playlistId = key.getPlaylistId();
            Timestamp commentedAt = new Timestamp(System.currentTimeMillis());

            // 2. 새 ID로 INSERT
            insertStmt.setLong(1, userId);
            insertStmt.setLong(2, playlistId);
            insertStmt.setString(4, entity.getContent());
            insertStmt.setTimestamp(5, commentedAt);

            int affectedRows = insertStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating comment failed, no rows affected.");
            }

            conn.commit(); // 트랜잭션 성공

            // 저장된 ID로 새 Comment 객체 반환
            CommentKey newKey = new CommentKey(commentedAt, userId, playlistId);
            return new Comment(newKey, entity.getContent());

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // 트랜잭션 실패
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw e; // 예외 다시 던지기
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // 커넥션 풀 반환 전 기본값 복원
                    conn.close(); // 커넥션 반환
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 복합 기본 키로 조회
     */
    @Override
    public Optional<Comment> findById(CommentKey id) {
        String sql = "SELECT * FROM COMMENTS WHERE User_id = ? AND Playlist_id = ? AND Comment_at = ?";
        return executeQueryOne(sql, this::mapResultSetToComment, id.getUserId(), id.getPlaylistId(), id.getCommentedAt());
    }

    @Override
    public List<Comment> findAll() {
        String sql = "SELECT * FROM COMMENTS";
        return executeQuery(sql, this::mapResultSetToComment);
    }

    /**
     * 쿼리 8.1 사용 (수정된 CommentWithAuthor DTO 기준)
     */
    public List<CommentWithAuthor> findByPlaylistWithAuthor(long playlistId) {
        String sql = "SELECT U.Nickname, C.Content, C.Commented_at " +
                "FROM COMMENTS C " +
                "JOIN USERS U ON C.User_id = U.User_id " +
                "WHERE C.Playlist_id = ? " +
                "ORDER BY C.Commented_at DESC";

        return executeQuery(sql, rs -> new CommentWithAuthor(
                rs.getString("Nickname"), // author
                rs.getString("Content"), // content
                rs.getTimestamp("Commented_at") // commentedAt
        ), playlistId);
    }

    public List<Comment> findByUserId(long userId) {
        String sql = "SELECT * FROM COMMENTS WHERE User_id = ?";
        return executeQuery(sql, this::mapResultSetToComment, userId);
    }

    private Comment mapResultSetToComment(ResultSet rs) throws SQLException {
        CommentKey key = new CommentKey(
                rs.getTimestamp("Commented_at"),
                rs.getLong("User_id"),
                rs.getLong("Playlist_id"));
        String content = rs.getString("Content");

        return new Comment(key, content);
    }
}