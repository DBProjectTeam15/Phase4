package knu.database.musicbase.controller.playlist;

import knu.database.musicbase.console.PageController;
import knu.database.musicbase.console.PageKey;
import knu.database.musicbase.data.SongDetail;
import knu.database.musicbase.exception.InvalidLoginStateException;
import knu.database.musicbase.service.PlaylistDetailService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlaylistDetailController implements PageController<PageKey> {
    private final PlaylistDetailService playlistDetailService;

    @Override
    public void displayScreen() throws InvalidLoginStateException {
        System.out.println("-- 플레이리스트 보기 --");
        System.out.println("-- 플레이리스트 아이디 : "
                + playlistDetailService.getCurrentPlaylistId()
                + " --");
        System.out.println("음악 아이디 : 음악 제목 : 아티스트 제목 : 플레이 링크");
        for (SongDetail songDetail : playlistDetailService.getCurrentPlaylist()) {
            System.out.println(songDetail.getId() + ":"
                    + songDetail.getTitle() + ":"
                    + songDetail.getArtistName() + ":"
                    + songDetail.getPlayLink()
            );
        }
        System.out.println("\n0. 돌아가기");
    }

    @Override
    public PageKey invoke(String[] commands) {
        if (commands[0].equals("0")) return playlistDetailService.getExitPage();
        else return PageKey.PLAYLIST_DETAIL;
    }
}
