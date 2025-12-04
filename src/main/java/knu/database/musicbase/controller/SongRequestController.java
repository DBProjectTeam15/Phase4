package knu.database.musicbase.controller;

import knu.database.musicbase.dto.SongRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/api/song-requests")
@RestController
public class SongRequestController {

    @GetMapping
    public ResponseEntity<List<SongRequestDto>> getSongRequests() {
        return ResponseEntity.ok(List.of(
                SongRequestDto.builder().id(1).title("song request").requestAt(LocalDateTime.now()).artist(1).requestUserId(1).build(),
                SongRequestDto.builder().id(2).title("song request").requestAt(LocalDateTime.now()).artist(1).requestUserId(1).build(),
                SongRequestDto.builder().id(3).title("song request").requestAt(LocalDateTime.now()).artist(1).requestUserId(1).build(),
                SongRequestDto.builder().id(4).title("song request").requestAt(LocalDateTime.now()).artist(1).requestUserId(1).build(),
                SongRequestDto.builder().id(5).title("song request").requestAt(LocalDateTime.now()).artist(1).requestUserId(1).build()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSongRequests(@PathVariable long id) {
        return ResponseEntity.ok().build();
    }
}
