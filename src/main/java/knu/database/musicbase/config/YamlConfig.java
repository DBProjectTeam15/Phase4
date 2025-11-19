package knu.database.musicbase.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
public class YamlConfig {
    private static YamlConfig instance;

    @Getter
    private String databaseUsername;

    @Getter
    private String databasePassword;

    @Getter
    private String databaseUrl;

    public static void init(String flag) throws IOException {
        instance = new YamlConfig(flag);
    }

    public static YamlConfig getInstance() {
        if (instance == null) throw new IllegalStateException("Config 없이 호출되었습니다. "
                + YamlConfig.class.getSimpleName()
                + " .init(flag) 를 먼저 호출하고 사용해주세요.");

        return instance;
    }

    private YamlConfig(String flag) throws IOException {
        InputStream inputStream = YamlConfig.class
                .getClassLoader()
                .getResourceAsStream("application-" + flag + ".yaml");

        if (inputStream == null) {
            throw new NoSuchElementException("application-" + flag + ".yaml" + " not found on classpath.");
        }

        Map<String, Object> config = new Yaml().load(inputStream);

        if (config.containsKey("database") && config.get("database") instanceof Map) {

            @SuppressWarnings("unchecked") // 타입 캐스팅 경고를 무시
            Map<String, Object> databaseConfig = (Map<String, Object>) config.get("database");

            // 내부 맵(databaseConfig)에서 원하는 키로 값을 가져옴
            this.databasePassword = (String) databaseConfig.get("password");
            this.databaseUsername = (String) databaseConfig.get("username");
            this.databaseUrl = (String) databaseConfig.get("url");

            if (this.databasePassword == null
                    || this.databaseUrl == null
                    || this.databaseUsername == null
            ) {
                throw new IllegalArgumentException("데이터베이스 설정이 완전하지 않습니다.");
            }
        }

        inputStream.close();
    }
}