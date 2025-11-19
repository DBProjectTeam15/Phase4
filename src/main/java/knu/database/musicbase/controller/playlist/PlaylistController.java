package knu.database.musicbase.controller.playlist;

import knu.database.musicbase.console.PageController;
import knu.database.musicbase.console.PageKey;
import knu.database.musicbase.data.Playlist;
import knu.database.musicbase.exception.InvalidLoginStateException;
import knu.database.musicbase.service.PlaylistService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class PlaylistController implements PageController<PageKey> {

    protected final PlaylistService playlistService;


    @Override
    public void displayScreen() throws InvalidLoginStateException {
        // playlistService 에서 받아와야할 플레이리스트들을 출력합니다.
        System.out.println("-- " + playlistService.getPlaylistName() + " --");

        List<Playlist> playlists = playlistService.getPlaylists();

        if (playlists.isEmpty()) {
            System.out.println("  * 해당 플레이리스트가 없습니다.");
        } else {

            for (Playlist pl : playlists) {
                System.out.println(pl.getId() + " : " + pl.getTitle() + " : " + pl.getIsCollaborative());
            }
        }

        System.out.println("\n0. 돌아가기");
    }

    @Override
    public abstract PageKey invoke(String[] commands);
}
