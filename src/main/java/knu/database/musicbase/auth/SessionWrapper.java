package knu.database.musicbase.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Controls manager session states
 */
@NoArgsConstructor
public class SessionWrapper {
    @Getter
    private Session session = null;

    public void updateSession(Session managerSession) {
        this.session = managerSession;
    }

    public boolean validateLogin() {
        return this.session != null;
    }
}
