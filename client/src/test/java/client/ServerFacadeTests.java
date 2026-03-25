package client;

import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;
import service.records.GameSummary;

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
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    private ServerFacade newFacade() {
        return new ServerFacade(serverUrl);
    }
    private String uniqueUsername() {
        return "user" + System.nanoTime();
    }

    @Test
    public void registerSuccess() throws Exception {
        ServerFacade facade = newFacade();
        AuthData auth = facade.register(uniqueUsername(), "pass", "email@test.com");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertNotNull(auth.username());
    }

    @Test
    public void registerFail() throws Exception {
        ServerFacade facade = newFacade();
        String username = uniqueUsername();
        facade.register(username, "pass", "email@test.com");
        assertThrows(Exception.class, () ->
                facade.register(username, "pass", "email@test.com"));
    }

    @Test
    public void loginSuccess() throws Exception {
        ServerFacade facade = newFacade();
        String username = uniqueUsername();
        facade.register(username, "pass", "email@test.com");
        AuthData auth = facade.login(username, "pass");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals(username, auth.username());
    }

    @Test
    public void loginFail() throws Exception {
        ServerFacade facade = newFacade();
        String username = uniqueUsername();
        facade.register(username, "pass", "email@test.com");
        assertThrows(Exception.class, () ->
                facade.login(username, "wrongpass"));
    }

    @Test
    public void logoutSuccess() throws Exception {
        ServerFacade facade = newFacade();
        AuthData auth = facade.register(uniqueUsername(), "pass", "email@test.com");
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    public void logoutFail() {
        ServerFacade facade = newFacade();
        assertThrows(Exception.class, () ->
                facade.logout("badAuthToken"));
    }

    @Test
    public void createGameSuccess() throws Exception {
        ServerFacade facade = newFacade();
        AuthData auth = facade.register(uniqueUsername(), "pass", "email@test.com");
        int gameID = facade.createGame(auth.authToken(), "testGame");
        assertTrue(gameID > 0);
    }

    @Test
    public void createGameFail() {
        ServerFacade facade = newFacade();
        assertThrows(Exception.class, () ->
                facade.createGame("badAuthToken", "testGame"));
    }

    @Test
    public void listGamesSuccess() throws Exception {
        ServerFacade facade = newFacade();
        AuthData auth = facade.register(uniqueUsername(), "pass", "email@test.com");
        facade.createGame(auth.authToken(), "game1");
        List<GameSummary> games = facade.listGames(auth.authToken());
        assertNotNull(games);
        assertFalse(games.isEmpty());
    }

    @Test
    public void listGamesFail() {
        ServerFacade facade = newFacade();
        assertThrows(Exception.class, () ->
                facade.listGames("badAuthToken"));
    }

    @Test
    public void joinGameSuccess() throws Exception {
        ServerFacade facade = newFacade();
        AuthData auth = facade.register(uniqueUsername(), "pass", "email@test.com");
        int gameID = facade.createGame(auth.authToken(), "game1");
        assertDoesNotThrow(() ->
                facade.joinGame(auth.authToken(), "WHITE", gameID));
    }

    @Test
    public void joinGameFail() throws Exception {
        ServerFacade facade = newFacade();
        AuthData auth = facade.register(uniqueUsername(), "pass", "email@test.com");
        assertThrows(Exception.class, () ->
                facade.joinGame(auth.authToken(), "WHITE", 999999));
    }
}
