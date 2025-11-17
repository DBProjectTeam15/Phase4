package knu.database.musebase.auth.manager;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Controls manager session states
 */
@NoArgsConstructor
public class ManagerSessionWrapper {
    @Getter
    private ManagerSession managerSession = null;

    public void updateManagerSession(ManagerSession managerSession) {
        this.managerSession = managerSession;
    }

    public boolean validateLogin() {
        return this.managerSession != null && this.managerSession.getLoggedInId() != null;
    }
}
