package knu.database.musicbase.controller.playlist;

import knu.database.musicbase.console.PageKey;
import knu.database.musicbase.data.Playlist;
import knu.database.musicbase.exception.InvalidLoginStateException;
import knu.database.musicbase.service.PlaylistDetailService;
import knu.database.musicbase.service.PlaylistService;

import java.util.List;


/**
 * 이전 상태값을 저장해야하는데, 시간이 없어서... 추가적으로 구현했습니다.
 * <br/>
 * TODO: 이전 상태로 돌아가는 명령을 할 시, 전역 상태에서 가져오기
 */
public class MyPagePlaylistController extends PlaylistController {
    private final PlaylistDetailService playlistDetailService;

    public MyPagePlaylistController(PlaylistService playlistService, PlaylistDetailService playlistDetailService) {
        super(playlistService);
        this.playlistDetailService = playlistDetailService;
    }

    @Override
    public void displayScreen() throws InvalidLoginStateException {
        // playlistService 에서 받아와야할 플레이리스트들을 출력합니다.
        System.out.println("-- " + playlistService.getPlaylistName() + " --");
        System.out.println("이곳에서 각각의 플레이리스트 인덱스 값을 명령어로 치면 자세한 정보를 보실 수 있습니다.");
        System.out.println("* 1. ... -> 1 입력시 상세 페이지 확인 가능");

        List<Playlist> playlists = playlistService.getPlaylists();

        if (playlists.isEmpty()) {
            System.out.println("  * 해당 플레이리스트가 없습니다.");
        } else {

            for (int i=0; i<playlists.size(); ++i) {
                Playlist pl = playlists.get(i);
                System.out.println((i + 1) + ". " + pl.getId() + " : " + pl.getTitle() + " : " + pl.getIsCollaborative());
            }
        }

        System.out.println("\n0. 돌아가기");
    }


    @Override
    public PageKey invoke(String[] commands){
        if  (commands[0].equals("0")) return PageKey.MY_PAGE;

        try {
            long id = Long.parseLong(commands[0]);
            if (id > 0 && id <= playlistService.getPlaylists().size()) {
                Playlist playlist = playlistService.getPlaylists().get((int) id-1);
                long playlistId = playlist.getId();
                String playlistName = playlist.getTitle();
                playlistDetailService.updatePageStatus(PageKey.MY_PAGE_PLAYLIST, playlistName);
                playlistDetailService.getPlaylistDetails(playlistId);

                return PageKey.PLAYLIST_DETAIL;
            }
        }
        catch (NumberFormatException e) {
            return PageKey.MY_PAGE;
        }

        return PageKey.MY_PAGE_PLAYLIST;
    }
}
