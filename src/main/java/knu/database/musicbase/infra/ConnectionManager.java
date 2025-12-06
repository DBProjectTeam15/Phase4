package knu.database.musicbase.infra;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import knu.database.musicbase.config.YamlConfig;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class ConnectionManager {
    private static ConnectionManager instance;
    private final DataSource dataSource;

    public static void init() {
        var config = YamlConfig.getInstance();
        instance = new ConnectionManager(config.getDatabaseUsername(), config.getDatabasePassword(), config.getDatabaseUrl());
    }

    public static void init(String username, String password, String url) {
        instance = new ConnectionManager(username, password, url);
    }

    public static ConnectionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(ConnectionManager.class.getSimpleName() + ".init 메서드를 먼저 호출하고 사용해주세요.");
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private ConnectionManager(String username, String password, String url) {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setDriverClassName("oracle.jdbc.driver.OracleDriver");

        try {
            this.dataSource = new HikariDataSource(hikariConfig);
        }
        catch (HikariPool.PoolInitializationException ex) {
            log.error("데이터 베이스 시작 에러 : " + ex.getMessage());
            throw ex;
        }

    }
}
