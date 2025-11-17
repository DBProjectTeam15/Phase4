package knu.database.musebase.service.auth;

import knu.database.musebase.auth.manager.ManagerSession;
import knu.database.musebase.crypto.PasswordEncryptor;
import knu.database.musebase.dao.manager.ManagerDAO;
import knu.database.musebase.data.Manager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ManagerAuthService {
    private final PasswordEncryptor passwordEncryptor;

    private final ManagerDAO managerDAO;

    /**
     * DB 로부터 아이디 비밀번호를 확인하여 로그인 수행
     */
    public ManagerSession login(String username, String password) {
        String passwordHash = passwordEncryptor.getPasswordHash(password);

        // DB 로부터 username 으로부터 가져와 비교
        Manager manager = managerDAO.findById(username).orElse(null);

        if (manager != null && passwordHash.equals(manager.getManagerPassword())) {
            // 성공 시 ManagerSession 만들어주며 return
            return new ManagerSession(manager.getManagerUsername(), manager.getManagerName());
        }

        return null;
    }
}