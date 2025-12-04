package knu.database.musicbase.controller;

import knu.database.musicbase.dto.CommentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/api/comments")
@RestController
public class CommentController {

    @GetMapping
    public ResponseEntity<List<CommentDto>> getMyComments() {
        var commentDtos = new ArrayList<CommentDto>();

        for (int i=1; i<=5; ++i) {
            commentDtos.add(CommentDto.builder()
                    .userId(1L)
                    .commentedAt(LocalDateTime.now())
                    .playlistId(2L)
                    .content("demo comment" + i)
                    .build()
            );
        }

        return ResponseEntity.ok(commentDtos);
    }
}
