package knu.database.musicbase.service;

import jakarta.servlet.http.HttpSession;
import knu.database.musicbase.dto.ManagerLoginDto;
import knu.database.musicbase.dto.UserDto;
import knu.database.musicbase.dto.UserLoginDto;
import knu.database.musicbase.enums.AuthType;
import knu.database.musicbase.dao.AuthDao;
import knu.database.musicbase.dao.UserDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String AUTH_TYPE_ATTR = "authType";
    private static final String SESSION_ATTR = "session";

    private final AuthDao authDao;
    private final UserDao userDao;

    public AuthType getAuthType(HttpSession session) {
        return (AuthType) session.getAttribute("authType");
    }

    public UserDto getLoggedInUser(HttpSession session) {
        return (UserDto) session.getAttribute(SESSION_ATTR);
    }

    public UserDto updateSession(UserDto userDto, HttpSession session) {
        session.setAttribute(SESSION_ATTR, userDto);
        return userDto;
    }

    public UserDto login(UserLoginDto userLoginDto, HttpSession session) {
        try {
            var userId = authDao.findByUserId(userLoginDto);
            var userDto = userDao.findUserInfoById(userId);

            session.setAttribute(AUTH_TYPE_ATTR, AuthType.USER);
            session.setAttribute(SESSION_ATTR, userDto);

            return userDto;
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public UserDto loginManager(ManagerLoginDto managerLoginDto, HttpSession session) {
        try {
            var id = authDao.findByManagerId(managerLoginDto);
            var userDto = userDao.findUserInfoByIdFromManager(id);

            session.setAttribute(AUTH_TYPE_ATTR, AuthType.MANAGER);
            session.setAttribute(SESSION_ATTR, userDto);

            return userDto;
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void logout(HttpSession session) {
        session.removeAttribute(AUTH_TYPE_ATTR);
        session.removeAttribute(SESSION_ATTR);
    }
}
