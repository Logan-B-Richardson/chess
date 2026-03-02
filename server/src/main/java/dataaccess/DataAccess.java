package dataaccess;

import model.*;
import org.eclipse.jetty.server.Authentication;

import java.util.Collection;

public interface DataAccess {
    // UserData interface
    UserData getUser(String username);
    void createUser(UserData user);

    // AuthData interface
    AuthData getAuth(String authToken);
    void createAuth(AuthData token);
    void deleteAuth(String token);

    // GameData interface
    int createGame(String gamename);
    GameData getGame(int gameid);
    Collection<GameData> listGames();
    void updateGame(GameData game);

    // clear function
    void clear();
}
