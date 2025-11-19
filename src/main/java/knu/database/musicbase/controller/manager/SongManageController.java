package knu.database.musicbase.controller.manager;

import knu.database.musicbase.console.PageController;
import knu.database.musicbase.dao.SongDAO;
import knu.database.musicbase.data.Song;
import knu.database.musicbase.console.ManagerPageKey;
import knu.database.musicbase.auth.manager.ManagerSessionWrapper;
import knu.database.musicbase.exception.InvalidLoginStateException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SongManageController implements PageController<ManagerPageKey> {
    private final ManagerSessionWrapper sessionWrapper;
    private final SongDAO songDAO;

    @Override
    public void displayScreen() throws InvalidLoginStateException {
        if (!sessionWrapper.validateLogin()) {
            throw new InvalidLoginStateException();
        }
        System.out.println("\n--- 음원 관리 ---");
        for (Song song : songDAO.findAll()) {
            System.out.println(song.getId() + ":" +  song.getTitle() + ":" + song.getPlayLink());
        }
        System.out.println("관리자 ID: " + sessionWrapper.getManagerSession().getLoggedInNickname());
        System.out.println("0. 돌아가기");
        System.out.println("\n*** 인자 정보는 아티스트와 동일 ***");
    }

    @Override
    public ManagerPageKey invoke(String[] commands) {
        return switch(commands[0]) {
            case "0" -> ManagerPageKey.MANAGER_MAIN;
            default -> ManagerPageKey.SONG_MANAGEMENT;
        };
    }
}
