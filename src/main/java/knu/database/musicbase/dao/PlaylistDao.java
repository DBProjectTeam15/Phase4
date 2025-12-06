package knu.database.musicbase.dao;

import knu.database.musicbase.exception.EntityNotFoundException;
import knu.database.musicbase.dto.PlaylistDto;
import knu.database.musicbase.dto.SongDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlaylistDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // PlaylistDto 매퍼
    private final RowMapper<PlaylistDto> playlistMapper = (rs, rowNum) ->
            PlaylistDto.builder()
                    .id(rs.getLong("PLAYLIST_ID"))
                    .title(rs.getString("TITLE"))
                    .isCollaborative(rs.getString("IS_COLLABORATIVE"))
                    .ownerId(rs.getLong("OWNER_ID"))
                    .build();

    private final RowMapper<SongDto> songMapper = (rs, rowNum) ->
            SongDto.builder()
                    .id(rs.getLong("SONG_ID"))
                    .title(rs.getString("SONG_TITLE"))
                    .artistName(rs.getString("ARTIST_NAME"))
                    .playLink(rs.getString("PLAY_LINK"))
                    .build();

    // 1. 음원 수가 많은 플리 10개 조회
    public List<PlaylistDto> getTop10BySongCounts() {
        String sql = """
            SELECT p.PLAYLIST_ID, p.TITLE, p.IS_COLLABORATIVE, p.USER_ID AS OWNER_ID
            FROM PLAYLISTS p
                     LEFT JOIN CONSISTED_OF ps ON p.PLAYLIST_ID = ps.PLAYLIST_ID
            GROUP BY p.PLAYLIST_ID, p.TITLE, p.IS_COLLABORATIVE, p.USER_ID
            ORDER BY COUNT(ps.SONG_ID) DESC
                FETCH FIRST 10 ROWS ONLY
        """;
        return jdbcTemplate.query(sql, playlistMapper);
    }

    public PlaylistDto getPlaylist(Long playlistId) {
        String sql = "SELECT p.PLAYLIST_ID, p.TITLE, p.IS_COLLABORATIVE, p.USER_ID AS OWNER_ID " +
                "FROM PLAYLISTS p WHERE Playlist_id = ?";
        // 결과가 없으면 예외가 발생하므로 실제론 try-catch 처리가 좋습니다.
        return jdbcTemplate.queryForObject(sql, playlistMapper, playlistId);
    }

    public List<SongDto> getPlaylistDetails(Long playlistId) {
        String sql = "SELECT S.SONG_ID, S.TITLE AS SONG_TITLE, S.PLAY_LINK, A.NAME AS ARTIST_NAME " +
                "FROM CONSISTED_OF CO " +
                "JOIN SONGS S ON CO.SONG_ID = S.SONG_ID " +
                "LEFT JOIN MADE_BY MB ON S.SONG_ID = MB.SONG_ID AND MB.ROLE = 'Singer' " +
                "LEFT JOIN ARTISTS A ON MB.ARTIST_ID = A.ARTIST_ID " +
                "WHERE CO.PLAYLIST_ID = ?";

        // 결과가 없으면 예외가 발생하므로 실제론 try-catch 처리가 좋습니다.
        return jdbcTemplate.query(sql, songMapper, playlistId);
    }

    // 3. 특정 음악이 포함된 플리 조회
    public List<PlaylistDto> getPlaylistBySong(Long songId) {
        String sql = """
            SELECT DISTINCT p.PLAYLIST_ID, p.TITLE, p.IS_COLLABORATIVE, p.USER_ID AS OWNER_ID FROM PLAYLISTS p
            JOIN CONSISTED_OF ps ON p.PLAYLIST_ID = ps.PLAYLIST_ID
            WHERE ps.SONG_ID = ?
        """;
        return jdbcTemplate.query(sql, playlistMapper, songId);
    }

    // 4. 내가 소유한 플리 (세션 사용)
    public List<PlaylistDto> findPlaylistsByUserId(long userId) {
        String sql = "SELECT p.PLAYLIST_ID, p.TITLE, p.IS_COLLABORATIVE, p.USER_ID AS OWNER_ID FROM PLAYLISTS p WHERE p.USER_ID = ?";
        return jdbcTemplate.query(sql, playlistMapper, userId);
    }

    // 5. 공유된 플리 (현재 미구현 - 빈 리스트 반환)
    public List<PlaylistDto> findSharedPlaylists(long userId) {
        String sql = "SELECT P.PLAYLIST_ID, P.TITLE, P.IS_COLLABORATIVE, P.USER_ID AS OWNER_ID " +
                "FROM PLAYLISTS P " +
                "JOIN USERS U ON P.USER_ID = U.USER_ID " +
                "JOIN EDITS E ON P.PLAYLIST_ID = E.PLAYLIST_ID " +
                "WHERE E.USER_ID = ? AND P.USER_ID != ?";

        return jdbcTemplate.query(sql, playlistMapper, userId, userId);
    }

    // 6. 편집 가능한 플리 (세션 사용)
    // 조건: 내가 Owner이거나, PLAYLIST_MEMBER 테이블에서 내 권한이 'EDIT'인 경우
    public List<PlaylistDto> findEditablePlaylists(long userId) {
        String sql = "SELECT Playlist_id, Title, Is_collaborative, User_id AS OWNER_ID FROM PLAYLISTS WHERE User_id = ? " +
                "UNION " +
                "SELECT P.Playlist_id, P.Title, P.Is_collaborative, P.User_id AS OWNER_ID " +
                "FROM PLAYLISTS P " +
                "JOIN EDITS E ON P.Playlist_id = E.Playlist_id " +
                "WHERE E.User_id = ?";

        return jdbcTemplate.query(sql, playlistMapper, userId, userId);
    }

    // 7. 플리 검색 (동적 쿼리)
    public List<PlaylistDto> searchPlaylists(String title, Integer songCount, Integer songCountMax,
                                             Integer commentCount, Integer commentCountMax,
                                             String ownerNickname, Integer totalLength,
                                             String sortBy, String sortOrder) {
        StringBuilder sql = new StringBuilder("SELECT p.PLAYLIST_ID, p.TITLE, p.IS_COLLABORATIVE, p.USER_ID AS OWNER_ID FROM PLAYLISTS p ");
        // Owner 닉네임 검색이 필요할 경우 USERS 테이블 조인
        if (ownerNickname != null) {
            sql.append("JOIN USERS u ON p.USER_ID = u.USER_ID ");
        }

        sql.append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        // 제목 검색 (부분 일치)
        if (title != null) {
            sql.append("AND p.TITLE LIKE ? ");
            params.add("%" + title + "%");
        }

        // 소유자 닉네임 검색
        if (ownerNickname != null) {
            sql.append("AND u.NICKNAME LIKE ? ");
            params.add("%" + ownerNickname + "%");
        }

        // 최소 수록곡 수 (서브쿼리 활용)
        if (songCount != null) {
            sql.append("AND (SELECT COUNT(*) FROM CONSISTED_OF ps WHERE ps.PLAYLIST_ID = p.PLAYLIST_ID) >= ? ");
            params.add(songCount);
        }

        // 최대 수록곡 수
        if (songCountMax != null) {
            sql.append("AND (SELECT COUNT(*) FROM CONSISTED_OF ps WHERE ps.PLAYLIST_ID = p.PLAYLIST_ID) <= ? ");
            params.add(songCountMax);
        }

        // 최소 댓글 수
        if (commentCount != null) {
            sql.append("AND (SELECT COUNT(*) FROM COMMENTS pc WHERE pc.PLAYLIST_ID = p.PLAYLIST_ID) >= ? ");
            params.add(commentCount);
        }

        // 최대 댓글 수
        if (commentCountMax != null) {
            sql.append("AND (SELECT COUNT(*) FROM COMMENTS pc WHERE pc.PLAYLIST_ID = p.PLAYLIST_ID) <= ? ");
            params.add(commentCountMax);
        }

        // 최소 총 재생 시간 (초) - SONGS 테이블과 조인 필요
        if (totalLength != null) {
            sql.append("""
                AND (SELECT COALESCE(SUM(s.LENGTH), 0)
                     FROM CONSISTED_OF ps
                     JOIN SONGS s ON ps.SONG_ID = s.SONG_ID
                     WHERE ps.PLAYLIST_ID = p.PLAYLIST_ID) >= ?
            """);
            params.add(totalLength);
        }

        // 정렬 (SQL Injection 방지를 위해 화이트리스트 검사 권장)
        String sortColumn = "p.TITLE"; // 기본값
        if ("songCount".equals(sortBy)) sortColumn = "(SELECT COUNT(*) FROM CONSISTED_OF ps WHERE ps.PLAYLIST_ID = p.PLAYLIST_ID)";

        String order = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";

        sql.append("ORDER BY ").append(sortColumn).append(" ").append(order);

        return jdbcTemplate.query(sql.toString(), playlistMapper, params.toArray());
    }

    // 8. SELECT FOR UPDATE로 플레이리스트 조회 (동시성 제어용)
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PlaylistDto getPlaylistForUpdate(Long playlistId) {
        String sql = "SELECT p.PLAYLIST_ID, p.TITLE, p.IS_COLLABORATIVE, p.USER_ID AS OWNER_ID " +
                     "FROM PLAYLISTS p WHERE p.PLAYLIST_ID = ? FOR UPDATE";
        try {
            return jdbcTemplate.queryForObject(sql, playlistMapper, playlistId);
        } catch (Exception e) {
            throw new EntityNotFoundException("플레이리스트를 찾을 수 없습니다. ID: " + playlistId);
        }
    }

    // 9. 플레이리스트에 곡 추가 (트랜잭션 + 동시성 제어)
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addSongToPlaylist(Long playlistId, Long songId) {
        // 1. 플레이리스트 존재 확인 및 잠금
        getPlaylistForUpdate(playlistId);

        // 2. 곡 존재 확인
        String checkSongSql = "SELECT COUNT(*) FROM SONGS WHERE SONG_ID = ?";
        Integer songExists = jdbcTemplate.queryForObject(checkSongSql, Integer.class, songId);
        if (songExists == null || songExists == 0) {
            throw new EntityNotFoundException("곡을 찾을 수 없습니다. ID: " + songId);
        }

        // 3. 중복 체크
        String checkDuplicateSql = "SELECT COUNT(*) FROM CONSISTED_OF WHERE PLAYLIST_ID = ? AND SONG_ID = ?";
        Integer duplicateCount = jdbcTemplate.queryForObject(checkDuplicateSql, Integer.class, playlistId, songId);
        if (duplicateCount != null && duplicateCount > 0) {
            throw new DuplicateKeyException("이미 플레이리스트에 추가된 곡입니다.");
        }

        // 4. 곡 추가
        String insertSql = "INSERT INTO CONSISTED_OF (PLAYLIST_ID, SONG_ID) VALUES (?, ?)";
        jdbcTemplate.update(insertSql, playlistId, songId);
    }

    // 10. 플레이리스트에서 곡 삭제 (트랜잭션)
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeSongFromPlaylist(Long playlistId, Long songId) {
        // 플레이리스트 존재 확인 및 잠금
        getPlaylistForUpdate(playlistId);

        // 곡 삭제
        String deleteSql = "DELETE FROM CONSISTED_OF WHERE PLAYLIST_ID = ? AND SONG_ID = ?";
        int rowsAffected = jdbcTemplate.update(deleteSql, playlistId, songId);

        if (rowsAffected == 0) {
            throw new EntityNotFoundException("플레이리스트에 해당 곡이 존재하지 않습니다.");
        }
    }
}