package knu.database.musicbase.controller;

import jakarta.servlet.http.HttpSession;
import knu.database.musicbase.dto.CommentDto;
import knu.database.musicbase.dto.UserDto;
import knu.database.musicbase.repository.CommentRepository;
import knu.database.musicbase.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/comments")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final AuthService authService;

    // 1. 내가 작성한 댓글 보기
    // GET /api/comments
    @GetMapping
    public ResponseEntity<List<CommentDto>> getMyComments(HttpSession session) {
        UserDto userDto = authService.getLoggedInUser(session);
        if (userDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(commentRepository.findCommentsByUserId(userDto.getId()));
    }

    @GetMapping("/playlists/{playlistId}")
    public List<CommentDto> getPlaylistComments(@PathVariable long playlistId) {
        return commentRepository.findCommentsByPlaylistId(playlistId);
    }
}