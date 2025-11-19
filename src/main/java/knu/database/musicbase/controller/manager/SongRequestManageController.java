package knu.database.musicbase.controller.manager;

import knu.database.musicbase.console.PageController;
import knu.database.musicbase.auth.manager.ManagerSessionWrapper;
import knu.database.musicbase.console.ManagerPageKey;
import knu.database.musicbase.dao.manager.SongRequestDAO;
import knu.database.musicbase.exception.InvalidLoginStateException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SongRequestManageController implements PageController<ManagerPageKey> {
    private final ManagerSessionWrapper sessionWrapper;
    private final SongRequestDAO songRequestDAO;

    @Override
    public void displayScreen() throws InvalidLoginStateException {
        if (!sessionWrapper.validateLogin()) {
            System.out.println("로그인 실패");
            throw new InvalidLoginStateException();

        }

        System.out.println("\n--- 악곡 요청 관리 페이지 (관리자) ---");
        System.out.println("관리자 ID: " + sessionWrapper.getManagerSession().getLoggedInNickname());
        // (데이터베이스 연동 시) 실제 데이터 조회
        songRequestDAO.findByManagerId(sessionWrapper.getManagerSession().getLoggedInId()).forEach(songRequest -> {
            var str = songRequest.getTitle() + " : " + songRequest.getArtist()
                    + " : 신청자 : " + songRequest.getUserId()
                    + " : " + songRequest.getRequestAt();
            System.out.println(str);
        });
        System.out.println("1. 요청 삭제 [신청번호]");
        // 요청 처리는 시간관계상 생략
        System.out.println("0. 돌아가기");
    }

    @Override
    public ManagerPageKey invoke(String[] commands) {
        return switch (commands[0]) {
            case "1" -> {
                long id = Long.parseLong(commands[1]);

                long removeResult = songRequestDAO.deleteById(id);

                if (removeResult > 0) {
                    System.out.println("정상적으로 요청이 정리되었습니다.");
                }
                else {
                    System.out.println("삭제된 요청이 없거나, 오류가 발생했습니다.");
                }

                yield ManagerPageKey.REQUEST_MANAGEMENT;
            }
            case "0" -> ManagerPageKey.MANAGER_MAIN;

            default -> ManagerPageKey.REQUEST_MANAGEMENT;
        };
    }

//    // 서비스 함수들을 따로 나눠주기
//    public Song acceptSongRequest(String[] commands) {
//
//    }
}
