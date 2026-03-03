package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessMemory;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import service.exceptions.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private DataAccess dao;
    private UserService userService;
    private GameService gameService;

    @BeforeEach
    void setup() {
        dao = new DataAccessMemory();
        userService = new UserService(dao);
        gameService = new GameService(dao);
    }

    private String seedUserAndLogin(String username) {
        userService.register(new RegisterRequest(username, "pw", username + "@mail.com"));
        var login = userService.login(new LoginRequest(username, "pw"));
        assertNotNull(login.authToken());
        assertNotNull(dao.getAuth(login.authToken()));
        return login.authToken();
    }

    @Test
    void registerPositiveCreatesUser() {
        var result = userService.register(new RegisterRequest("bob", "pw", "bob@mail.com"));
        assertEquals("bob", result.username());
        assertNotNull(result.authToken());
        UserData stored = dao.getUser("bob");
        assertNotNull(stored);
        assertEquals("bob", stored.username());
    }

    @Test
    void registerNegativeDuplicateUsernameThrows() {
        userService.register(new RegisterRequest("bob", "pw", "bob@mail.com"));
        assertThrows(AlreadyTakenException.class,
                () -> userService.register(new RegisterRequest("bob", "pw2", "bob2@mail.com")));
    }

    @Test
    void login_positiveReturnsTokenAndStoresAuth() {
        userService.register(new RegisterRequest("alice", "pw", "alice@mail.com"));
        var result = userService.login(new LoginRequest("alice", "pw"));
        assertEquals("alice", result.username());
        assertNotNull(result.authToken());
        assertNotNull(dao.getAuth(result.authToken()));
    }

    @Test
    void loginNegativeWrongPasswordThrows() {
        userService.register(new RegisterRequest("alice", "pw", "alice@mail.com"));
        assertThrows(UnauthorizedException.class,
                () -> userService.login(new LoginRequest("alice", "BADPW")));
    }

    @Test
    void logoutPositiveDeletesAuth() {
        String token = seedUserAndLogin("carl");
        userService.logout(token);
        assertNull(dao.getAuth(token));
    }

    @Test
    void logoutNegativeInvalidTokenThrows() {
        assertThrows(UnauthorizedException.class, () -> userService.logout("not-a-real-token"));
    }

    @Test
    void listGamesPositiveReturnsGames() {
        String token = seedUserAndLogin("dana");
        gameService.createGame(token, new CreateGameRequest("g1"));
        gameService.createGame(token, new CreateGameRequest("g2"));
        var results = gameService.listGames(token);
        assertNotNull(results.games());
        assertEquals(2, results.games().size());
    }

    @Test
    void listGamesNegativeUnauthorizedThrows() {
        assertThrows(UnauthorizedException.class, () -> gameService.listGames("bad-token"));
    }

    @Test
    void createGamePositiveStoresGame() {
        String token = seedUserAndLogin("erin");
        var result = gameService.createGame(token, new CreateGameRequest("my game"));
        assertTrue(result.gameID() > 0);
        assertNotNull(dao.getGame(result.gameID())); // game exists in DAO
    }

    @Test
    void createGameNegativeNullNameThrows() {
        String token = seedUserAndLogin("erin");
        assertThrows(BadRequestException.class, () -> gameService.createGame(token, new CreateGameRequest(null)));
    }

    @Test
    void joinGamePositiveClaimsWhiteSpot() {
        String token = seedUserAndLogin("frank");
        int gameId = gameService.createGame(token, new CreateGameRequest("joinable")).gameID();
        gameService.joinGame(token, new JoinGameRequest("WHITE", gameId));
        var stored = dao.getGame(gameId);
        assertEquals("frank", stored.whiteusername());
    }

    @Test
    void joinGameCegativeWhiteAlreadyTakenThrows() {
        String token1 = seedUserAndLogin("g1");
        String token2 = seedUserAndLogin("g2");
        int gameId = gameService.createGame(token1, new CreateGameRequest("full soon")).gameID();
        gameService.joinGame(token1, new JoinGameRequest("WHITE", gameId));
        assertThrows(ForbiddenException.class,
                () -> gameService.joinGame(token2, new JoinGameRequest("WHITE", gameId)));
    }
}
