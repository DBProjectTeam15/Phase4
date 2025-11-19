package knu.database.musicbase.controller.manager;

import knu.database.musicbase.console.PageController;
import knu.database.musicbase.dao.ProviderDAO;
import knu.database.musicbase.data.Provider;
import knu.database.musicbase.auth.manager.ManagerSessionWrapper;
import knu.database.musicbase.console.ManagerPageKey;
import knu.database.musicbase.exception.InvalidLoginStateException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProviderManageController implements PageController<ManagerPageKey> {
    private final ManagerSessionWrapper sessionWrapper;
    private final ProviderDAO providerDAO;

    @Override
    public void displayScreen() throws InvalidLoginStateException {
        if (!sessionWrapper.validateLogin()) {
            throw new InvalidLoginStateException();
        }
        System.out.println("\n--- 제공원 관리 ---");
        for (Provider provider : providerDAO.findAll()) {
            System.out.println(provider.getId() + ":" + provider.getName() + ":" + provider.getLink());
        }

        System.out.println("관리자 ID: " + sessionWrapper.getManagerSession().getLoggedInNickname());
        System.out.println("1. 제공원 추가 [제공원 이름] [제공원 링크]");
        System.out.println("2. 제공원 삭제 [제공원 번호]");
        System.out.println("0. 돌아가기");
    }

    @Override
    public ManagerPageKey invoke(String[] commands) {
        return switch (commands[0]) {
            case "1" -> {
                var provider = new Provider(commands[1], commands[2]);
                if (providerDAO.save(provider) == null) {
                    System.out.println("제공자를 추가하는데 실패했습니다.");
                }
                else {
                    System.out.println("성공적으로 제공자를 추가했습니다.");
                }
                yield ManagerPageKey.PROVIDER_MANAGEMENT;
            }
            case "2" -> {
                long id = Long.parseLong(commands[1]);
                long result = providerDAO.deleteById(id);
                if (result == 1) {
                    System.out.println("성공적으로 삭제했습니다.");
                }
                else {
                    System.out.println("삭제하는데 실패했습니다.");
                }
                yield ManagerPageKey.PROVIDER_MANAGEMENT;
            }
            case "0" -> ManagerPageKey.MANAGER_MAIN;
            default -> ManagerPageKey.PROVIDER_MANAGEMENT;
        };
    }
}
