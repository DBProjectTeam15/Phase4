package knu.database.musicbase.auth;

import lombok.Getter;

public class Session {
    @Getter
    private final String loggedInNickname;
    @Getter
    private final long loggedInId;

    // package private : 동일 패키지 이외에서는 접근하지 못함.
    Session(long loggedInId, String loggedInNickname) {
        this.loggedInId = loggedInId;
        this.loggedInNickname = loggedInNickname;
    }
}
