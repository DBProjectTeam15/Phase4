package knu.database.musicbase.controller;

import knu.database.musicbase.dto.MyInfoDto;
import knu.database.musicbase.dto.MyInfoUpdateDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MyInfoController {

    @GetMapping("/my")
    public ResponseEntity<MyInfoDto> getMyInfo() {
        return ResponseEntity.ok(
                MyInfoDto.builder().id(1L).username("demo nickname").build()
        );
    }

    @GetMapping("/manager/me")
    public ResponseEntity<MyInfoDto> getManagerInfo() {
        return ResponseEntity.ok(
                MyInfoDto.builder().id(1L).username("demo nickname").build()
        );
    }

    @PatchMapping("/my")
    public ResponseEntity<MyInfoDto> updateMyInfo(@RequestBody MyInfoUpdateDto myInfoUpdateDto) {
        var username = myInfoUpdateDto.getUsername();

        return ResponseEntity.ok(
                MyInfoDto.builder().id(1).username(username).build()
        );
    }
}
