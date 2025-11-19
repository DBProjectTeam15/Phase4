package knu.database.musicbase;

import knu.database.musicbase.console.ConsoleApplication;
import knu.database.musicbase.console.ConsoleMode;
import knu.database.musicbase.config.YamlConfig;
import knu.database.musicbase.infra.ConnectionManager;

import java.io.IOException;

// manager 모드를 사용하도록 하는 건 인자를 받아서 하도록 설정
public class MusicBaseApplication {
    public static void main(String[] args) {
        try {
            YamlConfig.init("develop");
            ConnectionManager.init();
        } catch (IOException e) {
            throw new RuntimeException("설정 파일을 읽는데 문제가 발생했습니다.");
        }

        if (args.length >= 1 && args[0] != null && args[0].equals(ConsoleMode.MANAGER.toLowerCase())) {
            new ConsoleApplication().init(ConsoleMode.MANAGER);
        }
        else {
            new ConsoleApplication().init(ConsoleMode.MAIN);
        }

    }
}