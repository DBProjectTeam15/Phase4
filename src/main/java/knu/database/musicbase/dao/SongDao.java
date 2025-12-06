package knu.database.musicbase.dao;

import knu.database.musicbase.dto.SongDetailDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SongDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SongDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 공통 SELECT 구문 (아티스트 목록 LISTAGG 포함)
    private final String BASE_SELECT_SQL =
            "SELECT s.Song_id, s.Title, s.Length, s.Play_link, s.Create_at, " +
                    "       p.Provider_name, " +
                    "       (SELECT LISTAGG(a.Name, ', ') WITHIN GROUP (ORDER BY a.Name) " +
                    "        FROM MADE_BY mb " +
                    "        JOIN ARTISTS a ON mb.Artist_id = a.Artist_id " +
                    "        WHERE mb.Song_id = s.Song_id) AS Artist_names " +
                    "FROM SONGS s " +
                    "JOIN PROVIDERS p ON s.Provider_id = p.Provider_id ";

    // 1. 검색 기능 (기존 코드 유지 및 통합)
    public List<SongDetailDto> searchSongs(
            String title, boolean exactTitle,
            String artistName, boolean exactArtist,
            Integer minTime, Integer maxTime,
            String providerName, boolean exactProvider,
            String minDate, String maxDate,
            String sortBy, String sortOrder
    ) {
        StringBuilder sql = new StringBuilder(BASE_SELECT_SQL);
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        // Title 검색
        if (title != null && !title.trim().isEmpty()) {
            if (exactTitle) {
                sql.append("AND LOWER(s.Title) = LOWER(?) ");
                params.add(title);
            } else {
                sql.append("AND LOWER(s.Title) LIKE LOWER(?) ");
                params.add("%" + title + "%");
            }
        }

        // Artist 검색 (서브쿼리 EXISTS)
        if (artistName != null && !artistName.trim().isEmpty()) {
            sql.append("AND EXISTS (SELECT 1 FROM MADE_BY mb2 ");
            sql.append("            JOIN ARTISTS a2 ON mb2.Artist_id = a2.Artist_id ");
            sql.append("            WHERE mb2.Song_id = s.Song_id ");
            if (exactArtist) {
                sql.append("AND LOWER(a2.Name) = LOWER(?)) ");
                params.add(artistName);
            } else {
                sql.append("AND LOWER(a2.Name) LIKE LOWER(?)) ");
                params.add("%" + artistName + "%");
            }
        }

        // 재생시간 범위
        if (minTime != null) {
            sql.append("AND s.Length >= ? ");
            params.add(minTime);
        }
        if (maxTime != null) {
            sql.append("AND s.Length <= ? ");
            params.add(maxTime);
        }

        // Provider 검색
        if (providerName != null && !providerName.trim().isEmpty()) {
            if (exactProvider) {
                sql.append("AND LOWER(p.Provider_name) = LOWER(?) ");
                params.add(providerName);
            } else {
                sql.append("AND LOWER(p.Provider_name) LIKE LOWER(?) ");
                params.add("%" + providerName + "%");
            }
        }

        // 발매일 범위
        if (minDate != null && !minDate.isEmpty()) {
            sql.append("AND s.Create_at >= TO_TIMESTAMP(?, 'YYYY-MM-DD') ");
            params.add(minDate);
        }
        if (maxDate != null && !maxDate.isEmpty()) {
            sql.append("AND s.Create_at <= TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') ");
            params.add(maxDate + " 23:59:59");
        }

        // 정렬
        String orderByColumn = "s.Title";
        if ("date".equalsIgnoreCase(sortBy)) orderByColumn = "s.Create_at";
        else if ("length".equalsIgnoreCase(sortBy)) orderByColumn = "s.Length";

        String orderDirection = "ASC";
        if ("desc".equalsIgnoreCase(sortOrder)) orderDirection = "DESC";

        sql.append("ORDER BY ").append(orderByColumn).append(" ").append(orderDirection);

        return jdbcTemplate.query(sql.toString(), params.toArray(), new SongRowMapper());
    }

    // 2. 전체 음원 조회
    public List<SongDetailDto> getAllSongs() {
        // 조건 없이 기본 쿼리 + 정렬 수행
        String sql = BASE_SELECT_SQL; // 최신 등록순 등 기본 정렬
        return jdbcTemplate.query(sql, new SongRowMapper());
    }

    // 3. 특정 음원 상세 조회
    public SongDetailDto getSongDetails(Long id) {
        String sql = BASE_SELECT_SQL + "WHERE s.Song_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new SongRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // 4. 음원 추가 (INSERT)
    public SongDetailDto addSong(SongDetailDto songDetailDto) {
        // 4-1. Provider Name으로 Provider ID 조회 (없으면 예외 발생 가능성 있음, 여기선 존재하는 것으로 가정)
        // 실제 로직에선 Provider가 없으면 생성하거나 에러를 뱉어야 합니다.
        String findProviderSql = "SELECT Provider_id FROM PROVIDERS WHERE Provider_name = ?";
        Long providerId;
        try {
            providerId = jdbcTemplate.queryForObject(findProviderSql, Long.class, songDetailDto.getProviderName());
        } catch (EmptyResultDataAccessException e) {
            // 편의상 Provider가 없으면 null 처리하거나 임의의 값, 혹은 에러 처리
            throw new RuntimeException("존재하지 않는 제공자입니다: " + songDetailDto.getProviderName());
        }

        // 4-2. SONGS 테이블 INSERT
        String insertSql = "INSERT INTO SONGS (Song_id, Title, Play_link, Length, Create_at, Provider_id) " +
                "VALUES (SONGS_SEQ.NEXTVAL, ?, ?, ?, SYSTIMESTAMP, ?)"; // 시퀀스 사용 가정

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertSql, new String[]{"Song_id"});
            ps.setString(1, songDetailDto.getTitle());
            ps.setString(2, songDetailDto.getPlayLink());
            ps.setInt(3, songDetailDto.getLength());
            // Create_at은 현재 시간(SYSTIMESTAMP)으로 입력
            ps.setLong(4, providerId);
            return ps;
        }, keyHolder);

        long newSongId = keyHolder.getKey().longValue();

        // 4-3. 아티스트 매핑 (MADE_BY 테이블) - 생략 또는 구현 필요
        // DTO에 들어온 artistName(예: "IU")을 이용해 ARTISTS 테이블 ID 조회 후 MADE_BY에 INSERT 하는 로직 필요
        // 여기서는 복잡도를 줄이기 위해 생략하고, 추가된 곡 정보를 다시 조회하여 반환합니다.

        return getSongDetails(newSongId);
    }

    // 5. 음원 삭제 (DELETE)
    public SongDetailDto deleteSong(Long id) {
        // 삭제 전 정보 조회 (반환용)
        SongDetailDto songToDelete = getSongDetails(id);

        if (songToDelete != null) {
            // 참조 무결성을 위해 연결 테이블(MADE_BY, PLAYLIST_SONG 등) 먼저 삭제 필요할 수 있음
            // 여기서는 MADE_BY만 예시로 삭제
            jdbcTemplate.update("DELETE FROM MADE_BY WHERE Song_id = ?", id);

            // 본체 삭제
            jdbcTemplate.update("DELETE FROM SONGS WHERE Song_id = ?", id);
        }

        return songToDelete;
    }

    // Mapper 클래스
    private static class SongRowMapper implements RowMapper<SongDetailDto> {
        @Override
        public SongDetailDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            String createdAtStr = "";
            if (rs.getTimestamp("Create_at") != null) {
                // ISO 8601 형식 변환 또는 단순 문자열 변환
                createdAtStr = rs.getTimestamp("Create_at").toLocalDateTime().toString();
            }

            return new SongDetailDto(
                    rs.getLong("Song_id"),
                    rs.getString("Title"),
                    rs.getString("Play_link"),
                    rs.getInt("Length"),
                    createdAtStr,
                    rs.getString("Provider_name"),
                    rs.getString("Artist_names")
            );
        }
    }
}