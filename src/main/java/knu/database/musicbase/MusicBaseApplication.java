package knu.database.musicbase;

import knu.database.musicbase.infra.ConnectionManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MusicBaseApplication {

    public static void main(String[] args) {

        try {
            // YamlConfig 관련 코드를 모두 제거하고,
            // 이전에 사용자님이 알려주신 DB 정보를 직접 ConnectionManager에 전달합니다.
            String dbUsername = "musicbase";
            String dbPassword = "musicbase1234";
            String dbUrl = "jdbc:oracle:thin:@localhost:1522:xe";

            // ConnectionManager.init(String, String, String) 오버로드 메서드를 사용합니다.
            ConnectionManager.init(dbUsername, dbPassword, dbUrl);
            System.out.println("ConnectionManager 초기화 완료.");

        } catch (Exception e) {
            System.err.println("ConnectionManager 초기화 중 심각한 오류 발생:");
            e.printStackTrace();
            return;
        }

        SpringApplication.run(MusicBaseApplication.class, args);
    }
}