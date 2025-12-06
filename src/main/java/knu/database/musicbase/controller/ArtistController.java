package knu.database.musicbase.controller;


import jakarta.servlet.http.HttpSession;
import knu.database.musicbase.dto.ArtistDto;
import knu.database.musicbase.enums.AuthType;
import knu.database.musicbase.repository.ArtistRepository;

import knu.database.musicbase.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/artists")
@RestController
public class ArtistController {

    @Autowired
    private ArtistRepository artistRepository;
    @Autowired
    private AuthService authService;


    // 아티스트 검색
    @GetMapping("/search")
    public List<ArtistDto> searchArtists(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String role,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder
    ) {
        // Repository 호출
        return artistRepository.searchArtists(name, gender, role, sortBy, sortOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistDto> getArtistDetails(@PathVariable long id) {
        return ResponseEntity.ok(artistRepository.findById(id));
    }

    // 아티스트 생성
    @PostMapping
    public ResponseEntity<ArtistDto> addArtist(@RequestBody ArtistDto artistDto, HttpSession session) {
        var authType = authService.getAuthType(session);
        if (authType == AuthType.MANAGER) {
            return ResponseEntity.ok(artistRepository.save(artistDto));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    
    // 아티스트 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ArtistDto> deleteArtist(@PathVariable Long id, HttpSession session) {
        var authType = authService.getAuthType(session);
        if (authType == AuthType.MANAGER) {
            ArtistDto deletedArtist = artistRepository.delete(id);
            return ResponseEntity.ok(deletedArtist);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

}
