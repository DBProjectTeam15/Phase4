package knu.database.musicbase.controller;


import knu.database.musicbase.dto.SongDetailDto;
import knu.database.musicbase.dao.SongDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/songs")
@RestController
public class SongController {

    @Autowired
    SongDao songDao;

    @GetMapping("/search")
    public List<SongDetailDto> searchSongs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false, defaultValue = "false") boolean exactTitle,
            @RequestParam(required = false) String artistName,
            @RequestParam(required = false, defaultValue = "false") boolean exactArtist,
            @RequestParam(required = false) Integer minTime,
            @RequestParam(required = false) Integer maxTime,
            @RequestParam(required = false) String songName,
            @RequestParam(required = false, defaultValue = "false") boolean exactsong,
            @RequestParam(required = false) String minDate,
            @RequestParam(required = false) String maxDate,
            @RequestParam(required = false, defaultValue = "title") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder
    ) {
        // 실제 DB 조회 로직 호출
        return songDao.searchSongs(
                title, exactTitle,
                artistName, exactArtist,
                minTime, maxTime,
                songName, exactsong,
                minDate, maxDate,
                sortBy, sortOrder
        );
    }


    // 전체 음원 조회?
    @GetMapping("")
    public List<SongDetailDto> getAllSongs(){
        return songDao.getAllSongs();
    }

    // 제공원 정보 조회
    @GetMapping("/{id}")
    public SongDetailDto getSongDetails(@PathVariable Long id) {
        return songDao.getSongDetails(id);
    }

    // 제공원 추가
    @PostMapping("")
    public SongDetailDto addSong(@RequestBody SongDetailDto songDetailDto) {
        return songDao.addSong(songDetailDto);
    }

    // 제공원 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<SongDetailDto> deleteSong(@PathVariable Long id) {
        SongDetailDto deletedSong = songDao.deleteSong(id);

        if (deletedSong != null) {
            // 성공 시: 200 OK
            return ResponseEntity.ok(deletedSong);
        } else {
            // 실패 시: 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }
    
}
