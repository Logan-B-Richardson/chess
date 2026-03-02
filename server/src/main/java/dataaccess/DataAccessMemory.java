package dataaccess;

import chess.ChessGame;
import io.javalin.http.UseProxyResponse;
import model.*;
import java.util.*;

public class DataAccessMemory implements DataAccess {

    // global variables
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> auths = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextid = 1;

    // UserData functions
    @Override public UserData getUser(String username) {
        return users.get(username);
    }
    @Override public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    // AuthData functions
    @Override public AuthData getAuth(String authToken) {
        return auths.get(authToken);
    }
    @Override public void createAuth(AuthData token) {
        auths.put(token.authToken(), token);
    }
    @Override public void deleteAuth(String token) {
        auths. remove(token);
    }

    // GameData functions
    @Override public int createGame(String gamename) {
        int id = nextid++;
        GameData game = new GameData(
                id,
                new ChessGame(),
                null,
                null,
                gamename
        );
        games.put(id, game);
        return id;
    }
    @Override public GameData getGame(int gameid) {
        return games.get(gameid);
    }
    @Override public Collection<GameData> listGames() {
        return games.values();
    }
    @Override public void updateGame(GameData game) {
        games.put(game.gameid(), game);
    }

    // clear function
    @Override public void clear () {
        users.clear();
        auths.clear();
        games.clear();
        nextid = 1;
    }
}
