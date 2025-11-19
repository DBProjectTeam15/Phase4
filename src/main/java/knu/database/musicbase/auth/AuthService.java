package knu.database.musicbase.auth;

import knu.database.musicbase.crypto.PasswordEncryptor;
import knu.database.musicbase.dao.UserDAO;
import knu.database.musicbase.data.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncryptor passwordEncryptor;
    private final UserDAO userDAO;

    /**
     * DB 로부터 아이디 비밀번호를 확인하여 로그인 수행
     */
    public Session login(String email, String password) {
        String passwordHash = passwordEncryptor.getPasswordHash(password);

        // DB 로부터 username 으로부터 가져와 비교
        User user = userDAO.findByEmail(email).orElse(null);

        if (user != null && passwordHash.equals(user.getPassword())) {
            // 성공 시 ManagerSession 만들어주며 return
            return new Session(user.getUserId(), user.getNickname());
        }

        return null;
    }
}