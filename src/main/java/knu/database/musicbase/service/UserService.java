package knu.database.musicbase.service;

import jakarta.servlet.http.HttpSession;
import knu.database.musicbase.dto.UserDto;
import knu.database.musicbase.enums.AuthType;
import knu.database.musicbase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    public UserDto getUserById(long id) {
        return userRepository.findUserInfoById(id);
    }

    public UserDto getManagerById(String id) {
        return userRepository.findUserInfoByIdFromManager(id);
    }

    public boolean deleteUser(long userId) {
        try {
            userRepository.deleteAccount(userId);
            return true;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    public UserDto updateUsername(String username, HttpSession session) {
        UserDto userDto = authService.getLoggedInUser(session);
        if (userDto == null) throw new IllegalStateException("User not logged in");

        try {
            userRepository.updateMyInfo(userDto.getId(), username);
        }
        catch (IllegalArgumentException e) {
            return null;
        }

        userDto.setUsername(username);

        authService.updateSession(userDto, session);

        return userDto;
    }
}
