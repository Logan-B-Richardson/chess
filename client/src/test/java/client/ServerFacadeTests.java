package client;

import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {
    private static Server server;
    private static String serverUrl;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        serverUrl = "http://localhost:" + port;
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void registerSuccess() throws Exception {
        ServerFacade facade = new ServerFacade(serverUrl);
        String username = "user" + System.currentTimeMillis();
        AuthData auth = facade.register(username, "pass", "email@test.com");
        assertNotNull(auth.authToken());
    }

    @Test
    public void loginSuccess() throws Exception {
        ServerFacade facade = new ServerFacade(serverUrl);
        String username = "user" + System.currentTimeMillis();
        facade.register(username, "pass", "email@test.com");
        AuthData auth = facade.login(username, "pass");
        assertNotNull(auth.authToken());
    }

    @Test
    public void createGameSuccess() throws Exception {
        ServerFacade facade = new ServerFacade(serverUrl);
        String username = "user" + System.currentTimeMillis();
        AuthData auth = facade.register(username, "pass", "email@test.com");
        int gameID = facade.createGame(auth.authToken(), "testGame");
        assertTrue(gameID > 0);
    }

    @Test
    public void listGamesSuccess() throws Exception {
        ServerFacade facade = new ServerFacade(serverUrl);
        String username = "user" + System.currentTimeMillis();
        AuthData auth = facade.register(username, "pass", "email@test.com");
        facade.createGame(auth.authToken(), "game1");
        List<?> games = facade.listGames(auth.authToken());
        assertNotNull(games);
        assertTrue(games.size() >= 1);
    }

    @Test
    public void joinGameSuccess() throws Exception {
        ServerFacade facade = new ServerFacade(serverUrl);
        String username = "user" + System.currentTimeMillis();
        AuthData auth = facade.register(username, "pass", "email@test.com");
        int gameID = facade.createGame(auth.authToken(), "game1");
        facade.joinGame(auth.authToken(), "WHITE", gameID);
        assertTrue(true);
    }

    @Test
    public void registerDuplicateFail() throws Exception {
        ServerFacade facade = new ServerFacade(serverUrl);
        String username = "user" + System.currentTimeMillis();
        facade.register(username, "pass", "email@test.com");
        assertThrows(Exception.class, () -> {
            facade.register(username, "pass", "email@test.com");
        });
    }

    @Test
    public void loginWrongPasswordFail() throws Exception {
        ServerFacade facade = new ServerFacade(serverUrl);
        String username = "user" + System.currentTimeMillis();
        facade.register(username, "pass", "email@test.com");
        assertThrows(Exception.class, () -> {
            facade.login(username, "wrong");
        });
    }

    @Test
    public void listGamesUnauthorizedFail() {
        ServerFacade facade = new ServerFacade(serverUrl);
        assertThrows(Exception.class, () -> {
            facade.listGames("badAuthToken");
        });
    }

    @Test
    public void joinGameInvalidFail() throws Exception {
        ServerFacade facade = new ServerFacade(serverUrl);
        String username = "user" + System.currentTimeMillis();
        AuthData auth = facade.register(username, "pass", "email@test.com");
        assertThrows(Exception.class, () -> {
            facade.joinGame(auth.authToken(), "WHITE", 999999);
        });
    }
}
