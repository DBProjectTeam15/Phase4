package knu.database.musebase.console;

import knu.database.musebase.service.auth.AuthService;
import knu.database.musebase.auth.SessionWrapper;
import knu.database.musebase.crypto.PasswordEncryptor;
import knu.database.musebase.crypto.PasswordEncryptorImpl;
import knu.database.musebase.dao.ArtistDAO;
import knu.database.musebase.dao.ProviderDAO;
import knu.database.musebase.dao.SongDAO;
import knu.database.musebase.service.auth.ManagerAuthService;
import knu.database.musebase.auth.manager.ManagerSessionWrapper;
import knu.database.musebase.controller.manager.ArtistManageController;
import knu.database.musebase.controller.manager.ManagerMainController;
import knu.database.musebase.controller.manager.ProviderManageController;
import knu.database.musebase.controller.manager.SongManageController;
import knu.database.musebase.controller.manager.SongRequestManageController;
import knu.database.musebase.dao.manager.ManagerDAO;
import knu.database.musebase.dao.manager.SongRequestDAO;
import knu.database.musebase.exception.InvalidLoginStateException;

import java.util.HashMap;
import java.util.Scanner;

public class ConsoleApplication {
    public void run(ConsoleMode mode) {
        if (mode == ConsoleMode.MANAGER) {
            runManager();
        }
        else {
            run();
        }
    }

    private static void clearScreen() {
        System.out.print("\u001B[H\u001B[2J");
        System.out.flush();
    }

    private void runManager() {

        // doing di

        // application states
        ManagerPageKey managerPageKey = ManagerPageKey.MANAGER_MAIN;
        ManagerSessionWrapper managerSessionWrapper = new ManagerSessionWrapper();

        // dao
        var managerDAO = new ManagerDAO();
        var songDAO = new SongDAO();
        var providerDAO = new ProviderDAO();
        var songRequestDAO = new SongRequestDAO();
        var artistDAO = new ArtistDAO();

        // crypto
        var passwordEncryptor = (PasswordEncryptor) new PasswordEncryptorImpl();

        // services
        var managerAuthService = new ManagerAuthService(passwordEncryptor, managerDAO);

        var pageControllers = new HashMap<ManagerPageKey, PageController<ManagerPageKey>>();

        pageControllers.put(ManagerPageKey.ARTIST_MANAGEMENT, new ArtistManageController(managerAuthService, managerSessionWrapper, artistDAO));
        pageControllers.put(ManagerPageKey.MANAGER_MAIN, new ManagerMainController(managerSessionWrapper, managerAuthService));
        pageControllers.put(ManagerPageKey.PROVIDER_MANAGEMENT, new ProviderManageController(managerSessionWrapper, providerDAO));
        pageControllers.put(ManagerPageKey.SONG_MANAGEMENT, new SongManageController(managerSessionWrapper, songDAO));
        pageControllers.put(ManagerPageKey.REQUEST_MANAGEMENT, new SongRequestManageController(managerSessionWrapper, songRequestDAO));
        pageControllers.put(ManagerPageKey.ARTIST_DETAILS, null);
        pageControllers.put(ManagerPageKey.EXIT, null);

        Scanner scanner = new Scanner(System.in);

        clearScreen();

        while (managerPageKey != ManagerPageKey.EXIT) {
            PageController<ManagerPageKey> pageController = pageControllers.get(managerPageKey);

            try {
                pageController.displayScreen();
            } catch (InvalidLoginStateException e) {
                System.out.println(e.getMessage() + '\n');
                managerPageKey = ManagerPageKey.MANAGER_MAIN;
                continue;
            }

            String command = scanner.nextLine();
            managerPageKey = pageController.invoke(command.split(" "));
            clearScreen();
        }

        scanner.close();
    }

    private void run() {

    }
}
