package knu.database.musicbase.controller.my;

import knu.database.musicbase.auth.AuthService;
import knu.database.musicbase.auth.SessionWrapper;
import knu.database.musicbase.console.PageKey;
import knu.database.musicbase.console.PageController;
import knu.database.musicbase.crypto.PasswordEncryptor;
import knu.database.musicbase.dao.UserDAO;
import knu.database.musicbase.data.User;
import knu.database.musicbase.exception.InvalidLoginStateException;
import knu.database.musicbase.service.CommentService;
import knu.database.musicbase.service.PlaylistService;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class MyPageController implements PageController<PageKey> {

    private final SessionWrapper sessionWrapper;
    private final AuthService authService;
    private final PlaylistService playlistService;
    private final CommentService commentService;
    private final PasswordEncryptor passwordEncryptor;
    private final UserDAO userDAO;

    @Override
    public void displayScreen() throws InvalidLoginStateException {
        System.out.println("-- 내 정보 페이지 --");
        if (!sessionWrapper.validateLogin()) {
            System.out.println("로그인되어있지 않습니다. 메인페이지에서 로그인을 해주세요.");
            throw new InvalidLoginStateException();
        }
        else {
            System.out.println("로그인 User ID: " +
                    sessionWrapper.getSession().getLoggedInId() +
                    " 닉네임 : " + sessionWrapper.getSession().getLoggedInNickname()
            );
        }
        System.out.println("\n1. 비밀번호 변경 [현재 비밀번호] [변경할 비밀번호]");
        System.out.println("2. 닉네임 변경 [변경할 닉네임]");
        System.out.println("3. 계정 삭제 [y]");
        System.out.println("4. 공유된 플레이리스트 (소유는 하지 않았지만 편집은 가능한) 보기");
        System.out.println("5. 편집 가능 플레이리스트 보기");
        System.out.println("6. 소유한 플레이리스트 보기");
        System.out.println("7. 내가 작성한 댓글 보기");
        System.out.println("0. 돌아가기");
    }

    // TODO: 시간없어서 못한 안티패턴 개선하기
    @Override
    public PageKey invoke(String[] commands) {
        return switch (commands[0]) {
            case "1" -> {
                if (!sessionWrapper.validateLogin()) {
                    yield PageKey.MY_PAGE;
                }

                User user = userDAO.findById(sessionWrapper.getSession().getLoggedInId()).orElse(null);

                if (user == null) {
                    yield PageKey.MY_PAGE;
                }

                var passwordHash = passwordEncryptor.getPasswordHash(commands[1]);
                var newPasswordHash = passwordEncryptor.getPasswordHash(commands[2]);

                if (!passwordHash.equals(user.getPassword())) {
                    System.out.println("현재 비밀번호가 다릅니다.");
                    yield PageKey.MY_PAGE;
                }

                try {
                    userDAO.update(new User(user.getUserId(), user.getNickname(), newPasswordHash, user.getEmail()));
                    System.out.println("비밀번호가 변경되었습니다.");
                }
                catch (SQLException ex) {
                    System.out.println("비밀번호 업데이트에 실패했습니다.");
                }

                yield PageKey.MY_PAGE;
            }
            case "2" -> {
                if (!sessionWrapper.validateLogin()) {
                    yield PageKey.MY_PAGE;
                }
                User user = userDAO.findById(sessionWrapper.getSession().getLoggedInId()).orElse(null);

                if (user == null) {
                    yield PageKey.MY_PAGE;
                }

                try {
                    userDAO.update(new User(user.getUserId(), commands[1], user.getPassword(), user.getEmail()));
                    sessionWrapper.updateSession(authService.login(user.getEmail(), user.getPassword()));
                    System.out.println("닉네임이 변경되었습니다. : " + commands[1]);
                }
                catch (SQLException ex) {
                    System.out.println("닉네임 업데이트에 실패했습니다.");
                }

                yield PageKey.MY_PAGE;
            }
            case "3" -> {
                if (!sessionWrapper.validateLogin() || !commands[1].equals("y")) {
                    yield PageKey.MY_PAGE;
                }
                userDAO.deleteByID(sessionWrapper.getSession().getLoggedInId());
                sessionWrapper.updateSession(null);
                yield PageKey.MAIN;
            }
            case "4" -> {
                if (!sessionWrapper.validateLogin()) yield PageKey.MY_PAGE;
                playlistService.updateSharedPlaylists(sessionWrapper.getSession().getLoggedInId());
                yield PageKey.MY_PAGE_PLAYLIST;
            }
            case "5" -> {
                if (!sessionWrapper.validateLogin()) yield PageKey.MY_PAGE;
                playlistService.updateEditablePlaylist(sessionWrapper.getSession().getLoggedInId());
                yield PageKey.MY_PAGE_PLAYLIST;
            }
            case "6" -> {
                if (!sessionWrapper.validateLogin()) yield PageKey.MY_PAGE;
                playlistService.updateMyPlaylist(sessionWrapper.getSession().getLoggedInId());
                yield PageKey.MY_PAGE_PLAYLIST;
            }
            case "7" -> {
                if (!sessionWrapper.validateLogin()) yield PageKey.MY_PAGE;
                commentService.findByUserId(sessionWrapper.getSession().getLoggedInId());
                yield PageKey.MY_PAGE_COMMENT;
            }
            case "0" -> PageKey.MAIN;
            default -> PageKey.MY_PAGE;
        };
    }
}
