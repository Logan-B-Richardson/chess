package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import org.mindrot.jbcrypt.BCrypt;

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
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        try (var con = DatabaseManager.getConnection();
             var ps = con.prepareStatement(sql)) {
                 ps.setString(1, user.username());
                 ps.setString(2, hashedPassword);
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
        String sql = """
                DELETE FROM auth
                WHERE authToken = ?
                """;
        try (var con = DatabaseManager.getConnection();
            var ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int createGame(String gamename) {
        String sql = """
                INSERT INTO game (gameName)
                VALUES (?)
                """;
        try (var con = DatabaseManager.getConnection();
            var ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, gamename);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public GameData getGame(int gameid) {
        String sql = """
                SELECT gameID, whiteUsername, blackUsername, gameName
                FROM game
                WHERE gameID = ?
                """;
        try (var con = DatabaseManager.getConnection();
            var ps = con.prepareStatement(sql)) {
            ps.setInt(1, gameid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new GameData(
                            rs.getInt("gameID"),
                            null,
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName")
                    );
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Collection<GameData> listGames() {
        String sql = """
                SELECT gameID, whiteUsername, blackUsername, gameName
                FROM game
                """;
        var games = new java.util.ArrayList<GameData>();
        try (var con = DatabaseManager.getConnection();
            var ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                games.add(new GameData(
                        rs.getInt("gameID"),
                        null,
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return games;
    }

    @Override
    public void updateGame(GameData game) {
        String sql = """
                UPDATE game
                SET whiteUsername = ?, blackUsername = ?
                WHERE gameID = ?
                """;
        try (var con = DatabaseManager.getConnection();
            var ps = con.prepareStatement(sql)) {
            ps.setString(1, game.whiteusername());
            ps.setString(2, game.blackusername());
            ps.setInt(3, game.gameid());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
