package knu.database.musicbase.dao;

import knu.database.musicbase.dto.ProviderDto;
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
public class ProviderDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ProviderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. 제공원 검색
    public List<ProviderDto> searchProviders(String name, String link, String sortBy, String sortOrder) {
        StringBuilder sql = new StringBuilder("SELECT Provider_id, Provider_name, Provider_link FROM PROVIDERS p WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        // 이름 검색 (부분 일치)
        if (name != null && !name.trim().isEmpty()) {
            sql.append("AND LOWER(p.Provider_name) LIKE LOWER(?) ");
            params.add("%" + name + "%");
        }

        // 링크 검색 (부분 일치)
        if (link != null && !link.trim().isEmpty()) {
            sql.append("AND LOWER(p.Provider_link) LIKE LOWER(?) ");
            params.add("%" + link + "%");
        }

        // 정렬 로직
        String orderByColumn = "p.Provider_name"; // 기본값
        if ("link".equalsIgnoreCase(sortBy)) {
            orderByColumn = "p.Provider_link";
        }

        String direction = "ASC";
        if ("desc".equalsIgnoreCase(sortOrder)) {
            direction = "DESC";
        }

        sql.append("ORDER BY ").append(orderByColumn).append(" ").append(direction);

        return jdbcTemplate.query(sql.toString(), params.toArray(), new ProviderRowMapper());
    }

    // 2. ID로 제공원 단건 조회
    public ProviderDto getProviderDetails(long id) {
        String sql = "SELECT Provider_id, Provider_name, Provider_link FROM PROVIDERS WHERE Provider_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new ProviderRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // 2. ID로 제공원 단건 조회
    public List<ProviderDto> findAll() {
        String sql = "SELECT Provider_id, Provider_name, Provider_link FROM PROVIDERS";
        try {
            return jdbcTemplate.query(sql, new ProviderRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // 3. 제공원 추가 (KeyHolder 사용)
    public ProviderDto addProvider(ProviderDto providerDto) {
        String sql = "INSERT INTO PROVIDERS (Provider_name, Provider_link) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            // DB 컬럼명이 "Provider_id"임을 명시
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"Provider_id"});
            ps.setString(1, providerDto.getName());
            ps.setString(2, providerDto.getLink());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        long generatedId = (key != null) ? key.longValue() : -1;

        // DTO 반환 (Builder 패턴 사용)
        return ProviderDto.builder()
                .id(generatedId)
                .name(providerDto.getName())
                .link(providerDto.getLink())
                .build();
    }

    // 4. 제공원 삭제
    public ProviderDto deleteProvider(long id) {
        ProviderDto providerToDelete = getProviderDetails(id);

        if (providerToDelete != null) {
            String sql = "DELETE FROM PROVIDERS WHERE Provider_id = ?";
            jdbcTemplate.update(sql, id);
        }

        return providerToDelete;
    }

    // RowMapper (Builder 패턴 사용)
    private static class ProviderRowMapper implements RowMapper<ProviderDto> {
        @Override
        public ProviderDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ProviderDto.builder()
                    .id(rs.getLong("Provider_id"))
                    .name(rs.getString("Provider_name"))
                    .link(rs.getString("Provider_link"))
                    .build();
        }
    }
}