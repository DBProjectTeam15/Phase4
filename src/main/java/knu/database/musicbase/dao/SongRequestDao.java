package knu.database.musicbase.dao;

import knu.database.musicbase.dto.SongRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Component
public class SongRequestDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper: DB 결과를 DTO로 매핑
    private final RowMapper<SongRequestDto> requestMapper = (rs, rowNum) ->
            SongRequestDto.builder()
                    .id(rs.getLong("REQUEST_ID"))
                    .title(rs.getString("REQUEST_SONG_TITLE"))
                    .artist(rs.getString("REQUEST_SONG_ARTIST"))
                    .requestUserId(rs.getLong("USER_ID"))
                    .requestAt(rs.getTimestamp("REQUEST_AT").toLocalDateTime())
                    .build();

//    // 1. 요청 검색 (제목, 아티스트 이름, 담당자 등)
//    public List<SongRequestViewDto> searchRequests(String title, String artistName, String managerIdStr, String sortBy, String sortOrder) {
//        StringBuilder sql = new StringBuilder();
//
//        // 기본 쿼리: SONG_REQUESTS 테이블
//        // 아티스트 이름 검색을 위해 ARTISTS 테이블 조인이 필요할 수 있음
//        sql.append("SELECT sr.REQUEST_ID, sr.TITLE, sr.ARTIST_ID, sr.USER_ID, sr.CREATED_AT ");
//        sql.append("FROM SONG_REQUESTS sr ");
//
//        // 아티스트 이름으로 검색하는 경우 조인 추가
//        if (artistName != null && !artistName.trim().isEmpty()) {
//            sql.append("JOIN ARTISTS a ON sr.ARTIST_ID = a.ARTIST_ID ");
//        }
//
//        sql.append("WHERE 1=1 ");
//        List<Object> params = new ArrayList<>();
//
//        // 제목 검색
//        if (title != null && !title.trim().isEmpty()) {
//            sql.append("AND LOWER(sr.TITLE) LIKE LOWER(?) ");
//            params.add("%" + title + "%");
//        }
//
//        // 아티스트 이름 검색
//        if (artistName != null && !artistName.trim().isEmpty()) {
//            sql.append("AND LOWER(a.NAME) LIKE LOWER(?) ");
//            params.add("%" + artistName + "%");
//        }
//
//        // 담당자 ID 검색 (문자열로 들어오지만 DB가 숫자형이면 변환 필요, 여기선 String 매칭 가정)
//        if (managerIdStr != null && !managerIdStr.trim().isEmpty()) {
//            sql.append("AND sr.MANAGER_ID = ? "); // 정확한 ID 매칭 가정
//            params.add(managerIdStr);
//        }
//
//        // 정렬
//        String sortColumn = "sr.CREATED_AT"; // 기본값
//        if ("title".equalsIgnoreCase(sortBy)) sortColumn = "sr.TITLE";
//
//        String orderDirection = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
//
//        sql.append("ORDER BY ").append(sortColumn).append(" ").append(orderDirection);
//
//        return jdbcTemplate.query(sql.toString(), requestMapper, params.toArray());
//    }

    // 2. 내가 관리하는 요청 검색 (세션 기반)
    public List<SongRequestDto> findByManagerId(long managerId) {
        String mid = String.valueOf(managerId);
        String sql = "SELECT * " +
                "FROM SONG_REQUESTS " +
                "WHERE MANAGER_ID = ? ";

        return jdbcTemplate.query(sql, requestMapper, mid);
    }

    // 3. 단건 조회 (삭제 시 반환용)
    public SongRequestDto findById(long id) {
        String sql = "SELECT * FROM SONG_REQUESTS WHERE REQUEST_ID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, requestMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // 4. 요청 삭제
    public SongRequestDto deleteSongRequest(long id) {
        // 삭제될 데이터 조회
        var deletedDto = findById(id);

        if (deletedDto != null) {
            String sql = "DELETE FROM SONG_REQUESTS WHERE REQUEST_ID = ?";
            jdbcTemplate.update(sql, id);
        }

        return deletedDto;
    }
}