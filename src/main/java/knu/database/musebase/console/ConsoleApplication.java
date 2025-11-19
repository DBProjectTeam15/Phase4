package knu.database.musebase.console;

import knu.database.musebase.controller.MainController;
import knu.database.musebase.controller.my.MyCommentController;
import knu.database.musebase.controller.my.MyPageController;
import knu.database.musebase.controller.playlist.MainPlaylistController;
import knu.database.musebase.controller.playlist.MyPagePlaylistController;
import knu.database.musebase.controller.playlist.PlaylistDetailController;
import knu.database.musebase.controller.search.ArtistResultController;
import knu.database.musebase.controller.search.ArtistSearchController;
import knu.database.musebase.controller.search.PlaylistResultController;
import knu.database.musebase.controller.search.PlaylistSearchController;
import knu.database.musebase.controller.search.SearchController;
import knu.database.musebase.controller.search.SongResultController;
import knu.database.musebase.controller.search.SongSearchController;
import knu.database.musebase.dao.CommentDAO;
import knu.database.musebase.dao.PlaylistDAO;
import knu.database.musebase.service.ArtistService;
import knu.database.musebase.service.CommentService;
import knu.database.musebase.service.PlaylistDetailService;
import knu.database.musebase.service.PlaylistService;
import knu.database.musebase.auth.AuthService;
import knu.database.musebase.auth.SessionWrapper;
import knu.database.musebase.crypto.PasswordEncryptor;
import knu.database.musebase.crypto.PasswordEncryptorImpl;
import knu.database.musebase.dao.ArtistDAO;
import knu.database.musebase.dao.ProviderDAO;
import knu.database.musebase.dao.SongDAO;
import knu.database.musebase.auth.manager.ManagerAuthService;
import knu.database.musebase.auth.manager.ManagerSessionWrapper;
import knu.database.musebase.controller.manager.ArtistManageController;
import knu.database.musebase.controller.manager.ManagerMainController;
import knu.database.musebase.controller.manager.ProviderManageController;
import knu.database.musebase.controller.manager.SongManageController;
import knu.database.musebase.controller.manager.SongRequestManageController;
import knu.database.musebase.dao.UserDAO;
import knu.database.musebase.dao.manager.ManagerDAO;
import knu.database.musebase.dao.manager.SongRequestDAO;
import knu.database.musebase.exception.InvalidLoginStateException;
import knu.database.musebase.service.SongService;

import java.util.HashMap;
import java.util.Scanner;

public class ConsoleApplication {
    public void init(ConsoleMode mode) {
        if (mode == ConsoleMode.MANAGER) {
            runManager();
        }
        else {
            init();
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

    private void init() {

        // doing di

        // application states
        PageKey pageKey = PageKey.MAIN;
        SessionWrapper sessionWrapper = new SessionWrapper();

        // dao
        var userDAO = new UserDAO();
        var songDAO = new SongDAO();
        var providerDAO = new ProviderDAO();
        var songRequestDAO = new SongRequestDAO();
        var artistDAO = new ArtistDAO();
        var playlistDAO = new PlaylistDAO();
        var commentDAO = new CommentDAO();

        // crypto
        var passwordEncryptor = (PasswordEncryptor) new PasswordEncryptorImpl();

        // services
        var authService = new AuthService(passwordEncryptor, userDAO);
        var playlistService = new PlaylistService(playlistDAO);
        var commentService = new CommentService(commentDAO);
        var songService = new SongService(songDAO);
        var artistService = new ArtistService(artistDAO);
        var playlistDetailService = new PlaylistDetailService(playlistDAO);

        var pageControllers = new HashMap<PageKey, PageController<PageKey>>();

        pageControllers.put(PageKey.MAIN, new MainController(sessionWrapper, authService, playlistService));
        pageControllers.put(PageKey.MY_PAGE, new MyPageController(sessionWrapper, authService, playlistService, commentService, passwordEncryptor, userDAO));
        pageControllers.put(PageKey.MY_PAGE_COMMENT, new MyCommentController(commentService));
        pageControllers.put(PageKey.PLAYLIST_PAGE, new MainPlaylistController(playlistService, playlistDetailService));

        pageControllers.put(PageKey.MY_PAGE_PLAYLIST, new MyPagePlaylistController(playlistService, playlistDetailService));
        pageControllers.put(PageKey.SEARCH, new SearchController());
        pageControllers.put(PageKey.PLAYLIST_SEARCH, new PlaylistSearchController(playlistService));
        pageControllers.put(PageKey.PLAYLIST_SEARCH_RESULT, new PlaylistResultController(playlistService));
        pageControllers.put(PageKey.PLAYLIST_DETAIL, new PlaylistDetailController(playlistDetailService));
        pageControllers.put(PageKey.SONG_SEARCH, new SongSearchController(songService));
        pageControllers.put(PageKey.SONG_RESULT, new SongResultController(songService));
        pageControllers.put(PageKey.ARTIST_RESULT, new ArtistResultController(artistService));
        pageControllers.put(PageKey.ARTIST_SEARCH, new ArtistSearchController(artistService));


        Scanner scanner = new Scanner(System.in);

        clearScreen();

        while (pageKey != PageKey.EXIT) {
            PageController<PageKey> pageController = pageControllers.get(pageKey);

            try {
                pageController.displayScreen();
            } catch (InvalidLoginStateException e) {
                System.out.println(e.getMessage() + '\n');
                pageKey = PageKey.MAIN;
                continue;
            }

            String command = scanner.nextLine();

            pageKey = pageController.invoke(command.split(" "));
            clearScreen();
        }

        scanner.close();
    }
}
