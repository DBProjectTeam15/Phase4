package knu.database.musicbase.controller;

import jakarta.servlet.http.HttpSession;
import knu.database.musicbase.dto.SongRequestDto;
import knu.database.musicbase.dto.SongRequestViewDto;
import knu.database.musicbase.dto.UserDto;
import knu.database.musicbase.enums.AuthType;
import knu.database.musicbase.repository.SongRequestRepository;
import knu.database.musicbase.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/song-requests")
@RestController
@RequiredArgsConstructor
public class SongRequestController {

    private final SongRequestRepository songRequestRepository;
    private final AuthService authService;

//    // 1. 요청 검색
//    @GetMapping("/search")
//    public List<SongRequestViewDto> searchRequests(
//            @RequestParam(required = false) String title,
//            @RequestParam(required = false) String artist,
//            @RequestParam(required = false) String manager, // 담당자 ID 검색
//            @RequestParam(required = false, defaultValue = "date") String sortBy,
//            @RequestParam(required = false, defaultValue = "desc") String sortOrder
//    ) {
//        return songRequestRepository.searchRequests(title, artist, manager, sortBy, sortOrder);
//    }

    @GetMapping
    public ResponseEntity<List<SongRequestDto>> getManagingSongRequests(HttpSession session) {
        if (authService.getAuthType(session) == AuthType.MANAGER) {
            UserDto userDto = authService.getLoggedInUser(session);

            return ResponseEntity.ok(songRequestRepository.findByManagerId(userDto.getId()));
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 3. 요청 삭제
    // 삭제된 정보를 반환하기 위해 ResponseEntity<SongRequestViewDto> 사용
    @DeleteMapping("/{id}")
    public ResponseEntity<SongRequestDto> deleteSongRequests(@PathVariable long id) {
        SongRequestDto deletedRequest = songRequestRepository.deleteSongRequest(id);

        if (deletedRequest != null) {
            return ResponseEntity.ok(deletedRequest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}