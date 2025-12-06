package knu.database.musicbase.controller;

import knu.database.musicbase.dto.ManagerLoginDto;
import knu.database.musicbase.dto.UserDto;
import knu.database.musicbase.dto.UserLoginDto;
import knu.database.musicbase.enums.AuthType;
import jakarta.servlet.http.HttpSession; // Spring Boot 버전 확인 필요
import knu.database.musicbase.service.AuthService;
import knu.database.musicbase.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class AuthController { //
    private final AuthService authService;
    private final UserService userService;

    // 유저 로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginDto userLoginDto, HttpSession session) {
        UserDto userDto = authService.login(userLoginDto, session);

        if (userDto != null) {
            return ResponseEntity.ok("Login Success"); // 200
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login Failed"); // 401
        }
    }

    // 관리자 로그인
    @PostMapping("/manager/login")
    public ResponseEntity<String> managerLogin(@RequestBody ManagerLoginDto managerLoginDto, HttpSession session) {
        UserDto userDto = authService.loginManager(managerLoginDto, session);

        if (userDto != null) {
            return ResponseEntity.ok("Manager Login Success"); // 200
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login Failed"); // 401
        }
    }

    // 유저 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        if (session == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not Logged In"); // 403
        }

        authService.logout(session);

        return ResponseEntity.ok("Logged Out");
    }

    // 관리자 로그아웃
    @PostMapping("/manager/logout")
    public ResponseEntity<String> managerLogout(HttpSession session) {
        if (session == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not Logged In"); // 403
        }

        authService.logout(session);

        return ResponseEntity.ok("Logged Out");
    }

    // 계정 삭제
    @DeleteMapping("/accounts")
    public ResponseEntity<String> deleteAccount(HttpSession session) {
        if (session == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not Logged In");
        }
        var userDto = authService.getLoggedInUser(session);
        var authType = authService.getAuthType(session);

        if (authType != AuthType.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot delete Manager");
        }

        boolean success = userService.deleteUser(userDto.getId());

        if (success) {
            return ResponseEntity.ok("Account Deleted");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found"); // 403
        }
    }
}
