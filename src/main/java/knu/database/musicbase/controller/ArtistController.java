package knu.database.musicbase.controller;

import knu.database.musicbase.dto.ArtistDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/api/artists")
@RestController
public class ArtistController {
    @GetMapping("/search")
    public ResponseEntity<List<ArtistDto>> searchArtists(@RequestParam(required = false) String search) {

        List<ArtistDto> artists = new ArrayList<>();

        for (int i=1; i<=5; ++i) {
            artists.add(ArtistDto.builder()
                    .id(i)
                    .name("artist name")
                    .gender("M")
                    .build()
            );
        }

        return ResponseEntity.ok(artists);
    }

    @PostMapping
    public ResponseEntity<ArtistDto> addArtist(@RequestBody ArtistDto artistDto) {
        return ResponseEntity.ok(artistDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable long id) {
        return ResponseEntity.ok().build();
    }
}
