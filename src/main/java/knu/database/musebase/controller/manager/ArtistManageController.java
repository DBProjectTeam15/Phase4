package knu.database.musebase.controller.manager;

import knu.database.musebase.dao.ArtistDAO;
import knu.database.musebase.data.Artist;
import knu.database.musebase.console.ManagerPageKey;
import knu.database.musebase.auth.manager.ManagerAuthService;
import knu.database.musebase.console.PageController;
import knu.database.musebase.auth.manager.ManagerSessionWrapper;
import knu.database.musebase.exception.InvalidLoginStateException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArtistManageController implements PageController<ManagerPageKey> {
    private final ManagerAuthService authService;
    private final ManagerSessionWrapper managerSessionWrapper;
    private final ArtistDAO artistDAO;

    @Override
    public void displayScreen() throws InvalidLoginStateException {

        if (!managerSessionWrapper.validateLogin()) {
            throw new InvalidLoginStateException();
        }

        System.out.println("\n--- 아티스트 관리 ---");


        System.out.println("관리자 ID: " + managerSessionWrapper.getManagerSession().getLoggedInNickname());

        for (Artist artist : artistDAO.findAll()) {
            System.out.println(artist.getId() + ":" + artist.getName() + ":" + artist.getGender());
        }

        System.out.println("\n1. 아티스트 정보 확인 [아티스트 ID]");
        System.out.println("2. 아티스트 추가 [이름] [성별]");
        System.out.println("3. 아티스트 삭제 [아티스트 ID]");
        System.out.println("0. 돌아가기");
    }

    @Override
    public ManagerPageKey invoke(String[] commands) {
        return switch (commands[0]) {
            case "0" -> ManagerPageKey.MANAGER_MAIN;
            case "1" -> {
                System.out.println("아티스트 정보 출력\n");
                long id = Long.parseLong(commands[1]);
                Artist artist = artistDAO.findById(id).orElse(null);

                if (artist == null) {
                    System.out.println("해당 ID의 아티스트가 없습니다.");
                }
                else {
                    System.out.println(artist.getId() + ":" + artist.getName() + ":" + artist.getGender());
                }

                yield ManagerPageKey.ARTIST_MANAGEMENT;
            }
            case "2" -> {

                System.out.println("아티스트 추가\n");

                Artist artist = new Artist(commands[1], commands[2]);
                artist = artistDAO.save(artist);

                if (artist == null) {
                    System.out.println("아티스트 정보 저장에 실패했습니다.");
                }
                else {
                    System.out.println("아티스트 정보 저장에 성공했습니다.");
                }

                yield ManagerPageKey.ARTIST_MANAGEMENT;
            }
            case "3" -> {

                System.out.println("아티스트 삭제\n");

                long id = Long.parseLong(commands[1]);

                long result = artistDAO.deleteById(id);

                if (result == 1) System.out.println("아티스트 삭제에 성공했습니다.");
                else System.out.println("아티스트 삭제에 실패했거나, 해당 아티스트가 없습니다.");

                yield ManagerPageKey.ARTIST_MANAGEMENT;
            }

            default -> ManagerPageKey.ARTIST_MANAGEMENT;
        };
    }
}
