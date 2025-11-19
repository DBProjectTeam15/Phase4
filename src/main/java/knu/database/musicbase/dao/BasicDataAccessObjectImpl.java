package knu.database.musicbase.dao;

import knu.database.musicbase.infra.ConnectionManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BasicDataAccessObjectImpl<T, K extends Serializable> implements BasicDataAccessObject<T, K> {
    private final ConnectionManager connectionManager = ConnectionManager.getInstance();

    protected Connection getConnection() throws SQLException {
        try {
            return connectionManager.getConnection();
        }
        catch (SQLException e) {
            throw new IllegalStateException("커넥션 풀로부터 커넥션 객체를 가져오는 중 오류가 발생했습니다.");
        }
    }
}
