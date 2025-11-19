package knu.database.musicbase.service;

import knu.database.musicbase.console.PageKey;
import knu.database.musicbase.dao.PlaylistDAO;
import knu.database.musicbase.data.SongDetail;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PlaylistDetailService {
    @Getter
    private PageKey exitPage;

    @Getter
    private String playlistName;

    @Getter
    private Long currentPlaylistId;

    @Getter
    private List<SongDetail> currentPlaylist;

    private final PlaylistDAO playlistDAO;

    public void updatePageStatus(PageKey exitPage, String playlistName) {
        this.exitPage = exitPage;
        this.playlistName = playlistName;
    }

    public List<SongDetail> getPlaylistDetails(long playlistId) {
        currentPlaylistId = playlistId;
        return currentPlaylist = playlistDAO.findPlaylistDetailsByPlaylistId(playlistId);
    }
}
