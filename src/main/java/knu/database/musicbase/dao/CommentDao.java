package knu.database.musicbase.dao;

import knu.database.musicbase.dto.CommentDto;
import knu.database.musicbase.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CommentDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper: DB 결과를 DTO로 변환
    private final RowMapper<CommentDto> commentMapper = (rs, rowNum) ->
            CommentDto.builder()
                    .commentedAt(rs.getTimestamp("COMMENTED_AT").toLocalDateTime())
                    .userId(rs.getLong("USER_ID"))
                    .playlistId(rs.getLong("PLAYLIST_ID"))
                    .content(rs.getString("CONTENT"))
                    .build();

    // 1. 내가 작성한 댓글 보기 (세션 활용)
    public List<CommentDto> findCommentsByUserId(long id) {
        String sql = "SELECT * FROM COMMENTS WHERE User_id = ?";
        return jdbcTemplate.query(sql, commentMapper, id);
    }

    // 2. 특정 플레이리스트에 작성된 댓글 보기
    public List<CommentDto> findCommentsByPlaylistId(long playlistId) {
        String sql = "SELECT U.User_Id, C.Content, C.Commented_at, C.Playlist_Id " +
                "FROM COMMENTS C " +
                "JOIN USERS U ON C.User_id = U.User_id " +
                "WHERE C.Playlist_id = ? " +
                "ORDER BY C.Commented_at DESC";

        return jdbcTemplate.query(sql, commentMapper, playlistId);
    }

    // 3. 댓글 추가 (트랜잭션)
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CommentDto addComment(Long playlistId, Long userId, String content) {
        // 1. 플레이리스트 존재 확인
        String checkPlaylistSql = "SELECT COUNT(*) FROM PLAYLISTS WHERE PLAYLIST_ID = ?";
        Integer playlistExists = jdbcTemplate.queryForObject(checkPlaylistSql, Integer.class, playlistId);
        if (playlistExists == null || playlistExists == 0) {
            throw new EntityNotFoundException("플레이리스트를 찾을 수 없습니다. ID: " + playlistId);
        }

        // 2. 댓글 삽입 (Commented_at은 SYSDATE로 자동 생성)
        String insertSql = "INSERT INTO COMMENTS (USER_ID, PLAYLIST_ID, CONTENT, COMMENTED_AT) " +
                          "VALUES (?, ?, ?, SYSDATE)";
        jdbcTemplate.update(insertSql, userId, playlistId, content);

        // 3. 방금 추가된 댓글 조회 (가장 최근 댓글)
        String selectSql = "SELECT USER_ID, PLAYLIST_ID, CONTENT, COMMENTED_AT " +
                          "FROM COMMENTS " +
                          "WHERE USER_ID = ? AND PLAYLIST_ID = ? " +
                          "ORDER BY COMMENTED_AT DESC " +
                          "FETCH FIRST 1 ROWS ONLY";

        return jdbcTemplate.queryForObject(selectSql, commentMapper, userId, playlistId);
    }
}