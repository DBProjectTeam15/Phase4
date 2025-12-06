package knu.database.musicbase.controller;

import jakarta.servlet.http.HttpSession;
import knu.database.musicbase.dto.CommentDto;
import knu.database.musicbase.dto.UserDto;
import knu.database.musicbase.dao.CommentDao;
import knu.database.musicbase.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/comments")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentDao commentDao;
    private final AuthService authService;

    // 1. 내가 작성한 댓글 보기
    // GET /api/comments
    @GetMapping
    public ResponseEntity<List<CommentDto>> getMyComments(HttpSession session) {
        UserDto userDto = authService.getLoggedInUser(session);
        if (userDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(commentDao.findCommentsByUserId(userDto.getId()));
    }

    @GetMapping("/playlists/{playlistId}")
    public List<CommentDto> getPlaylistComments(@PathVariable long playlistId) {
        return commentDao.findCommentsByPlaylistId(playlistId);
    }

    // 2. 댓글 추가 (트랜잭션)
    @PostMapping("/playlists/{playlistId}")
    public ResponseEntity<?> addComment(
            @PathVariable Long playlistId,
            @RequestBody CommentRequest request,
            HttpSession session
    ) {
        // 세션에서 사용자 정보 가져오기
        UserDto userDto = authService.getLoggedInUser(session);
        if (userDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        // 댓글 추가
        CommentDto comment = commentDao.addComment(playlistId, userDto.getId(), request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    // 댓글 요청 DTO
    @Data
    static class CommentRequest {
        private String content;
    }
}