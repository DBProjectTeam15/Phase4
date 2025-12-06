package knu.database.musicbase.controller;

import jakarta.servlet.http.HttpSession;
import knu.database.musicbase.dto.PlaylistDto;
import knu.database.musicbase.dto.SongDto;
import knu.database.musicbase.dto.UserDto;
import knu.database.musicbase.dao.PlaylistDao;
import knu.database.musicbase.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/playlists")
@RestController
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistDao playlistDao;
    private final AuthService authService;


    @GetMapping("/top10")
    public List<PlaylistDto> getTop10BySongCounts() {
        return playlistDao.getTop10BySongCounts();
    }

    @GetMapping("/{id}")
    public PlaylistDto getPlaylist(@PathVariable Long id) {
        return playlistDao.getPlaylist(id);
    }

    @GetMapping("/{id}/songs")
    public List<SongDto> getPlaylistDetails(@PathVariable Long id) {
        return playlistDao.getPlaylistDetails(id);

    }

    // 음악 포함 플리 조회 (sodId 오타 수정 -> songId)
    @GetMapping("/containing-song/{songId}")
    public List<PlaylistDto> getPlaylistBySong(@PathVariable Long songId) {
        return playlistDao.getPlaylistBySong(songId);
    }

    // 내가 소유한 플리 조회 (세션은 Repository 내부에서 처리)
    @GetMapping("/my")
    public ResponseEntity<List<PlaylistDto>> getMyPlaylists(HttpSession session) {
        UserDto userDto = authService.getLoggedInUser(session);
        if (userDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(playlistDao.findPlaylistsByUserId(userDto.getId()));
    }

    // 공유된 플리 조회
    @GetMapping("/shared")
    public ResponseEntity<List<PlaylistDto>> getSharedPlaylists(HttpSession session) {
        UserDto userDto = authService.getLoggedInUser(session);
        if (userDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(playlistDao.findSharedPlaylists(userDto.getId()));
    }

    // 편집 가능한 플리 조회 (세션은 Repository 내부에서 처리)
    @GetMapping("/editable")
    public ResponseEntity<List<PlaylistDto>> getEditablePlaylists(HttpSession session) {
        UserDto userDto = authService.getLoggedInUser(session);
        if (userDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(playlistDao.findEditablePlaylists(userDto.getId()));
    }

    // 플리 검색
    @GetMapping("/search")
    public List<PlaylistDto> searchPlaylists(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer songCount,         // 최소 수록곡 수
            @RequestParam(required = false) Integer songCountMax,      // 최대 수록곡 수
            @RequestParam(required = false) Integer commentCount,      // 최소 댓글 수
            @RequestParam(required = false) Integer commentCountMax,   // 최대 댓글 수
            @RequestParam(required = false) String owner,              // 소유자 닉네임
            @RequestParam(required = false) Integer totalLength,       // 최소 총 재생 시간(초)
            @RequestParam(required = false, defaultValue = "title") String sortBy, // 정렬 기준
            @RequestParam(required = false, defaultValue = "asc") String sortOrder // 정렬 순서
    ) {
        return playlistDao.searchPlaylists(title, songCount, songCountMax, commentCount, commentCountMax, owner, totalLength, sortBy, sortOrder);
    }

    // 플레이리스트에 곡 추가 (동시성 제어)
    @PostMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<String> addSongToPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId
    ) {
        playlistDao.addSongToPlaylist(playlistId, songId);
        return ResponseEntity.ok("곡이 플레이리스트에 추가되었습니다.");
    }

    // 플레이리스트에서 곡 삭제
    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<String> removeSongFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId
    ) {
        playlistDao.removeSongFromPlaylist(playlistId, songId);
        return ResponseEntity.ok("곡이 플레이리스트에서 삭제되었습니다.");
    }
}