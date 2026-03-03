package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import service.exceptions.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private DataAccess dao;
    private UserService userService;
    private GameService gameService;
    private String authToken;

    @BeforeEach
    void setup() {
        dao = new InMemoryDataAccess();
        userService = new UserService(dao);
        gameService = new GameService(dao);
        var reg = userService.register(new RegisterRequest("user", "pw", "u@mail.com"));
        authToken = reg.authToken();
    }

    @Test
    void register_positive() {
        var result = userService.register(
                new RegisterRequest("logan", "pw", "l@mail.com"));
        assertNotNull(result.authToken());
        assertEquals("logan", result.username());
        assertNotNull(dao.getUser("logan"));
    }

    @Test
    void register_negative_duplicate() {
        userService.register(new RegisterRequest("dup", "pw", "d@mail.com"));
        assertThrows(AlreadyTakenException.class, () ->
                userService.register(new RegisterRequest("dup", "pw", "d@mail.com")));
    }

    @Test
    void login_positive() {
        userService.register(new RegisterRequest("bob", "pw", "b@mail.com"));
        var result = userService.login(new LoginRequest("bob", "pw"));
        assertNotNull(result.authToken());
        assertEquals("bob", result.username());
    }

    @Test
    void login_negative_wrongPassword() {
        userService.register(new RegisterRequest("bob", "pw", "b@mail.com"));
        assertThrows(UnauthorizedException.class, () ->
                userService.login(new LoginRequest("bob", "pa")));
    }

    @Test
    void logout_positive() {
        userService.logout(authToken);
        assertNull(dao.getAuth(authToken));
    }

    @Test
    void logout_negative_invalidToken() {
        assertThrows(UnauthorizedException.class, () ->
                userService.logout("token"));
    }

    @Test
    void createGame_positive() {
        var result = gameService.createGame(
                authToken,
                new CreateGameRequest("mygame"));
         assertNotNull(dao.getGame(result.gameID()));
    }

    @Test
    void createGame_negative_unauthorized() {
        assertThrows(UnauthorizedException.class, () ->
                gameService.createGame("badtoken",
                        new CreateGameRequest("mygame")));
    }

    @Test
    void listGames_positive() {
        gameService.createGame(authToken,
                new CreateGameRequest("g1"));
        var result = gameService.listGames(authToken);
        assertNotNull(result.games());
        assertFalse(result.games().isEmpty());
    }

    @Test
    void listGames_negative_unauthorized() {
        assertThrows(UnauthorizedException.class, () ->
                gameService.listGames("badtoken"));
    }

    @Test
    void joinGame_positive() {
        int gid = gameService
                .createGame(authToken,
                        new CreateGameRequest("g"))
                .gameID();
        gameService.joinGame(authToken,
                new JoinGameRequest("WHITE", gid));
        var game = dao.getGame(gid);
        assertEquals("user", game.whiteusername());
    }

    @Test
    void joinGame_negative_colorTaken() {
        int gid = gameService
                .createGame(authToken,
                        new CreateGameRequest("g"))
                .gameID();
        gameService.joinGame(authToken,
                new JoinGameRequest("WHITE", gid));
        var reg2 = userService.register(
                new RegisterRequest("user2", "pw", "u2@mail.com"));
        assertThrows(ForbiddenException.class, () ->
                gameService.joinGame(reg2.authToken(),
                        new JoinGameRequest("WHITE", gid)));
    }

    static class InMemoryDataAccess implements DataAccess {

        private final Map<String, UserData> users = new HashMap<>();
        private final Map<String, AuthData> auths = new HashMap<>();
        private final Map<Integer, GameData> games = new HashMap<>();
        private int nextId = 1;

        @Override
        public UserData getUser(String username) {
            return users.get(username);
        }

        @Override
        public void createUser(UserData user) {
            users.put(user.username(), user);
        }

        @Override
        public AuthData getAuth(String token) {
            return auths.get(token);
        }

        @Override
        public void createAuth(AuthData token) {
            auths.put(token.authToken(), token);
        }

        @Override
        public void deleteAuth(String token) {
            auths.remove(token);
        }

        @Override
        public int createGame(String gamename) {
            int id = nextId++;
            GameData game = new GameData(id, null, null, gamename, null);
            games.put(id, game);
            return id;
        }

        @Override
        public GameData getGame(int gameid) {
            return games.get(gameid);
        }

        @Override
        public Collection<GameData> listGames() {
            return games.values();
        }

        @Override
        public void updateGame(GameData game) {
            games.put(game.gameid(), game);
        }

        @Override
        public void clear() {

        }
    }
}
