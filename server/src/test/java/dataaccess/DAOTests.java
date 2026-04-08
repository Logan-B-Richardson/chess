package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class DAOTests {
    private MySqlDataAccess dao;

    @BeforeEach
    public void setup() {
        dao = new MySqlDataAccess();
        dao.clear();
    }

    @Test
    public void clearPositive() {
        dao.createUser(new UserData("u", "p", "e@test.com"));
        dao.clear();
        assertNull(dao.getUser("u"));
    }

    @Test
    public void createUserPositive() {
        UserData user = new UserData("logan", "pw", "l@test.com");
        dao.createUser(user);
        UserData actual = dao.getUser("logan");
        assertNotNull(actual);
        assertEquals("logan", actual.username());
        assertEquals("l@test.com", actual.email());
        assertNotEquals("pw", actual.password()); // hashed
    }

    @Test
    public void createUserNegative() {
        UserData user = new UserData("logan", "pw", "l@test.com");
        dao.createUser(user);
        assertThrows(RuntimeException.class, ()->dao.createUser(user));
    }

    @Test
    public void getUserPositive() {
        dao.createUser(new UserData("logan", "pw", "l@test.com"));
        assertNotNull(dao.getUser("logan"));
    }

    @Test
    public void getUserNegative() {
        assertNull(dao.getUser("missing"));
    }

    @Test
    public void createAuthPositive() {
        AuthData auth = new AuthData("token123", "logan");
        dao.createAuth(auth);
        assertNotNull(dao.getAuth("token123"));
    }

    @Test
    public void createAuthNegative() {
        AuthData auth = new AuthData("token123", "logan");
        dao.createAuth(auth);
        assertThrows(RuntimeException.class, ()->dao.createAuth(auth));
    }

    @Test
    public void getAuthPositive() {
        dao.createAuth(new AuthData("token123", "logan"));
        assertEquals("logan", dao.getAuth("token123").username());
    }

    @Test
    public void getAuthNegative() {
        assertNull(dao.getAuth("bad-token"));
    }

    @Test
    public void deleteAuthPositive() {
        dao.createAuth(new AuthData("token123", "logan"));
        dao.deleteAuth("token123");
        assertNull(dao.getAuth("token123"));
    }

    @Test
    public void deleteAuthNegative() {
        assertDoesNotThrow(() -> dao.deleteAuth("missing"));
    }

    @Test
    public void createGamePositive() {
        int id = dao.createGame("test game");
        assertTrue(id > 0);
        assertEquals("test game", dao.getGame(id).gamename());
    }

    @Test
    public void createGameNegative() {
        assertThrows(RuntimeException.class, ()->dao.createGame(null));
    }

    @Test
    public void getGamePositive() {
        int id = dao.createGame("test game");
        assertNotNull(dao.getGame(id));
    }

    @Test
    public void getGameNegative() {
        assertNull(dao.getGame(-1));
    }

    @Test
    public void listGamesPositive() {
        dao.createGame("g1");
        dao.createGame("g2");
        Collection<GameData> games = dao.listGames();
        assertEquals(2, games.size());
    }

    @Test
    public void listGamesNegative() {
        assertTrue(dao.listGames().isEmpty());
    }

    @Test
    public void updateGamePositive() {
        int id = dao.createGame("g1");
        GameData oldGame = dao.getGame(id);
        GameData updated = new GameData(
                id,
                "white",
                "black",
                oldGame.gamename(),
                oldGame.game());
        dao.updateGame(updated);
        GameData actual = dao.getGame(id);
        assertEquals("white", actual.whiteusername());
        assertEquals("black", actual.blackusername());
    }

    @Test
    public void updateGameNegative() {
        GameData fake = new GameData(9999,
                "w",
                "b",
                "fake",
                new ChessGame());
        assertDoesNotThrow(() -> dao.updateGame(fake));
        assertNull(dao.getGame(9999));
    }
}
