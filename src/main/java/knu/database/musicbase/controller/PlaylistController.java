package knu.database.musicbase.controller;

import knu.database.musicbase.dto.PlaylistDto;
import knu.database.musicbase.dto.SongDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/playlists")
@RestController
public class PlaylistController {
    List<PlaylistDto> demoPlaylists = List.of(
            PlaylistDto.builder().id(1).title("demo title").isCollaborative("False").ownerId(1).build(),
            PlaylistDto.builder().id(2).title("demo title").isCollaborative("False").ownerId(1).build(),
            PlaylistDto.builder().id(3).title("demo title").isCollaborative("False").ownerId(1).build(),
            PlaylistDto.builder().id(4).title("demo title").isCollaborative("False").ownerId(1).build(),
            PlaylistDto.builder().id(5).title("demo title").isCollaborative("False").ownerId(1).build()
    );

    @GetMapping("/top10")
    public ResponseEntity<List<PlaylistDto>> getTop10BySongCounts() {
        return ResponseEntity.ok(demoPlaylists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistDto> getPlaylistDetails(@PathVariable String id) {
        return ResponseEntity.ok(demoPlaylists.get(0));
    }

    @GetMapping("/{id}/songs")
    public ResponseEntity<SongDto> getPlaylistSongs(@PathVariable String id) {
        return ResponseEntity.ok(
                SongDto.builder()
                        .id(1L)
                        .title("song title")
                        .playLink("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                        .artistName("artist")
                        .build()
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<PlaylistDto>> getMyPlaylists() {
        return ResponseEntity.ok(demoPlaylists);
    }

    @GetMapping("/shared")
    public ResponseEntity<List<PlaylistDto>>     getSharedPlaylists() {
        return ResponseEntity.ok(demoPlaylists);
    }

    @GetMapping("/editable")
    public ResponseEntity<List<PlaylistDto>> getEditablePlaylists() {
        return ResponseEntity.ok(demoPlaylists);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlaylistDto>> searchPlaylists() {
        return ResponseEntity.ok(demoPlaylists);
    }
}
