package knu.database.musicbase.controller;

import jakarta.servlet.http.HttpSession;
import knu.database.musicbase.dto.UserUpdateRequestDto;
import knu.database.musicbase.dto.UserDto;
import knu.database.musicbase.enums.AuthType;
import knu.database.musicbase.service.AuthService;
import knu.database.musicbase.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyInfoController {

    private final UserService userService;
    private final AuthService authService;

    // 내 정보 조회
    @GetMapping("/my")
    public ResponseEntity<UserDto> getMyInfo(HttpSession session) {
        UserDto myInfo = authService.getLoggedInUser(session);

        if (myInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(myInfo);
    }

    // 매니저용 정보 조회
    @GetMapping("/manager/me")
    public ResponseEntity<UserDto> getManagerInfo(HttpSession session) {
        UserDto managerInfo = authService.getLoggedInUser(session);
        AuthType authType = authService.getAuthType(session);
        if (authType == AuthType.MANAGER) {
            return ResponseEntity.ok(managerInfo);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 내 정보 수정
    @PatchMapping("/my")
    public ResponseEntity<UserDto> updateMyInfo(@RequestBody UserUpdateRequestDto userUpdateRequestDto, HttpSession session) {
        if (authService.getLoggedInUser(session) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String newUsername = userUpdateRequestDto.getUsername();

        UserDto updatedUserDto = userService.updateUsername(newUsername, session);

        return ResponseEntity.ok(updatedUserDto);
    }
}