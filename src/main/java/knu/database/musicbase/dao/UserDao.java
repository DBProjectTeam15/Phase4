package knu.database.musicbase.dao;

import knu.database.musicbase.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
public class UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 일반 사용자 매퍼
    private final RowMapper<UserDto> userRowMapper = (rs, rowNum) ->
            UserDto.builder()
                    .id(rs.getLong("USER_ID"))
                    .username(rs.getString("NICKNAME"))
                    .build();

    // 관리자 매퍼 (NICKNAME -> username)
    private final RowMapper<UserDto> managerRowMapper = (rs, rowNum) ->
            UserDto.builder()
                    .id(Long.parseLong(rs.getString("MANAGER_ID")))
                    .username(rs.getString("NAME"))
                    .build();

    // [세션에서 ID 추출하여 조회]
    public UserDto findUserInfoById(long id) {
        try {
            String sql = "SELECT USER_ID, NICKNAME FROM USERS WHERE USER_ID = ?";
            return jdbcTemplate.queryForObject(sql, userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // [세션에서 관리자 ID 추출하여 조회]
    public UserDto findUserInfoByIdFromManager(String id) {
        try {
            String sql = "SELECT MANAGER_ID, NAME FROM MANAGERS WHERE MANAGER_ID = ?";
            return jdbcTemplate.queryForObject(sql, managerRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // [세션 ID로 정보 수정]
    public UserDto updateMyInfo(long id, String newUsername) {
        String updateSql = "UPDATE USERS SET USERNAME = ? WHERE USER_ID = ?";
        jdbcTemplate.update(updateSql, newUsername, id);
        // 수정된 정보 반환 (재조회 로직 재사용)
        return findUserInfoById(id);
    }

    // 4. 계정 삭제
    public void deleteAccount(long id) {
        // DB 삭제
        String sql = "DELETE FROM USERS WHERE User_id = ?";
        int updated = jdbcTemplate.update(sql, id);

        if (updated <= 0) {
            throw new IllegalArgumentException("User not found");
        }
    }
}