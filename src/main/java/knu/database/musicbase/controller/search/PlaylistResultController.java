package knu.database.musicbase.controller.search;

import knu.database.musicbase.console.PageController;
import knu.database.musicbase.console.PageKey;
import knu.database.musicbase.data.Playlist;
import knu.database.musicbase.service.PlaylistService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PlaylistResultController implements PageController<PageKey> {

    // Service에서 현재 상태(검색 결과)를 받아옵니다.
    private final PlaylistService playlistService;

    /**
     * playlistService로부터 현재 저장된 플레이리스트 목록과 제목을 가져와 출력합니다.
     */
    @Override
    public void displayScreen() {
        // 1. 서비스에서 리스트 제목과 데이터 가져오기
        String title = playlistService.getPlaylistName();
        List<Playlist> results = playlistService.getPlaylists();

        System.out.println("--- " + title + " ---");

        // 2. 검색 결과 출력
        if (results == null || results.isEmpty()) {
            System.out.println("검색 결과가 없습니다.");
        } else {
            System.out.printf("총 %d개의 플레이리스트를 찾았습니다.\n", results.size());
            System.out.println("-------------------------------------------------");
            // 1-based index로 목록 출력
            for (int i = 0; i < results.size(); i++) {
                Playlist p = results.get(i);
                // Playlist DTO에 getTitle()과 getId()가 있다고 가정합니다.
                // (필요시 User 닉네임 등 추가 정보 표시)
                System.out.printf("%d. %s (ID: %d)\n", i + 1, p.getTitle(), p.getId());
            }
        }
        System.out.println("-------------------------------------------------");

        System.out.println("0. 검색 화면으로 돌아가기");
    }

    /**
     * 사용자 입력을 처리합니다.
     * '0'은 검색 화면으로, '1' 이상의 숫자는 상세 페이지로 이동합니다.
     */
    @Override
    public PageKey invoke(String[] commands) {
        String cmd = commands[0];

        if ("0".equals(cmd)) {
            // 0. 검색 화면으로 돌아가기
            return PageKey.PLAYLIST_SEARCH; // (PlaylistSearchController의 PageKey로 가정)
        }
        return PageKey.PLAYLIST_SEARCH_RESULT;
    }
}
