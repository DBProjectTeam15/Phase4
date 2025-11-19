package knu.database.musicbase.controller.search;

import knu.database.musicbase.console.PageController;
import knu.database.musicbase.console.PageKey;
import knu.database.musicbase.exception.InvalidLoginStateException;

public class SearchController implements PageController<PageKey> {
    @Override
    public void displayScreen() throws InvalidLoginStateException {

        System.out.println("-- 검색 페이지 --\n");

        System.out.println("1. 플레이리스트 검색");
        System.out.println("2. 악곡 검색");
        System.out.println("3. 아티스트 검색");
        System.out.println("0. 돌아가기");
    }

    @Override
    public PageKey invoke(String[] commands) {
        return switch (commands[0]) {
            case "1" -> PageKey.PLAYLIST_SEARCH;
            case "2" -> PageKey.SONG_SEARCH;
            case "3" -> PageKey.ARTIST_SEARCH;
            case "0" -> PageKey.MAIN;
            default -> PageKey.SEARCH;
        };
    }
}
