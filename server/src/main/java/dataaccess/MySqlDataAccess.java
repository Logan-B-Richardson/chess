package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class MySqlDataAccess implements DataAccess{
    public MySqlDataAccess() {}

    @Override
    public UserData getUser(String username) {
        String sql = """
                SELECT username, password, email
                FROM user
                WHERE username = ?
                """;
        try (var con = DatabaseManager.getConnection();
            var ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void createUser(UserData user) {
        String sql = """
                    INSERT INTO user (username, password, email)
                    VALUES (?, ?, ?)
                    """;
        try (var con = DatabaseManager.getConnection();
             var ps = con.prepareStatement(sql)) {
                 ps.setString(1, user.username());
                 ps.setString(2, user.password());
                 ps.setString(3, user.email());
                 ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) {
        String sql = """
                SELECT authToken, username
                FROM auth
                WHERE authToken = ?
                """;
        try (var con = DatabaseManager.getConnection();
            var ps = con.prepareStatement(sql)) {
            ps.setString(1, authToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(
                            rs.getString("authToken"),
                            rs.getString("username")
                    );
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void createAuth(AuthData token) {
        String sql = """
                INSERT INTO auth (authToken, username)
                VALUES (?, ?)
                """;
        try (var con = DatabaseManager.getConnection();
            var ps = con.prepareStatement(sql)) {
            ps.setString(1, token.authToken());
            ps.setString(2, token.username());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAuth(String token) {

    }

    @Override
    public int createGame(String gamename) {
        return 0;
    }

    @Override
    public GameData getGame(int gameid) {
        return null;
    }

    @Override
    public Collection<GameData> listGames() {
        return List.of();
    }

    @Override
    public void updateGame(GameData game) {

    }

    @Override
    public void clear() {
        try (var con = DatabaseManager.getConnection()) {
            con.prepareStatement("DELETE FROM auth").executeUpdate();
            con.prepareStatement("DELETE FROM game").executeUpdate();
            con.prepareStatement("DELETE FROM user").executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
