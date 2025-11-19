package knu.database.musicbase.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class User {
    private long userId;
    private String nickname;
    private String password;
    private String email;
}
