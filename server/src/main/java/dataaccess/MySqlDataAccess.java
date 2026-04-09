package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import chess.ChessGame;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import org.mindrot.jbcrypt.BCrypt;
import com.google.gson.Gson;

public class MySqlDataAccess implements DataAccess{
    private final Gson gson = new Gson();

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
        ChessGame game = new ChessGame();
        game.getBoard().resetBoard();
        String gameJson = gson.toJson(game);

        String sql = """
                INSERT INTO game (whiteUsername, blackUsername, gameName, gameState)
                VALUES (?, ?, ?, ?)
                """;
        try (var con = DatabaseManager.getConnection();
            var ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, null);
            ps.setString(2, null);
            ps.setString(3, gamename);
            ps.setString(4, gameJson);
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
    public GameData getGame(int gameID) {
        String sql = """
                SELECT gameID, whiteUsername, blackUsername, gameName, gameState
                FROM game
                WHERE gameID = ?
                """;
        try (var con = DatabaseManager.getConnection();
            var ps = con.prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ChessGame game = gson.fromJson(rs.getString("gameState"), ChessGame.class);
                    return new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            game
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
        var games = new ArrayList<GameData>();
        try (var con = DatabaseManager.getConnection();
             var ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                games.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        null
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
                SET whiteUsername = ?, blackUsername = ?, gameName = ?, gameState = ?
                WHERE gameID = ?
                """;
        try (var con = DatabaseManager.getConnection();
            var ps = con.prepareStatement(sql)) {
            ps.setString(1, game.whiteusername());
            ps.setString(2, game.blackusername());
            ps.setString(3, game.gamename());
            ps.setString(4, gson.toJson(game.game()));
            ps.setInt(5, game.gameid());
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
