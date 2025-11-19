package knu.database.musicbase.controller.search;

import knu.database.musicbase.console.PageController;
import knu.database.musicbase.console.PageKey;
import knu.database.musicbase.data.Song;
import knu.database.musicbase.service.SongService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SongResultController implements PageController<PageKey> {

    private final SongService songService;

    @Override
    public void displayScreen() {
        String title = songService.getSongListName();
        List<Song> results = songService.getSongs();

        System.out.println("--- " + title + " ---");

        if (results == null || results.isEmpty()) {
            System.out.println("결과가 없습니다.");
        } else {
            System.out.printf("총 %d개의 악곡을 찾았습니다.\n", results.size());
            System.out.println("-------------------------------------------------");
            for (int i = 0; i < results.size(); i++) {
                Song s = results.get(i);
                // [수정됨] 출력 형식 변경
                System.out.printf("%d. %s (ID: %d)\n",
                        i + 1,
                        s.getTitle(),
                        s.getId());
                System.out.printf("   (재생시간: %d초, 발매일: %s, 제공원: %s)\n",
                        s.getLength(),
                        s.getCreateAt(),
                        s.getProviderName() != null ? s.getProviderName() : "N/A");
                System.out.printf("   (링크: %s)\n", s.getPlayLink()); // [추가됨]
            }
        }
        System.out.println("-------------------------------------------------");
        System.out.println("0. 검색 화면으로 돌아가기");
    }

    @Override
    public PageKey invoke(String[] commands) {
        if ("0".equals(commands[0])) {
            return PageKey.SONG_SEARCH; // 악곡 검색 화면으로
        } else {
            System.out.println("오류: 유효한 명령(0)을 입력하세요.");
            return PageKey.SONG_RESULT; // 현재 페이지 유지
        }
    }
}
