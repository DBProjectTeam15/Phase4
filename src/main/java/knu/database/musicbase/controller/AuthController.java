package knu.database.musicbase.controller;

import knu.database.musicbase.dto.ManagerLoginDto;
import knu.database.musicbase.dto.UserDto;
import knu.database.musicbase.dto.UserLoginDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/")
@RestController
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody UserLoginDto userLoginDto) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/manager/login")
    public ResponseEntity<Void> managerLogin(@RequestBody ManagerLoginDto managerLoginDto) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/manager/logout")
    public ResponseEntity<Void> managerLogout() {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/accounts")
    public ResponseEntity<Void> deleteAccount() {
        return ResponseEntity.ok().build();
    }
}
