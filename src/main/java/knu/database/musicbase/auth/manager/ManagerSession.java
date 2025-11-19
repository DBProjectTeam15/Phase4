package knu.database.musicbase.auth.manager;

import lombok.Getter;

public class ManagerSession {
    @Getter
    private final String loggedInNickname;
    @Getter
    private final String loggedInId;

    // package private : 동일 패키지 이외에서는 접근하지 못함.
    ManagerSession(String loggedInId, String loggedInNickname) {
        this.loggedInId = loggedInId;
        this.loggedInNickname = loggedInNickname;
    }
}
