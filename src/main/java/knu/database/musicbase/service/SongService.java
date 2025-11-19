package knu.database.musicbase.service;

import knu.database.musicbase.dao.SongDAO;
import knu.database.musicbase.data.Song;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
@Setter
public class SongService {

    private final SongDAO songDAO;

    // 정렬 기준 매핑 (SQL Injection 방지용)
    // Controller가 이 Map을 참조하여 안전한 SQL 컬럼명을 Service에 넘겨줍니다.
    @Getter
    private static final Map<String, String> columnMap = Map.of(
            "곡명", "s.Title",
            "아티스트", "a.Name",
            "재생시간", "s.Length",
            "발매일", "s.Create_at",
            "제공원", "p.Provider_name"
    );

    // --- 검색 결과 ---
    private List<Song> songs = List.of();
    private String songListName = "악곡 검색 결과";

    // --- SongSearchController를 위한 필터 상태 ---
    private String titleKeyword = null;
    private boolean titleExact = false;
    private String artistKeyword = null;
    private boolean artistExact = false;
    private Integer lengthMin = null;
    private Integer lengthMax = null;
    private String providerKeyword = null;
    private boolean providerExact = false;
    private String dateMin = null;
    private String dateMax = null;
    private String orderBy = "s.Title"; // 기본 정렬 (SONGS.Title)
    private String orderDir = "ASC";    // 기본 오름차순

    public int getSearchCount() {
        return songDAO.countSongs(
                titleKeyword, titleExact,
                artistKeyword, artistExact,
                lengthMin, lengthMax,
                providerKeyword, providerExact,
                dateMin, dateMax
        );
    }

    public void executeSearch() {
        this.songListName = "악곡 검색 결과";
        this.songs = songDAO.searchSongs(
                titleKeyword, titleExact,
                artistKeyword, artistExact,
                lengthMin, lengthMax,
                providerKeyword, providerExact,
                dateMin, dateMax,
                orderBy, orderDir
        );
    }

    public void clearSearchFilters() {
        this.titleKeyword = null;
        this.titleExact = false;
        this.artistKeyword = null;
        this.artistExact = false;
        this.lengthMin = null;
        this.lengthMax = null;
        this.providerKeyword = null;
        this.providerExact = false;
        this.dateMin = null;
        this.dateMax = null;
        this.orderBy = "s.Title";
        this.orderDir = "ASC";
    }
}