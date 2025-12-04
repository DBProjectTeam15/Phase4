package knu.database.musicbase.controller;

import knu.database.musicbase.dto.SongDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/songs")
@RestController
public class SongController {

    List<SongDto> demoSongs = List.of(
            SongDto.builder().id(1L).title("song title")
                    .playLink("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                    .artistName("artist").build(),
            SongDto.builder().id(1L).title("song title")
                    .playLink("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                    .artistName("artist").build(),
            SongDto.builder().id(1L).title("song title")
                    .playLink("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                    .artistName("artist").build(),
            SongDto.builder().id(1L).title("song title")
                    .playLink("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                    .artistName("artist").build(),
            SongDto.builder().id(1L).title("song title")
                    .playLink("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                    .artistName("artist").build()
    );

    @GetMapping("/search")
    public ResponseEntity<List<SongDto>> searchSongs(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(demoSongs);
    }

    @GetMapping
    public ResponseEntity<List<SongDto>> getAllSongs() {
        return ResponseEntity.ok(demoSongs);
    }
}
