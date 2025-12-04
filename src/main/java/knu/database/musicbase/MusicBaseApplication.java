package knu.database.musicbase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// manager 모드를 사용하도록 하는 건 인자를 받아서 하도록 설정
@SpringBootApplication
public class MusicBaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(MusicBaseApplication.class, args);
    }
}