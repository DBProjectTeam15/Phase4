package knu.database.musicbase.service;

import knu.database.musicbase.dao.ArtistDAO;
import knu.database.musicbase.data.Artist;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public class ArtistService {

    private final ArtistDAO artistDAO;

    // --- 검색 결과 ---
    private List<Artist> artists = List.of();
    private String artistListName = "아티스트 검색 결과";

    // --- ArtistSearchController를 위한 필터 상태 ---
    private String nameKeyword = null;
    private boolean nameExact = false;
    private String gender = null; // "F", "M", "None" 또는 null
    private List<String> roles = new ArrayList<>();

    /**
     * 현재 필터를 기반으로 DAO를 호출하여 검색 결과 수를 계산합니다.
     */
    public int getSearchCount() {
        return artistDAO.countArtists(nameKeyword, nameExact, gender, roles);
    }

    /**
     * 현재 필터를 기반으로 DAO를 호출하여 실제 데이터를 검색하고,
     * 'artists' 리스트에 저장합니다.
     */
    public void executeSearch() {
        this.artistListName = "아티스트 검색 결과";
        this.artists = artistDAO.searchArtists(nameKeyword, nameExact, gender, roles);
    }

    // --- 컨트롤러가 필터 값을 설정하기 위한 헬퍼 메소드 ---

    public void setNameFilter(String keyword, boolean exact) {
        this.nameKeyword = keyword;
        this.nameExact = exact;
    }

    public void setGenderFilter(String gender) {
        // "NONE"을 "None"으로, "NULL"을 null로 변환
        if (gender == null || gender.equalsIgnoreCase("NULL")) {
            this.gender = null;
        } else if (gender.equalsIgnoreCase("NONE")) {
            this.gender = "None";
        } else {
            this.gender = gender.toUpperCase(); // "F" or "M"
        }
    }

    public void setRolesFilter(List<String> roles) {
        this.roles.clear();
        if (roles != null) {
            this.roles.addAll(roles);
        }
    }

    public void clearSearchFilters() {
        this.nameKeyword = null;
        this.nameExact = false;
        this.gender = null;
        this.roles.clear();
    }
}