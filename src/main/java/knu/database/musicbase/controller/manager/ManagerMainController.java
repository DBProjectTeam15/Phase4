package knu.database.musicbase.controller.manager;

import knu.database.musicbase.console.PageController;
import knu.database.musicbase.console.ManagerPageKey;
import knu.database.musicbase.auth.manager.ManagerAuthService;
import knu.database.musicbase.auth.manager.ManagerSessionWrapper;
import knu.database.musicbase.exception.InvalidLoginStateException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ManagerMainController implements PageController<ManagerPageKey> {
    private final ManagerSessionWrapper sessionWrapper;
    private final ManagerAuthService managerAuthService;

    @Override
    public void displayScreen() throws InvalidLoginStateException {
        System.out.println("\n--- 관리자 페이지 ---");
        if (sessionWrapper.validateLogin()) {
            System.out.println("관리자 ID: " + sessionWrapper.getManagerSession().getLoggedInNickname());
            System.out.println("1. 로그아웃");
        } else {
            System.out.println("1. 관리자 로그인 [아이디] [비밀번호]");
        }
        System.out.println("2. 악곡 요청 관리");
        System.out.println("3. 제공원 관리");
        System.out.println("4. 아티스트 관리");
        System.out.println("5. 음원 관리");
        System.out.println("---------------------");
        System.out.println("0. 종료");
    }

    @Override
    public ManagerPageKey invoke(String[] commands) {
        return switch (commands[0]) { // 로그인 / 로그아웃
            case "1" -> {
                System.out.println("로그인 시도를 했습니다.");
                if (!sessionWrapper.validateLogin()) {
                    if (commands.length >= 3) {
                        sessionWrapper.updateManagerSession(
                                managerAuthService.login(commands[1], commands[2])
                        );
                    }
                }
                else {
                    sessionWrapper.updateManagerSession(null);
                }
                yield ManagerPageKey.MANAGER_MAIN;
            }
            case "2" -> ManagerPageKey.REQUEST_MANAGEMENT;
            case "3" -> ManagerPageKey.PROVIDER_MANAGEMENT;
            case "4" -> ManagerPageKey.ARTIST_MANAGEMENT;
            case "5" -> ManagerPageKey.SONG_MANAGEMENT;
            case "0" -> ManagerPageKey.EXIT;
            default -> ManagerPageKey.MANAGER_MAIN;
        };

    }

}
