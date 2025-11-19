package knu.database.musicbase.controller.search;

import knu.database.musicbase.console.PageController;
import knu.database.musicbase.console.PageKey;
import knu.database.musicbase.data.Artist;
import knu.database.musicbase.service.ArtistService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ArtistResultController implements PageController<PageKey> {

    private final ArtistService artistService;

    @Override
    public void displayScreen() {
        String title = artistService.getArtistListName();
        List<Artist> results = artistService.getArtists();

        System.out.println("--- " + title + " ---");

        if (results == null || results.isEmpty()) {
            System.out.println("결과가 없습니다.");
        } else {
            System.out.printf("총 %d명의 아티스트를 찾았습니다.\n", results.size());
            System.out.println("-------------------------------------------------");
            for (int i = 0; i < results.size(); i++) {
                Artist a = results.get(i);
                String genderStr = (a.getGender() == null) ? "N/A" : a.getGender();
                System.out.printf("%d. %s (성별: %s, ID: %d)\n", i + 1, a.getName(), genderStr, a.getId());
            }
        }
        System.out.println("-------------------------------------------------");
        System.out.println("0. 검색 화면으로 돌아가기");
    }

    @Override
    public PageKey invoke(String[] commands) {
        if ("0".equals(commands[0])) {
            return PageKey.ARTIST_SEARCH; // 아티스트 검색 화면으로
        }
        return PageKey.ARTIST_RESULT;
    }
}