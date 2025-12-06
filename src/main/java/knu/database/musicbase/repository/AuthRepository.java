package knu.database.musicbase.repository;

import knu.database.musicbase.dto.ManagerLoginDto;
import knu.database.musicbase.dto.UserLoginDto;
import jakarta.servlet.http.HttpSession; // Spring Boot 3.x (2.x라면 javax.servlet...)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

@Repository
public class AuthRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. 일반 유저 로그인
    public long findByUserId(UserLoginDto userLoginDto) {
        try {
            String sql = "SELECT User_id FROM USERS WHERE Email = ? AND Password = ?";
            Long userId = jdbcTemplate.queryForObject(sql, Long.class,
                    userLoginDto.getEmail(), userLoginDto.getPassword());
            if  (userId == null) {
                throw new EmptyResultDataAccessException(1);
            }

            return userId;
        }
        catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("User not found");
        }
    }

    // 2. 관리자 로그인
    public String findByManagerId(ManagerLoginDto managerLoginDto) {
        try {
            String sql = "SELECT Manager_id FROM MANAGERS WHERE Manager_id = ? AND Password = ?";

            String managerId = jdbcTemplate.queryForObject(sql, String.class,
                    managerLoginDto.getId(), managerLoginDto.getPassword());

            if (managerId == null) throw new IllegalArgumentException("Manager not found");

            return managerId;
        }
        catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Manager not found");
        }
    }

    // 3. 로그아웃 (공통)
    public boolean logout(HttpSession session) {
        if (session.getAttribute("id_type") == null) {
            return false; // 로그인 되어있지 않음
        }
        session.invalidate(); // 세션 날리기
        return true;
    }

    public boolean managerLogout(HttpSession session) {
        if (session.getAttribute("id_type") == null) {
            return false; // 로그인 되어있지 않음
        }
        session.invalidate(); // 세션 날리기
        return true;
    }


}