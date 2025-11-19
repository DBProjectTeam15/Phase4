package knu.database.musicbase.service;

import knu.database.musicbase.dao.PlaylistDAO;
import knu.database.musicbase.data.Playlist;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter; // 추가

import java.util.List;

/**
 * 플레이리스트 관련 비즈니스 로직 및 상태(필터 포함)를 관리합니다.
 */
@RequiredArgsConstructor
@Getter // 모든 필드의 Getter 자동 생성
@Setter // 모든 필드의 Setter 자동 생성 (필터 설정을 위해)
public class PlaylistService {

    // --- 기존 상태 ---
    private List<Playlist> playlists = List.of();
    private String playlistName = "";
    private final PlaylistDAO playlistDAO;

    // --- PlaylistSearchController를 위한 필터 상태 ---
    private String titleKeyword = null;
    private boolean titleExact = false;
    private Integer songCountMin = null;
    private Integer songCountMax = null;
    private Integer commentCountMin = null;
    private Integer commentCountMax = null;
    private String ownerKeyword = null;
    private boolean ownerExact = false;
    private Integer lengthMin = null;
    private Integer lengthMax = null;

    // --- 기존 메소드 ---
    public void updateForMainPage() {
        playlistName = "음원수가 많은 최상위 10개 플레이리스트";
        playlists = playlistDAO.top10BySongCountOrderByDESCAndSet();
    }

    public void updateEditablePlaylist(long userId) {
        playlistName = "편집 가능한 플레이리스트";
        playlists = playlistDAO.findEditable(userId);
    }

    public void updateMyPlaylist(long userId) {
        playlistName = "내가 소유한 플레이리스트";
        playlists = playlistDAO.findByUserId(userId);
    }

    // --- PlaylistSearchController를 위한 신규 메소드 ---

    /**
     * Service에 저장된 현재 필터 값들을 기반으로 DAO를 호출하여 검색을 실행합니다.
     * 결과는 playlists 리스트에 덮어씌워집니다.
     */
    public void executeSearch() {
        this.playlistName = "플레이리스트 검색 결과";
        this.playlists = playlistDAO.searchPlaylists(
                titleKeyword, titleExact,
                songCountMin, songCountMax,
                commentCountMin, commentCountMax,
                ownerKeyword, ownerExact,
                lengthMin, lengthMax
        );
    }

    public void updateSharedPlaylists(long userId) {
        this.playlistName = "공유 (소유하진 않았지만, 편집권한이 있는) 플레이리스트";
        this.playlists = playlistDAO.findSharedPlaylistsByUserId(userId);
    }

    /**
     * 모든 검색 필터 값을 초기화합니다.
     */
    public void clearSearchFilters() {
        this.titleKeyword = null;
        this.titleExact = false;
        this.songCountMin = null;
        this.songCountMax = null;
        this.commentCountMin = null;
        this.commentCountMax = null;
        this.ownerKeyword = null;
        this.ownerExact = false;
        this.lengthMin = null;
        this.lengthMax = null;
    }

    // --- 컨트롤러가 필터 값을 설정하기 위한 헬퍼 메소드 ---

    public void setTitleFilter(String keyword, boolean exact) {
        this.titleKeyword = keyword;
        this.titleExact = exact;
    }

    public void setSongCountFilter(Integer min, Integer max) {
        this.songCountMin = min;
        this.songCountMax = max;
    }

    public void setCommentCountFilter(Integer min, Integer max) {
        this.commentCountMin = min;
        this.commentCountMax = max;
    }

    public void setOwnerFilter(String keyword, boolean exact) {
        this.ownerKeyword = keyword;
        this.ownerExact = exact;
    }

    public void setLengthFilter(Integer min, Integer max) {
        this.lengthMin = min;
        this.lengthMax = max;
    }
}