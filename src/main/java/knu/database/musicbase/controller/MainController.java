package knu.database.musicbase.controller;

import knu.database.musicbase.auth.SessionWrapper;
import knu.database.musicbase.console.PageController;
import knu.database.musicbase.console.PageKey;
import knu.database.musicbase.exception.InvalidLoginStateException;
import knu.database.musicbase.service.PlaylistService;
import knu.database.musicbase.auth.AuthService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MainController implements PageController<PageKey> {

    private final SessionWrapper sessionWrapper;
    private final AuthService authService;
    private final PlaylistService playlistService;

    @Override
    public void displayScreen() throws InvalidLoginStateException {
        System.out.println("-- Musebase --");

        if (sessionWrapper.validateLogin()) {
            System.out.println("1. 로그아웃");
        }
        else {
            System.out.println("1. 로그인 [아이디] [비밀번호]");
        }
        System.out.println("2. 검색하기");
        System.out.println("3. 내 정보 보기 (로그인 중에만)");
        System.out.println("4. 플레이리스트 찾아보기");
        System.out.println("0. 종료");
    }

    @Override
    public PageKey invoke(String[] commands) {
        return switch (commands[0]) {
            case "1" -> {
                // 로그인
                if (sessionWrapper.validateLogin()) {
                    sessionWrapper.updateSession(null);
                }
                else sessionWrapper.updateSession(authService.login(commands[1], commands[2]));

                yield PageKey.MAIN;
            }
            case "2" -> PageKey.SEARCH;
            case "3" -> PageKey.MY_PAGE;
            case "4" -> {
                playlistService.updateForMainPage();
                yield PageKey.PLAYLIST_PAGE;
            }
            case "0" -> PageKey.EXIT;
            default -> PageKey.MAIN;
        };
    }
}
