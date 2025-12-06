package knu.database.musicbase.repository;

import knu.database.musicbase.dto.ArtistDto;
import lombok.RequiredArgsConstructor;
import org.jooq.Require;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.dao.EmptyResultDataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ArtistRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<ArtistDto> searchArtists(String name, String gender, String role, String sortBy, String sortOrder) {
        StringBuilder sql = new StringBuilder("SELECT Artist_id, Name, Gender FROM ARTISTS a WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        // 1. 이름 검색 (부분 일치, 대소문자 무시)
        if (name != null && !name.trim().isEmpty()) {
            sql.append("AND LOWER(a.Name) LIKE LOWER(?) ");
            params.add("%" + name + "%");
        }

        // 2. 성별 검색 (완전 일치, 대소문자 무시)
        if (gender != null && !gender.trim().isEmpty()) {
            sql.append("AND UPPER(a.Gender) = UPPER(?) ");
            params.add(gender);
        }

        // 3. 역할(Role) 검색
        if (role != null && !role.trim().isEmpty()) {
            sql.append("AND EXISTS (SELECT 1 FROM MADE_BY mb ");
            sql.append("            WHERE mb.Artist_id = a.Artist_id ");
            sql.append("            AND LOWER(mb.Role) LIKE LOWER(?)) ");
            params.add("%" + role + "%");
        }

        // 4. 정렬 로직
        String orderByColumn = "a.Name"; // 기본값
        if ("gender".equalsIgnoreCase(sortBy)) {
            orderByColumn = "a.Gender";
        }

        String direction = "ASC";
        if ("desc".equalsIgnoreCase(sortOrder)) {
            direction = "DESC";
        }

        sql.append("ORDER BY ").append(orderByColumn).append(" ").append(direction);

        return jdbcTemplate.query(sql.toString(), params.toArray(), new ArtistRowMapper());
    }

    public ArtistDto findById(long id) {
        String sql = "SELECT Artist_id, Name, Gender FROM ARTISTS WHERE Artist_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new ArtistRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null; // 또는 예외 처리
        }
    }

    public ArtistDto save(ArtistDto artistDto) {
        String sql = "INSERT INTO ARTISTS (Name, Gender) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"Artist_id"});
            ps.setString(1, artistDto.getName());
            ps.setString(2, artistDto.getGender());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        long generatedId = (key != null) ? key.longValue() : -1;

        return ArtistDto.builder()
                .id(generatedId)
                .name(artistDto.getName())
                .gender(artistDto.getGender())
                .build();
    }

    public ArtistDto delete(long id) {
        ArtistDto artistToDelete = findById(id);

        if (artistToDelete != null) {
            String sql = "DELETE FROM ARTISTS WHERE Artist_id = ?";
            jdbcTemplate.update(sql, id);
        }

        return artistToDelete;
    }

    private static class ArtistRowMapper implements RowMapper<ArtistDto> {
        @Override
        public ArtistDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ArtistDto.builder()
                    .id(rs.getLong("Artist_id"))
                    .name(rs.getString("Name"))
                    .gender(rs.getString("gender"))
                    .build();
        }
    }
}