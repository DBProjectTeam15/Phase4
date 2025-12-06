package knu.database.musicbase.repository;

import knu.database.musicbase.dto.CommentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommentRepository {

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
}