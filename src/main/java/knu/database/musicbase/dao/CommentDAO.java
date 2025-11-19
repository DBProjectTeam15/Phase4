package knu.database.musicbase.dao;

import knu.database.musicbase.data.Comment;
import knu.database.musicbase.data.CommentKey;
import knu.database.musicbase.data.CommentWithAuthor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// ID 타입을 CommentKey로 되돌립니다.
public class CommentDAO extends BasicDataAccessObjectImpl<Comment, CommentKey> {

    /**
     * Weak Entity 저장을 구현합니다.
     * 1. (UserId, PlaylistId)에 해당하는 현재 Comment_id의 최대값을 조회합니다. (FOR UPDATE로 Lock)
     * 2. (최대값 + 1)을 새 Comment_id로 하여 INSERT를 수행합니다.
     * 3. 트랜잭션으로 묶여야 합니다.
     */
    @Override
    public Comment save(Comment entity) throws SQLException {
        String nextIdSql = "SELECT COALESCE(MAX(Comment_id), 0) + 1 AS next_id FROM COMMENTS " +
                "WHERE User_id = ? AND Playlist_id = ? FOR UPDATE";

        String insertSql = "INSERT INTO COMMENTS (User_id, Playlist_id, Comment_id, Content, Commented_at) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = getConnection();
        try (PreparedStatement idStmt = conn.prepareStatement(nextIdSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            conn.setAutoCommit(false); // 트랜잭션 시작

            CommentKey key = entity.getId();
            long userId = key.getUserId();
            long playlistId = key.getPlaylistId();

            // 1. 다음 ID 조회 (FOR UPDATE)
            idStmt.setLong(1, userId);
            idStmt.setLong(2, playlistId);

            long nextCommentId = 1; // 기본값
            try (ResultSet rs = idStmt.executeQuery()) {
                if (rs.next()) {
                    nextCommentId = rs.getLong("next_id");
                }
            }

            // 2. 새 ID로 INSERT
            insertStmt.setLong(1, userId);
            insertStmt.setLong(2, playlistId);
            insertStmt.setLong(3, nextCommentId);
            insertStmt.setString(4, entity.getContent());
            insertStmt.setTimestamp(5, entity.getCommentedAt());

            int affectedRows = insertStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating comment failed, no rows affected.");
            }

            conn.commit(); // 트랜잭션 성공

            // 저장된 ID로 새 Comment 객체 반환
            CommentKey newKey = new CommentKey(nextCommentId, userId, playlistId);
            return new Comment(newKey, entity.getContent(), entity.getCommentedAt());

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
        String sql = "SELECT * FROM COMMENTS WHERE User_id = ? AND Playlist_id = ? AND Comment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id.getUserId());
            pstmt.setLong(2, id.getPlaylistId());
            pstmt.setLong(3, id.getId());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToComment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Comment> findAll() {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM COMMENTS";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                comments.add(mapResultSetToComment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    /**
     * 쿼리 8.1 사용 (수정된 CommentWithAuthor DTO 기준)
     */
    public List<CommentWithAuthor> findByPlaylistWithAuthor(long playlistId) {
        List<CommentWithAuthor> comments = new ArrayList<>();
        String sql = "SELECT U.Nickname, C.Content, C.Commented_at " +
                "FROM COMMENTS C " +
                "JOIN USERS U ON C.User_id = U.User_id " +
                "WHERE C.Playlist_id = ? " +
                "ORDER BY C.Commented_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, playlistId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(new CommentWithAuthor(
                            rs.getString("Nickname"), // author
                            rs.getString("Content"),  // content
                            rs.getTimestamp("Commented_at") // commentedAt
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public List<Comment> findByUserId(long userId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM COMMENTS WHERE User_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    private Comment mapResultSetToComment(ResultSet rs) throws SQLException {
        CommentKey key = new CommentKey(
                rs.getLong("Comment_id"),
                rs.getLong("User_id"),
                rs.getLong("Playlist_id")
        );
        String content = rs.getString("Content");
        Timestamp commentedAt = rs.getTimestamp("Commented_at");

        return new Comment(key, content, commentedAt);
    }
}